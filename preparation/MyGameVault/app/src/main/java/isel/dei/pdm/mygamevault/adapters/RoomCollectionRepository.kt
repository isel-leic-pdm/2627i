package isel.dei.pdm.mygamevault.adapters

import android.util.Log
import androidx.room.withTransaction
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.adapters.db.ActiveSessionEntity
import isel.dei.pdm.mygamevault.adapters.db.GameDatabase
import isel.dei.pdm.mygamevault.adapters.db.toCollectionEntry
import isel.dei.pdm.mygamevault.adapters.db.toEntity
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.RecoverablePersistenceException
import isel.dei.pdm.mygamevault.ports.UnrecoverablePersistenceException
import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteFullException
import android.database.sqlite.SQLiteTableLockedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds

/**
 * Room-based implementation of the [CollectionRepository].
 */
internal class RoomCollectionRepository(
    private val database: GameDatabase
) : CollectionRepository {

    private val gameDao = database.gameDao()

    private companion object {
        val TAG = MyGameVaultApplication.buildTag("RoomCollectionRepository")
        private const val MAX_LIMIT = 100
    }

    override suspend fun save(entry: CollectionEntry) {
        Log.d(TAG, "save: entry = ${entry.game.id} on platform ${entry.platform.id}")
        try {
            gameDao.saveEntry(
                game = entry.game.toEntity(),
                platform = entry.platform.toEntity(),
                entry = entry.toEntity()
            )
            Log.d(TAG, "save: successfully saved entry")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "save: error occurred", e)
            throw mapToPersistenceException("Error saving entry", e)
        }
    }

    override suspend fun delete(gameId: Long, platform: Platform) {
        Log.d(TAG, "delete: gameId = $gameId, platformId = ${platform.id}")
        try {
            gameDao.deleteEntry(gameId, platform.id)
            Log.d(TAG, "delete: successfully deleted entry")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "delete: error occurred", e)
            throw mapToPersistenceException("Error deleting entry", e)
        }
    }

    override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? {
        Log.d(TAG, "get: gameId = $gameId, platformId = ${platform.id}")
        return try {
            val entryWithDetails = gameDao.getEntry(gameId, platform.id)
            val session = gameDao.getActiveSessionOnce()
            
            entryWithDetails?.toCollectionEntry(
                sessionStartTime = if (session != null && session.gameId == gameId && session.platformId == platform.id) {
                    Instant.fromEpochSeconds(session.startTimeSeconds)
                } else null
            ).also {
                Log.d(TAG, "get: successfully retrieved ${if (it != null) "entry" else "null"}")
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "get: error occurred", e)
            throw mapToPersistenceException("Error getting entry", e)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getActiveSession(): Flow<CollectionEntry?> {
        Log.d(TAG, "getActiveSession observation started")
        return gameDao.observeActiveSession()
            .distinctUntilChanged()
            .flatMapLatest { session ->
                if (session == null) {
                    flowOf(null)
                } else {
                    gameDao.observeEntry(session.gameId, session.platformId).map { entryWithDetails ->
                        entryWithDetails?.toCollectionEntry(
                            sessionStartTime = Instant.fromEpochSeconds(session.startTimeSeconds)
                        )
                    }
                }
            }
            .distinctUntilChanged()
    }

    override suspend fun startSession(gameId: Long, platformId: Long) {
        Log.d(TAG, "startSession: gameId = $gameId, platformId = $platformId")
        try {
            database.withTransaction {
                stopSession()
                gameDao.upsertActiveSession(
                    ActiveSessionEntity(
                        gameId = gameId,
                        platformId = platformId,
                        startTimeSeconds = Clock.System.now().epochSeconds
                    )
                )
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "startSession: error occurred", e)
            throw mapToPersistenceException("Error starting session", e)
        }
    }

    override suspend fun stopSession() {
        Log.d(TAG, "stopSession")
        try {
            database.withTransaction {
                val active = gameDao.getActiveSessionOnce()
                if (active != null) {
                    val duration = (Clock.System.now().epochSeconds - active.startTimeSeconds).seconds
                    Log.d(TAG, "stopSession: found active session, duration = $duration")
                    val entryWithDetails = gameDao.getEntry(active.gameId, active.platformId)
                    if (entryWithDetails != null) {
                        val entry = entryWithDetails.toCollectionEntry().addPlayTime(duration)
                        gameDao.saveEntry(
                            game = entry.game.toEntity(),
                            platform = entry.platform.toEntity(),
                            entry = entry.toEntity()
                        )
                    }
                }
                gameDao.deleteActiveSession()
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "stopSession: error occurred", e)
            throw mapToPersistenceException("Error stopping session", e)
        }
    }

    override fun getLatest(limit: Int): Flow<List<CollectionEntry>> {
        Log.d(TAG, "getLatest: limit = $limit")
        return performSearch(gameDao.searchLatest(limit.coerceAtMost(MAX_LIMIT)), CollectionRepository.OrderBy.ADDED_AT)
    }

    override fun searchByName(
        partialName: String,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByName: partialName = \"$partialName\", orderBy = $orderBy, limit = $limit")
        return performSearch(gameDao.searchByName(partialName, limit.coerceAtMost(MAX_LIMIT)), orderBy)
    }

    override fun searchByPlatforms(
        platforms: Set<Platform>,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByPlatforms: platforms = ${platforms.map { it.id }}, orderBy = $orderBy, limit = $limit")
        return performSearch(gameDao.searchByPlatforms(platforms.map { it.id }.toSet(), limit.coerceAtMost(MAX_LIMIT)), orderBy)
    }

    override fun searchByStates(
        states: Set<PlayStatus.State>,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByStates: states = $states, orderBy = $orderBy, limit = $limit")
        return performSearch(gameDao.searchByStates(states, limit.coerceAtMost(MAX_LIMIT)), orderBy)
    }

    private fun performSearch(
        baseFlow: Flow<List<isel.dei.pdm.mygamevault.adapters.db.CollectionEntryWithDetails>>,
        orderBy: CollectionRepository.OrderBy
    ): Flow<List<CollectionEntry>> = baseFlow.map { list ->
        list.map { it.toCollectionEntry() }
            .sortedWith(
                compareBy<CollectionEntry> { it.platform.name() }
                    .thenBy { it.playStatus.state.ordinal }
                    .thenBy {
                        when (orderBy) {
                            CollectionRepository.OrderBy.NAME -> it.game.name()
                            CollectionRepository.OrderBy.RELEASE_DATE -> it.game.releaseDate?.toEpochDay() ?: 0L
                            CollectionRepository.OrderBy.ADDED_AT -> it.addedAt.toEpochDay()
                        }
                    }
            )
    }

    private fun mapToPersistenceException(message: String, e: Exception) = when (e) {
        is SQLiteDatabaseLockedException,
        is SQLiteTableLockedException,
        is SQLiteFullException,
        is android.database.sqlite.SQLiteOutOfMemoryException ->
            RecoverablePersistenceException(message, e)

        else -> UnrecoverablePersistenceException(message, e)
    }
}
