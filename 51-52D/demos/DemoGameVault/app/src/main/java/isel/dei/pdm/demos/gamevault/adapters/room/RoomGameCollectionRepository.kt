package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.withTransaction
import isel.dei.pdm.demos.gamevault.domain.LimitedResult
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.domain.collection.games.AddGameResult
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.StartSessionResult
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A [GameCollectionRepository] that stores the user's collection in a Room database.
 *
 * The domain rules that involve several entries (one entry per game, one active session overall)
 * are enforced in transactions: the entries involved are read, checked against the rules, and
 * written within the same transaction, so that concurrent operations cannot break them. The rules
 * themselves are the domain's (e.g. [NewGame.isSameGameAs], [CollectionEntry.isOwnedOn]): the
 * queries only narrow down the entries they are applied to.
 *
 * The observable queries are backed by Room's flows, which emit again whenever any of the tables
 * they read changes. Emissions equal to the previous one (e.g. after changes to other games) are
 * discarded.
 *
 * As required by the contract, errors are translated into [PersistenceException]s (see
 * [translate]), except those thrown by the changes given to [updateEntry], which are propagated
 * as is, as they are the domain rules rejecting the changes.
 *
 * @param database The database, used to run transactions.
 * @param clock The clock used to timestamp the games added to the collection.
 */
class RoomGameCollectionRepository(
    private val database: GameVaultDatabase,
    private val clock: Clock = Clock.System
) : GameCollectionRepository {

    private val gameDao = database.gameCollectionDao()
    private val platformDao = database.platformDao()

    // Adding games

    override suspend fun findSameEntry(game: NewGame): CollectionEntry? =
        persisting(errorMessage = "Failed to read the collection") {
            database.withTransaction { findSame(game) }
        }

    override suspend fun add(
        game: NewGame,
        platform: Platform,
        playStatus: PlayStatus,
        details: NewGameDetails
    ): AddGameResult = persisting(errorMessage = "Failed to add game") {
        database.withTransaction {
            val existing = findSame(game)
            when {
                existing == null -> {
                    val gameId = gameDao.insert(
                        game = game.toEntity(playStatus, addedAt = clock.now()),
                        platformId = platform.id,
                        // The game's identifier is assigned by the insertion
                        runs = playStatus.toRunEntities(gameId = 0),
                        details = details.toEntity(gameId = 0)
                    )
                    AddGameResult.Added(gameId)
                }
                existing.isOwnedOn(platform) -> AddGameResult.AlreadyOwnedOnPlatform(existing.game)
                else -> {
                    gameDao.insertPlatformLink(GamePlatformEntity(existing.game.id, platform.id))
                    AddGameResult.PlatformAdded(existing.game)
                }
            }
        }
    }

    /**
     * The entry of the game in the collection that is the same as the given one, if any. Meant to
     * be called within a transaction.
     */
    private suspend fun findSame(game: NewGame): CollectionEntry? {
        val year = game.releaseDate?.let { Year.of(it) }
        val sameGame = gameDao
            .getSameGameCandidates(year?.firstDay(), year?.lastDay())
            .asSequence()
            .map { it.toGame() }
            .firstOrNull { game isSameGameAs it }
            ?: return null
        return gameDao.getEntry(sameGame.id)?.toCollectionEntry()
    }

    // Observing the collection

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeItems(
        query: CollectionQuery,
        limit: Int
    ): Flow<LimitedResult<CollectionItem>> {
        require(limit > 0) { "The limit must be positive" }
        // One more row than the limit is asked for, to find out whether there are more
        val rowsLimit = if (limit < Int.MAX_VALUE) limit + 1 else limit
        val platform = query.platform
        val rows =
            if (platform == null) gameDao.observeItems(query, platformIds = null, rowsLimit)
            // The versions of the platform are recognized among the collection's platforms,
            // which are observed too, as new versions may be added while observing
            else platformDao.observeAll()
                .map { platforms -> platform.versionIdsAmong(platforms) }
                .distinctUntilChanged()
                .flatMapLatest { ids -> gameDao.observeItems(query, ids, rowsLimit) }
        return rows
            .map { found ->
                LimitedResult(
                    items = found.take(limit).map { it.toCollectionItem() },
                    hasMore = found.size > limit
                )
            }
            .observed(errorMessage = "Failed to read the collection")
    }

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> =
        gameDao.observeEntry(gameId)
            .map { it?.toCollectionEntry() }
            .observed(errorMessage = "Failed to read the entry")

    override fun observeDetails(gameId: Long): Flow<GameDetails?> =
        gameDao.observeDetails(gameId)
            .map { it?.toGameDetails() }
            .observed(errorMessage = "Failed to read the game's details")

    override fun observeActiveSession(): Flow<CollectionEntry?> =
        gameDao.observeActiveSession()
            .map { it?.toCollectionEntry() }
            .observed(errorMessage = "Failed to read the active session")

    override fun observeSummary(year: Year): Flow<CollectionSummary> =
        gameDao.observeSummary(year.firstDay(), year.lastDay())
            .map { it.toCollectionSummary(year) }
            .observed(errorMessage = "Failed to read the collection's summary")

    // Changing the collection

    override suspend fun updateEntry(
        gameId: Long,
        change: (CollectionEntry) -> CollectionEntry
    ): CollectionEntry? = persisting(errorMessage = "Failed to update the entry") {
        database.withTransaction {
            val current = gameDao.getEntry(gameId)?.toCollectionEntry() ?: return@withTransaction null
            val updated = rejectable {
                change(current).also {
                    require(it.game.id == gameId) { "The change cannot replace the entry's game" }
                    check(!it.isSessionActive || current.isSessionActive) {
                        "Sessions must be started through startSession"
                    }
                }
            }
            if (updated != current) store(updated)
            updated
        }
    }

    override suspend fun startSession(gameId: Long, now: Instant): StartSessionResult =
        persisting(errorMessage = "Failed to start the session") {
            database.withTransaction {
                val current = gameDao.getEntry(gameId)?.toCollectionEntry()
                    ?: return@withTransaction StartSessionResult.NotInCollection
                val active = gameDao.getActiveSession()?.toCollectionEntry()
                if (active != null) {
                    return@withTransaction StartSessionResult.AnotherSessionActive(active)
                }
                val updated = current.startSession(now)
                store(updated)
                StartSessionResult.Started(updated)
            }
        }

    override suspend fun updateGame(game: Game): Boolean =
        persisting(errorMessage = "Failed to update the game") {
            gameDao.updateGameInfo(game.toInfoUpdate())
        }

    override suspend fun updateDetails(details: GameDetails): Boolean =
        persisting(errorMessage = "Failed to update the game's details") {
            gameDao.updateDetails(details.toEntity())
        }

    override suspend fun remove(gameId: Long): Boolean =
        persisting(errorMessage = "Failed to remove the game") {
            gameDao.delete(gameId) > 0
        }

    /**
     * Stores the given entry, replacing the stored one. Meant to be called within a transaction.
     */
    private suspend fun store(entry: CollectionEntry) {
        gameDao.update(
            game = entry.toEntity(),
            platforms = entry.toPlatformEntities(),
            runs = entry.playStatus.toRunEntities(entry.game.id)
        )
    }

    // Error handling

    /**
     * Wraps the exceptions thrown by the domain rules (e.g. a change given to [updateEntry]), so
     * that they are propagated as is, instead of being translated into persistence exceptions:
     * they may be IllegalArgumentExceptions, which would otherwise be mistaken for stored data
     * that does not meet the domain requirements.
     */
    private class DomainRejection(override val cause: Exception) : Exception(cause)

    /**
     * Executes the given block, whose exceptions are the domain rules rejecting an operation.
     */
    private inline fun <T> rejectable(block: () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            throw DomainRejection(e)
        }

    /**
     * Executes the given block, translating its errors into persistence exceptions, except for
     * the domain rejections (see [rejectable]), which are unwrapped. When thrown within a
     * transaction, both cause it to be rolled back.
     */
    private suspend fun <T> persisting(errorMessage: String, block: suspend () -> T): T =
        try {
            translatingErrors(errorMessage) { block() }
        } catch (e: DomainRejection) {
            throw e.cause
        }

    /**
     * This observable query, discarding repeated emissions and translating its errors.
     */
    private fun <T> Flow<T>.observed(errorMessage: String): Flow<T> =
        distinctUntilChanged().catch { error -> throw translate(error, errorMessage) }
}

