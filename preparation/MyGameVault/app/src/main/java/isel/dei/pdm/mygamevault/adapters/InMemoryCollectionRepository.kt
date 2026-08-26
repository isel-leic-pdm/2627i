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

    override fun search(
        partialName: String?,
        platforms: Set<Platform>,
        states: Set<PlayStatus.State>,
        orderBy: CollectionRepository.OrderBy
    ): Flow<List<CollectionEntry>> = entries.map { entry ->
        entry.values.filter {
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
        )
    }
}
