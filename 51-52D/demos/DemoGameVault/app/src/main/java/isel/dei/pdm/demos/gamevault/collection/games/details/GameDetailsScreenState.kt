package isel.dei.pdm.demos.gamevault.collection.games.details

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import kotlin.time.Duration

/**
 * Represents the possible states of the game details screen.
 */
sealed class GameDetailsScreenState {

    /**
     * When the game is being read. As its information is known in advance and stored locally, the
     * screen's layout is displayed meanwhile, with placeholders instead of the game's information
     * (see [GameDetailsLoadingView]).
     */
    data object Loading : GameDetailsScreenState()

    /**
     * When the game is displayed.
     *
     * @property entry The game's entry in the collection, with the user's notes.
     * @property sessionElapsedTime The time elapsed since the active play session started, or null
     * if no session is active. While a session is active, it is kept up to date.
     * @property isSessionShort Whether the active play session, if any, is short (see
     * [CollectionEntry.isShortSession]), in which case the user is asked whether to register it
     * when stopping it. Kept up to date along with [sessionElapsedTime].
     */
    data class Displaying(
        val entry: CollectionEntry,
        val sessionElapsedTime: Duration?,
        val isSessionShort: Boolean = false
    ) : GameDetailsScreenState()

    /**
     * When the game is not in the collection (e.g. it was just removed from it).
     */
    data object NotFound : GameDetailsScreenState()

    /**
     * When the game could not be read.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : GameDetailsScreenState()
}
