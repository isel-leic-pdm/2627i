package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.LimitedResult
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
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Year
import java.util.Locale
import kotlin.time.Instant

/**
 * A fake, in-memory implementation of the [GameCollectionRepository] interface.
 * We use it to have fine control on the repository behavior while developing the app. It follows
 * the contract, including its rules (one entry per game and one active play session overall), so
 * that it can stand in for the actual implementation.
 *
 * It is meant to be used only during development and in tests, confined to the main thread. It is
 * therefore not thread safe: each operation is atomic because it runs without suspending. The
 * collection is published through a state flow, so that all observable queries are updated after
 * each change.
 *
 * @param initialEntries The entries the collection starts with (e.g. for previews or tests), with
 * no details. Their games' identifiers must be distinct, and at most one may have an active
 * session.
 */
class FakeGameCollectionRepository(
    initialEntries: List<CollectionEntry> = emptyList()
) : GameCollectionRepository {

    /**
     * A game stored in the collection: its entry (which holds the game) and its details. The
     * details are stored without the game, so that they always refer to its current version.
     */
    private data class StoredGame(val entry: CollectionEntry, val details: NewGameDetails)

    /**
     * The collection, indexed by game identifier.
     */
    private val collection = MutableStateFlow(
        initialEntries.associate { it.game.id to StoredGame(it, NewGameDetails()) }
    )

    private var nextId = (initialEntries.maxOfOrNull { it.game.id } ?: 0L) + 1L

    init {
        require(collection.value.size == initialEntries.size) {
            "The initial entries' games must have distinct identifiers"
        }
        require(initialEntries.count { it.isSessionActive } <= 1) {
            "At most one initial entry may have an active session"
        }
    }

    // Adding games

    override suspend fun findSameEntry(game: NewGame): CollectionEntry? =
        collection.value.values.firstOrNull { game isSameGameAs it.entry.game }?.entry

    override suspend fun add(
        game: NewGame,
        platform: Platform,
        playStatus: PlayStatus,
        details: NewGameDetails
    ): AddGameResult {
        val existing = collection.value.values.firstOrNull { game isSameGameAs it.entry.game }
        if (existing != null) {
            val entry = existing.entry
            if (entry.isOwnedOn(platform)) {
                return AddGameResult.AlreadyOwnedOnPlatform(entry.game)
            }
            collection.value += entry.game.id to existing.copy(entry = entry.addPlatform(platform))
            return AddGameResult.PlatformAdded(entry.game)
        }
        val added = Game(
            id = nextId++,
            name = game.name,
            releaseDate = game.releaseDate,
            coverUri = game.coverUri,
            thumbnailUri = game.thumbnailUri,
            source = game.source
        )
        val entry = CollectionEntry(game = added, platforms = setOf(platform), playStatus = playStatus)
        collection.value += added.id to StoredGame(entry, details)
        return AddGameResult.Added(added.id)
    }

    // Observing the collection

    override fun observeItems(
        query: CollectionQuery,
        limit: Int
    ): Flow<LimitedResult<CollectionItem>> {
        require(limit > 0) { "The limit must be positive" }
        return collection.map { stored ->
            val matching = stored.values
                .map { it.entry }
                .filter { it.meets(query) }
                .sortedWith(
                    compareByDescending<CollectionEntry> { it.isSessionActive }
                        .thenBy { it.game.name.value.lowercase(Locale.ROOT) }
                )
            LimitedResult(
                items = matching.take(limit).map { it.toItem() },
                hasMore = matching.size > limit
            )
        }
    }

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> =
        collection.map { it[gameId]?.entry }

    override fun observeDetails(gameId: Long): Flow<GameDetails?> =
        collection.map { stored -> stored[gameId]?.let { it.details.toGameDetails(it.entry.game) } }

    override fun observeActiveSession(): Flow<CollectionEntry?> =
        collection.map { stored -> stored.values.firstOrNull { it.entry.isSessionActive }?.entry }

    override fun observeSummary(year: Year): Flow<CollectionSummary> =
        collection.map { stored -> summaryOf(stored.values.map { it.entry.playStatus }, year) }

    // Changing the collection

    override suspend fun updateEntry(
        gameId: Long,
        change: (CollectionEntry) -> CollectionEntry
    ): CollectionEntry? {
        val stored = collection.value[gameId] ?: return null
        // If the change throws, nothing is stored
        val updated = change(stored.entry)
        require(updated.game.id == gameId) { "The change cannot replace the entry's game" }
        check(!updated.isSessionActive || stored.entry.isSessionActive) {
            "Sessions must be started through startSession"
        }
        collection.value += gameId to stored.copy(entry = updated)
        return updated
    }

    override suspend fun startSession(gameId: Long, now: Instant): StartSessionResult {
        val stored = collection.value[gameId] ?: return StartSessionResult.NotInCollection
        val active = collection.value.values.firstOrNull { it.entry.isSessionActive }
        if (active != null) {
            return StartSessionResult.AnotherSessionActive(active.entry)
        }
        val updated = stored.entry.startSession(now)
        collection.value += gameId to stored.copy(entry = updated)
        return StartSessionResult.Started(updated)
    }

    override suspend fun updateGame(game: Game): Boolean {
        val stored = collection.value[game.id] ?: return false
        collection.value += game.id to stored.copy(entry = stored.entry.copy(game = game))
        return true
    }

    /**
     * Updates the details of the given game. Only the details are updated: the game they hold is
     * only used to identify it (see [updateGame]).
     */
    override suspend fun updateDetails(details: GameDetails): Boolean {
        val gameId = details.game.id
        val stored = collection.value[gameId] ?: return false
        val newDetails = NewGameDetails(
            description = details.description,
            developers = details.developers,
            publishers = details.publishers,
            genres = details.genres,
            notes = details.notes
        )
        collection.value += gameId to stored.copy(details = newDetails)
        return true
    }

    override suspend fun remove(gameId: Long): Boolean {
        if (gameId !in collection.value) return false
        collection.value -= gameId
        return true
    }
}

