package isel.dei.pdm.mygamevault.add

import isel.dei.pdm.mygamevault.core.Game

/**
 * Represents the possible states of the Add Game screen.
 * @property results The list of games from the last successful search.
 */
sealed class AddGameScreenState(val results: List<Game>) {
    /**
     * The initial state, or when the search query is empty.
     */
    class Idle(results: List<Game> = emptyList()) : AddGameScreenState(results)

    /**
     * When the user is actively typing in the search box.
     */
    class Typing(results: List<Game>) : AddGameScreenState(results)

    /**
     * When a search is in progress.
     */
    class Searching(results: List<Game>) : AddGameScreenState(results)

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
