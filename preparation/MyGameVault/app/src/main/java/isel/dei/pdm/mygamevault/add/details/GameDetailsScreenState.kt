package isel.dei.pdm.mygamevault.add.details

import isel.dei.pdm.mygamevault.domain.GameDetails

/**
 * Represents the state of the Game Details screen.
 */
sealed interface GameDetailsScreenState {
    /**
     * Data is being loaded.
     */
    data object Loading : GameDetailsScreenState

    /**
     * Data has been successfully loaded.
     */
    data class Loaded(val details: GameDetails) : GameDetailsScreenState

    /**
     * An error occurred while loading the data.
     */
    data class Error(val error: Throwable) : GameDetailsScreenState
}
