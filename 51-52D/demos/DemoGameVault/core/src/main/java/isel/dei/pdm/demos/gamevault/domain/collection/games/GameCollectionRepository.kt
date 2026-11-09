package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.LimitedResult
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.flow.Flow
import java.time.Year
import kotlin.time.Instant

/**
 * Contract to be supported by repositories that store the user's collection of games.
 *
 * DRAFT: The contract is tailored to the app's use cases (one operation per use case, each one
 * implementable as a fixed query), instead of offering general purpose search operations.
 *
 * The collection has at most one entry per game, so entries are identified by their game's
 * identifier. It also has at most one active play session overall. Implementations must enforce
 * both rules, as they involve several entries.
 *
 * Listings are made of lightweight [CollectionItem]s, while the complete [CollectionEntry] is only
 * provided when working with a single game. Instead of being paginated, listings are limited:
 * the user narrows them down through the criteria of a [CollectionQuery], and is advised to
 * refine the query when the limit is exceeded. A collection may span a few thousand games.
 *
 * All operations report failures uniformly, by throwing a [PersistenceException]: the flows when
 * collected, and the suspending functions when called. Implementations must translate the errors
 * of the underlying storage technology into [PersistenceException]s. Expected outcomes, such as
 * the game being already in the collection, are reported through the operations' results.
 */
interface GameCollectionRepository {

    // Adding games

    /**
     * Finds the game in the collection that is the same as the given one (see
     * [NewGame.isSameGameAs]). Meant, for example, to signal search results that are already in
     * the collection.
     * @param game The game to look for.
     * @return The game in the collection, or null if not found.
     * @throws PersistenceException If the collection cannot be read.
     */
    suspend fun findSameGame(game: NewGame): Game?

    /**
     * Adds the given game to the collection, with its details, owned on the given platform. Both
     * are stored atomically.
     * @param game The game to add.
     * @param platform The platform the game is owned on.
     * @param playStatus The game's initial play status (e.g. BACKLOG, or the progress obtained
     * from an import).
     * @param details The game's details.
     * @return [AddGameResult.Added], or [AddGameResult.AlreadyInCollection] if the same game is
     * already in the collection (see [NewGame.isSameGameAs]), in which case nothing is added.
     * @throws PersistenceException If the game cannot be added.
     */
    suspend fun add(
        game: NewGame,
        platform: Platform,
        playStatus: PlayStatus = PlayStatus(),
        details: NewGameDetails = NewGameDetails()
    ): AddGameResult

    // Observing the collection

    /**
     * The items of the entries that meet the given query, as an observable query. They are sorted
     * by game name, ignoring case, except that the one with the active session, if any, comes
     * first (e.g. in the carousel of games being played).
     * @param query The criteria the entries must meet. By default, all entries are included.
     * @param limit The maximum number of items. Must be positive.
     * @return The items, at most [limit] of them, and whether more entries meet the query.
     */
    fun observeItems(
        query: CollectionQuery = CollectionQuery(),
        limit: Int = DEFAULT_LIMIT
    ): Flow<LimitedResult<CollectionItem>>

    /**
     * The entry of the given game, or null if the game is not in the collection (e.g. if it was
     * removed), as an observable query.
     * @param gameId The game's identifier.
     */
    fun observeEntry(gameId: Long): Flow<CollectionEntry?>

    /**
     * The details of the given game, or null if the game is not in the collection, as an
     * observable query.
     * @param gameId The game's identifier.
     */
    fun observeDetails(gameId: Long): Flow<GameDetails?>

    /**
     * The entry with the active play session, or null if there is none, as an observable query.
     * Meant, for example, to restore the session's timer when the app is restarted.
     */
    fun observeActiveSession(): Flow<CollectionEntry?>

    /**
     * The summary of the collection's accomplishments, for the given year and all time, as an
     * observable query.
     * @param year The year of the yearly figures (e.g. the current year).
     */
    fun observeSummary(year: Year): Flow<CollectionSummary>

    // Changing the collection

    /**
     * Applies the given change to the entry of the given game, and stores the result, atomically.
     * The change is a domain operation on the entry (e.g. `{ it.addRun(today) }`), so the domain
     * rules are enforced by [CollectionEntry] itself. To start a session, use [startSession]
     * instead, as it requires checking the remaining entries.
     * @param gameId The game's identifier.
     * @param change The change to apply.
     * @return The updated entry, or null if the game is not in the collection.
     * @throws PersistenceException If the entry cannot be read or stored.
     * Exceptions thrown by [change] (i.e. the domain rules rejecting it) are propagated, and
     * nothing is stored.
     */
    suspend fun updateEntry(gameId: Long, change: (CollectionEntry) -> CollectionEntry): CollectionEntry?

    /**
     * Starts a play session of the given game, unless another session is active, atomically.
     * See [CollectionEntry.startSession].
     * @param gameId The game's identifier.
     * @param now The current time.
     * @return The outcome of the operation.
     * @throws PersistenceException If the collection cannot be read or stored.
     */
    suspend fun startSession(gameId: Long, now: Instant): StartSessionResult

    /**
     * Updates the given game's information (e.g. its name, cover or source reference).
     * @param game The updated game, identified by its identifier.
     * @return Whether the game was updated (false if it is not in the collection).
     * @throws PersistenceException If the game cannot be stored.
     */
    suspend fun updateGame(game: Game): Boolean

    /**
     * Updates the details of the given game (e.g. after the user edited their notes).
     * @param details The updated details, identified by their game's identifier.
     * @return Whether the details were updated (false if the game is not in the collection).
     * @throws PersistenceException If the details cannot be stored.
     */
    suspend fun updateDetails(details: GameDetails): Boolean

    /**
     * Removes the given game from the collection, with its entry and details.
     * @param gameId The game's identifier.
     * @return Whether the game was removed (false if it is not in the collection).
     * @throws PersistenceException If the game cannot be removed.
     */
    suspend fun remove(gameId: Long): Boolean

    companion object {
        /**
         * The default maximum number of items in listings. It is large enough not to be noticed
         * when browsing narrowed down listings, and small enough for listings to remain light.
         */
        const val DEFAULT_LIMIT = 200
    }
}

/**
 * The outcome of adding a game to the collection.
 */
sealed interface AddGameResult {
    /**
     * The game was added, with the given identifier.
     */
    data class Added(val gameId: Long) : AddGameResult

    /**
     * The game was not added, because the same game is already in the collection.
     */
    data class AlreadyInCollection(val existing: Game) : AddGameResult
}

/**
 * The outcome of starting a play session.
 */
sealed interface StartSessionResult {
    /**
     * The session was started.
     */
    data class Started(val entry: CollectionEntry) : StartSessionResult

    /**
     * The session was not started, because a session of the given entry is already active.
     */
    data class AnotherSessionActive(val active: CollectionEntry) : StartSessionResult

    /**
     * The session was not started, because the game is not in the collection.
     */
    data object NotInCollection : StartSessionResult
}
