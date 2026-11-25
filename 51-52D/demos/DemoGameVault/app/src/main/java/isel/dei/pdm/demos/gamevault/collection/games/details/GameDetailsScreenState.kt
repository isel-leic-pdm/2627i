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
     * When the screen is to be closed, as there is nothing left to do in it. It is final.
     * @property reason Why the screen is to be closed.
     */
    data class Closed(val reason: Reason) : GameDetailsScreenState() {
        enum class Reason {
            /**
             * The game is not in the collection (e.g. it was just removed from it).
             */
            GAME_NOT_FOUND,

            /**
             * The user left the screen, once its play session was stopped.
             */
            LEFT
        }
    }

    /**
     * When the game could not be read.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : GameDetailsScreenState()
}

/**
 * The failure of an action on the game, to be shown to the user.
 * @property action The action that failed.
 * @property error The error that occurred.
 */
data class GameActionFailure(val action: Action, val error: PersistenceException) {
    enum class Action { START_SESSION, STOP_SESSION }
}
