package isel.dei.pdm.mygamevault.adapters

import android.util.Log
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * In-memory implementation of the [CollectionRepository].
 * This implementation does not persist data across application restarts.
 */
class InMemoryCollectionRepository : CollectionRepository {

    private companion object {
        val TAG = MyGameVaultApplication.buildTag("InMemoryCollectionRepository")
        private const val MAX_LIMIT = 100
    }

    private val entries = MutableStateFlow<Map<EntryKey, CollectionEntry>>(emptyMap())

    private data class EntryKey(val gameId: Long, val platformAbbreviation: String)

    override suspend fun save(entry: CollectionEntry) {
        Log.d(TAG, "save: entry = ${entry.game.id} on platform ${entry.platform.id}")
        val key = EntryKey(entry.game.id, entry.platform.abbreviation())
        val current = entries.first()
        entries.emit(current + (key to entry))
        Log.d(TAG, "save: successfully saved entry")
    }

    override suspend fun delete(gameId: Long, platform: Platform) {
        Log.d(TAG, "delete: gameId = $gameId, platformId = ${platform.id}")
        val key = EntryKey(gameId, platform.abbreviation())
        val current = entries.first()
        entries.emit(current - key)
        Log.d(TAG, "delete: successfully deleted entry")
    }

    override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? {
        Log.d(TAG, "get: gameId = $gameId, platformId = ${platform.id}")
        val key = EntryKey(gameId, platform.abbreviation())
        return entries.first()[key].also {
            Log.d(TAG, "get: successfully retrieved ${if (it != null) "entry" else "null"}")
        }
    }

    override fun getLatest(limit: Int): Flow<List<CollectionEntry>> {
        Log.d(TAG, "getLatest: limit = $limit")
        return searchInternal(null, emptySet(), emptySet(), CollectionRepository.OrderBy.ADDED_AT, limit)
    }

    override fun searchByName(
        partialName: String,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByName: partialName = \"$partialName\", orderBy = $orderBy, limit = $limit")
        return searchInternal(partialName, emptySet(), emptySet(), orderBy, limit)
    }

    override fun searchByPlatforms(
        platforms: Set<Platform>,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByPlatforms: platforms = ${platforms.map { it.id }}, orderBy = $orderBy, limit = $limit")
        return searchInternal(null, platforms, emptySet(), orderBy, limit)
    }

    override fun searchByStates(
        states: Set<PlayStatus.State>,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> {
        Log.d(TAG, "searchByStates: states = $states, orderBy = $orderBy, limit = $limit")
        return searchInternal(null, emptySet(), states, orderBy, limit)
    }

    private fun searchInternal(
        partialName: String?,
        platforms: Set<Platform>,
        states: Set<PlayStatus.State>,
        orderBy: CollectionRepository.OrderBy,
        limit: Int
    ): Flow<List<CollectionEntry>> = entries.map { entry ->
        val cappedLimit = limit.coerceAtMost(MAX_LIMIT)
        entry.values.asSequence().filter {
            val nameMatch = partialName == null || 
                it.game.name.contains(partialName, ignoreCase = true)
            val platformMatch = platforms.isEmpty() || it.platform in platforms
            val stateMatch = states.isEmpty() || it.playStatus.state in states
            nameMatch && platformMatch && stateMatch
        }.sortedWith(
            comparator = compareBy<CollectionEntry> { it.platform.name() }
                .thenBy { it.playStatus.state.ordinal }
                .thenBy {
                    when (orderBy) {
                        CollectionRepository.OrderBy.NAME -> it.game.name()
                        CollectionRepository.OrderBy.RELEASE_DATE -> it.game.releaseDate?.toEpochDay() ?: 0L
                        CollectionRepository.OrderBy.ADDED_AT -> it.addedAt.toEpochDay()
                    }
                }
        ).take(cappedLimit).toList()
    }
}
