package isel.dei.pdm.demos.gamevault.domain.collection

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import kotlinx.coroutines.flow.Flow

/**
 * Contract to be supported by repositories that can be used to store the user's collection of platforms.
 *
 * All operations report failures uniformly, by throwing a [PersistenceException]: the flows when
 * collected, and the suspending functions when called. Implementations must translate the errors
 * of the underlying storage technology into [PersistenceException]s.
 */
interface PlatformCollectionRepository {

    /**
     * Adds a new platform to the user's collection.
     * @param platform The platform to be added.
     * @return The unique identifier of the newly added platform.
     * @throws PersistenceException If the platform cannot be added.
     */
    suspend fun add(platform: NewPlatform): Long

    /**
     * Gets the platform with the given ID.
     * @param id The ID of the platform to get.
     * @return The platform, or null if not found.
     * @throws PersistenceException If the platform cannot be read.
     */
    suspend fun getById(id: Long): Platform?

    /**
     * Gets the list of all platforms in the user's collection. The size of the list is not
     * large enough to warrant pagination.
     * @return The list of platforms.
     * @throws PersistenceException If the platforms cannot be read.
     */
    suspend fun getAll(): List<Platform>

    /**
     * The list of platforms in the user's collection (as an observable query).
     * Collecting the flow throws a [PersistenceException] if the platforms cannot be read.
     */
    val platforms: Flow<List<Platform>>
}
