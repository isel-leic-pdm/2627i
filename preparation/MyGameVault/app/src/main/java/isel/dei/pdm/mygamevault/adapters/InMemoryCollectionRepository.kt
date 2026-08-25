package isel.dei.pdm.mygamevault.adapters

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

    private val entries = MutableStateFlow<Map<EntryKey, CollectionEntry>>(emptyMap())

    private data class EntryKey(val gameId: Long, val platformAbbreviation: String)

    override suspend fun save(entry: CollectionEntry) {
        val key = EntryKey(entry.game.id, entry.platform.abbreviation())
        val current = entries.first()
        entries.emit(current + (key to entry))
    }

    override suspend fun delete(gameId: Long, platform: Platform) {
        val key = EntryKey(gameId, platform.abbreviation())
        val current = entries.first()
        entries.emit(current - key)
    }

    override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? {
        val key = EntryKey(gameId, platform.abbreviation())
        return entries.first()[key]
    }

    override fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> =
        entries.map { it.values.filter { entry -> entry.playStatus.state == PlayStatus.State.PLAYING } }

    override fun search(
        partialName: String?,
        platform: Platform?,
        state: PlayStatus.State?
    ): Flow<List<CollectionEntry>> = entries.map {
        it.values.filter { entry ->
            val nameMatch = partialName == null || 
                entry.game.name.contains(partialName, ignoreCase = true)
            val platformMatch = platform == null || entry.platform == platform
            val stateMatch = state == null || entry.playStatus.state == state
            nameMatch && platformMatch && stateMatch
        }
    }
}