/**
 * The listing items of the games that meet the given query (see [GameCollectionDao.observeItems]).
 * @param platformIds The identifiers of the versions of the query's platform, or null if any.
 */
private fun GameCollectionDao.observeItems(
    query: CollectionQuery,
    platformIds: List<Long>?,
    limit: Int
): Flow<List<CollectionItemRow>> = observeItems(
    state = query.state?.name,
    platinumOnly = query.platinumOnly,
    platformIds = platformIds,
    nameContains = query.nameContains?.value,
    initial = query.initial,
    limit = limit
)

/**
 * The identifiers of the versions of this platform among the given ones (see
 * [Platform.isSameGamePlatformAs]), including its own. Games may be registered as owned on any of
 * them.
 * @throws IllegalArgumentException if the stored platforms do not meet the domain requirements.
 */
private fun Platform.versionIdsAmong(platforms: List<PlatformEntity>): List<Long> =
    (platforms.map { it.toPlatform() }.filter { it.isSameGamePlatformAs(this) }.map { it.id } + id)
        .distinct()

/**
 * The first day of this year.
 */
private fun Year.firstDay(): LocalDate = LocalDate(value, 1, 1)

/**
 * The last day of this year.
 */
private fun Year.lastDay(): LocalDate = LocalDate(value, 12, 31)
