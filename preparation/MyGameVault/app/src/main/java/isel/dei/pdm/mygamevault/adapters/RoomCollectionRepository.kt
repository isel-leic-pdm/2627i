package isel.dei.pdm.mygamevault.adapters

import android.util.Log
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.adapters.db.GameDao
import isel.dei.pdm.mygamevault.adapters.db.toCollectionEntry
import isel.dei.pdm.mygamevault.adapters.db.toEntity
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.UnexpectedPersistenceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-based implementation of the [CollectionRepository].
 */
internal class RoomCollectionRepository(
    private val gameDao: GameDao
) : CollectionRepository {

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
            throw UnexpectedPersistenceException("Error saving entry", e)
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
            throw UnexpectedPersistenceException("Error deleting entry", e)
        }
    }

    override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? {
        Log.d(TAG, "get: gameId = $gameId, platformId = ${platform.id}")
        return try {
            gameDao.getEntry(gameId, platform.id)?.toCollectionEntry().also {
                Log.d(TAG, "get: successfully retrieved ${if (it != null) "entry" else "null"}")
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Log.e(TAG, "get: error occurred", e)
            throw UnexpectedPersistenceException("Error getting entry", e)
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
}
