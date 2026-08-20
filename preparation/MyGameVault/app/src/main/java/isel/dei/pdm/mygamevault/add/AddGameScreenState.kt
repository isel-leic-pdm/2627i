package isel.dei.pdm.mygamevault.add

import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.SearchServiceException

/**
 * Represents the possible states of the Add Game screen.
 * @property results The list of games from the last successful search.
 */
sealed class AddGameScreenState(val results: List<Game>) {
    /**
     * The initial state, or when a search has finished and no typing is occurring.
     * @property sourceQuery The query that produced these results, or null if no search was issued.
     */
    class Idle(val sourceQuery: String? = null, results: List<Game> = emptyList()) : AddGameScreenState(results)

    /**
     * When the user is actively typing in the search box.
     */
    class Typing(results: List<Game>) : AddGameScreenState(results)

    /**
     * When a search is in progress.
     */
    class Searching(results: List<Game>) : AddGameScreenState(results)

    /**
     * When a search has failed.
     * @property error The exception that caused the failure.
     * @property previousResults The results that were being displayed before the error occurred
     */
    class Error(val error: SearchServiceException, val previousResults: List<Game>)
        : AddGameScreenState(previousResults)

    /**
     * Whether the software keyboard should be visible.
     */
    val isKeyboardVisible: Boolean
        get() = this is Typing || this is Searching

    /**
     * Whether clicks on list items should be detected.
     */
    val isClickDetectionEnabled: Boolean
        get() = this is Idle
}