/**
 * Checks whether this entry meets all the criteria of the given query.
 */
private fun CollectionEntry.meets(query: CollectionQuery): Boolean =
    (query.state == null || playStatus.state == query.state) &&
        (!query.platinumOnly || playStatus.platinum != null) &&
        (query.platformId == null || platforms.any { it.id == query.platformId }) &&
        (query.nameContains?.let { game.name.value.contains(it.value, ignoreCase = true) } ?: true) &&
        (query.initial == null || game.name.value.initial() == query.initial)

/**
 * The initial under which this name is listed, for alphabetical navigation.
 * DRAFT: Accents and leading articles are not taken into account yet, as the rules are still to
 * be decided (see [CollectionQuery.initial]).
 */
private fun String.initial(): Char {
    val first = trimStart().first().uppercaseChar()
    return if (first in 'A'..'Z') first else CollectionQuery.OTHER_INITIAL
}

/**
 * Computes the summary of the given play statuses, following the domain rules (see
 * [CollectionSummary]).
 */
private fun summaryOf(statuses: Collection<PlayStatus>, year: Year) = CollectionSummary(
    year = year,
    finishedInYear = statuses.count { it.wasFirstFinishedIn(year) },
    replayedInYear = statuses.count { it.wasReplayedIn(year) },
    platinumInYear = statuses.count { it.obtainedPlatinumIn(year) },
    owned = statuses.size,
    played = statuses.count { it.wasPlayed() },
    finished = statuses.count { it.isFinished },
    platinum = statuses.count { it.platinum != null }
)

/**
 * Whether the game was started at some point (see [CollectionSummary.played]).
 */
private fun PlayStatus.wasPlayed(): Boolean =
    (state != PlayStatus.State.NONE && state != PlayStatus.State.BACKLOG) ||
        timeSpent.hours > 0 || timeSpent.minutes > 0 ||
        isFinished || platinum != null
