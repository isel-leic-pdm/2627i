package isel.dei.pdm.mygamevault.ports

import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlinx.coroutines.flow.Flow

/**
 * Interface that represents the repository for managing the game collection.
 */
interface CollectionRepository {
    /**
     * Represents the possible ordering criteria for the collection.
     */
    enum class OrderBy {
        NAME,
        RELEASE_DATE,
        ADDED_AT
    }

    /**
     * Saves an entry to the collection. If an entry already exists for the same game and platform,
     * it will be updated.
     * @param entry The entry to save.
     * @throws PersistenceException If an error occurs during persistence.
     */
    suspend fun save(entry: CollectionEntry)

    /**
     * Deletes an entry from the collection.
     * @param gameId The ID of the game to delete.
     * @param platform The platform of the game to delete.
     * @throws PersistenceException If an error occurs during persistence.
     */
    suspend fun delete(gameId: Long, platform: Platform)

    /**
     * Gets an entry from the collection.
     * @param gameId The ID of the game to get.
     * @param platform The platform of the game to get.
     * @return The entry, or null if it does not exist.
     */
    suspend fun get(gameId: Long, platform: Platform): CollectionEntry?

    /**
     * Gets a flow that emits the list of entries currently being played.
     * @return The flow of entry lists.
     */
    fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> =
        search(states = setOf(PlayStatus.State.PLAYING))

    /**
     * Searches for entries in the collection that match the given criteria.
     * @param partialName The partial name of the game to search for, or null for any.
     * @param platforms The set of platforms to search for. If empty, all platforms are included.
     * @param states The set of playing statuses to search for. If empty, all states are included.
     * @param orderBy The criteria to order the results by within each group (platform/status).
     * @return A flow that emits the list of matching entries.
     */
    fun search(
        partialName: String? = null,
        platforms: Set<Platform> = emptySet(),
        states: Set<PlayStatus.State> = emptySet(),
        orderBy: OrderBy = OrderBy.ADDED_AT
    ): Flow<List<CollectionEntry>>
}
