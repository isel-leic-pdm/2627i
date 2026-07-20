package isel.dei.pdm.puzzle.ui.play

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.puzzle.domain.Board
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

const val SOLVED_TIMEOUT_MS = 3000L

/**
 * The possible states of the play screen.
 */
sealed interface PlayScreenState {
    data object Idle : PlayScreenState
    data class Solving(val board: Board) : PlayScreenState
    data object Solved : PlayScreenState
}

/**
 * The ViewModel for the play screen.
 * It hosts the screen's state machine and exposes the current state as a mutable state property.
 */
class PlayScreenViewModel(initialState: PlayScreenState = PlayScreenState.Idle) : ViewModel() {

    var state by mutableStateOf(initialState)
        private set

    /**
     * Starts the game with a random board.
     */
    fun start() {
        state = PlayScreenState.Solving(Board.createRandom())
    }

    /**
     * Moves a tile on the board.
     * @param tile the tile to move.
     */
    fun move(tile: Int) {
        val currentState = state
        if (currentState is PlayScreenState.Solving) {
            val nextBoard = currentState.board.move(tile)
            if (nextBoard.isSolved) {
                state = PlayScreenState.Solved
                viewModelScope.launch {
                    delay(SOLVED_TIMEOUT_MS.milliseconds)
                    state = PlayScreenState.Idle
                }
            } else {
                state = PlayScreenState.Solving(nextBoard)
            }
        }
    }

    /**
     * Resets the game to the idle state.
     */
    fun reset() {
        state = PlayScreenState.Idle
    }

    companion object {
        /**
         * The factory for the PlayScreenViewModel.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayScreenViewModel()
            }
        }
    }
}
