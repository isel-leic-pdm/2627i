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
     * Gets a flow that emits the entry with an active play session, if any.
     * @return The flow of the active session entry, or null if no session is active.
     */
    fun getActiveSession(): Flow<CollectionEntry?>

    /**
     * Starts a play session for the given game and platform.
     * If a session is already active, it will be replaced.
     * @param gameId The ID of the game to start the session for.
     * @param platformId The ID of the platform the game is played on.
     */
    suspend fun startSession(gameId: Long, platformId: Long)

    /**
     * Stops the current active play session.
     */
    suspend fun stopSession()

    /**
     * Gets a flow that emits the list of entries currently being played.
     * @return The flow of entry lists.
     */
    fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> =
        searchByStates(states = setOf(PlayStatus.State.PLAYING))

    /**
     * Gets the [limit] most recently added entries in the collection.
     * The results are limited to at most 100 entries.
     *
     * @param limit The maximum number of entries to return. Defaults to 20.
     * @return A flow that emits the list of matching entries.
     */
    fun getLatest(limit: Int = 20): Flow<List<CollectionEntry>>

    /**
     * Searches for entries where the game name contains [partialName].
     * The results are limited to at most 100 entries.
     *
     * @param partialName The partial name of the game to search for.
     * @param orderBy The criteria to order the results by. Defaults to NAME.
     * @param limit The maximum number of entries to return. Defaults to 20.
     * @return A flow that emits the list of matching entries.
     */
    fun searchByName(
        partialName: String,
        orderBy: OrderBy = OrderBy.NAME,
        limit: Int = 20
    ): Flow<List<CollectionEntry>>

    /**
     * Searches for entries belonging to any of the given [platforms].
     * The results are limited to at most 100 entries.
     *
     * @param platforms The set of platforms to search for.
     * @param orderBy The criteria to order the results by. Defaults to ADDED_AT.
     * @param limit The maximum number of entries to return. Defaults to 20.
     * @return A flow that emits the list of matching entries.
     */
    fun searchByPlatforms(
        platforms: Set<Platform>,
        orderBy: OrderBy = OrderBy.ADDED_AT,
        limit: Int = 20
    ): Flow<List<CollectionEntry>>

    /**
     * Searches for entries in any of the given playing [states].
     * The results are limited to at most 100 entries.
     *
     * @param states The set of playing statuses to search for.
     * @param orderBy The criteria to order the results by. Defaults to ADDED_AT.
     * @param limit The maximum number of entries to return. Defaults to 20.
     * @return A flow that emits the list of matching entries.
     */
    fun searchByStates(
        states: Set<PlayStatus.State>,
        orderBy: OrderBy = OrderBy.ADDED_AT,
        limit: Int = 20
    ): Flow<List<CollectionEntry>>
}
