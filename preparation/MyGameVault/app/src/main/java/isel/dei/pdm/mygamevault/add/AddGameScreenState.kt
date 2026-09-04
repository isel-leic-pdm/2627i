package isel.dei.pdm.mygamevault.add

import androidx.annotation.StringRes
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.ports.SearchServiceException

/**
 * Represents the possible states of the Add Game screen.
 * @property results The list of games from the last successful search.
 * @property selectedPlatform The platform used for the search.
 * @property selectedCategory The category used for the search, or null for all.
 * @property recoverableErrorMsgId An optional resource ID for a transient error.
 */
sealed class AddGameScreenState(
    val results: List<Game>,
    val selectedPlatform: Platform,
    val selectedCategory: Game.Category?,
    @StringRes val recoverableErrorMsgId: Int? = null
) {
    /**
     * The initial state, or when a search has finished and no typing is occurring.
     * @property sourceQuery The query that produced these results, or null if no search was issued.
     */
    class Idle(
        val sourceQuery: String? = null,
        results: List<Game> = emptyList(),
        selectedPlatform: Platform = Platforms.PS5,
        selectedCategory: Game.Category? = null,
        @StringRes recoverableErrorMsgId: Int? = null
    ) : AddGameScreenState(results, selectedPlatform, selectedCategory, recoverableErrorMsgId)

    /**
     * When the user is actively typing in the search box.
     */
    class Typing(
        results: List<Game>,
        selectedPlatform: Platform,
        selectedCategory: Game.Category?,
        @StringRes recoverableErrorMsgId: Int? = null
    ) : AddGameScreenState(results, selectedPlatform, selectedCategory, recoverableErrorMsgId)

    /**
     * When a search is in progress.
     */
    class Searching(
        results: List<Game>,
        selectedPlatform: Platform,
        selectedCategory: Game.Category?,
        @StringRes recoverableErrorMsgId: Int? = null
    ) : AddGameScreenState(results, selectedPlatform, selectedCategory, recoverableErrorMsgId)

    /**
     * When a search has failed due to an unrecoverable error.
     * @property error The exception that caused the failure.
     * @property previousResults The results that were being displayed before the error occurred
     */
    class Error(
        val error: SearchServiceException,
        val previousResults: List<Game>,
        selectedPlatform: Platform,
        selectedCategory: Game.Category?
    ) : AddGameScreenState(previousResults, selectedPlatform, selectedCategory)

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
