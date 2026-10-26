package isel.dei.pdm.demos.gamevault.domain.collection

import kotlinx.coroutines.flow.Flow

/**
 * Contract to be supported by repositories that can be used to store the user's collection of platforms.
 */
interface PlatformCollectionRepository {

    /**
     * Adds a new platform to the user's collection.
     * @param platform The platform to be added.
     * @return A result containing the unique identifier of the newly added platform, or an error
     * if the operation failed.
     */
    suspend fun add(platform: NewPlatform): Result<Long>

    /**
     * Gets the platform with the given ID.
     * @param id The ID of the platform to get.
     * @return A result containing the platform, or null if not found.
     */
    suspend fun getById(id: Long): Result<Platform?>

    /**
     * Gets the list of all platforms in the user's collection. The size of the list is not
     * large enough to warrant pagination.
     * @return A result containing the list of platforms, or an error if the operation failed.
     */
    suspend fun getAll(): Result<List<Platform>>

    /**
     * The list of platforms in the user's collection (as an observable query).
     */
    val platforms: Flow<List<Platform>>
}