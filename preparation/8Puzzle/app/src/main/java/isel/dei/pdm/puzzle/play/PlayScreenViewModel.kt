package isel.dei.pdm.puzzle.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.domain.learningRealTimeAStar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

internal const val SOLVED_TIMEOUT_MS = 3000L
internal const val AUTO_SOLVE_DELAY_MS = 500L

/**
 * The possible states of the play screen.
 */
internal sealed interface PlayScreenState {
    data object Idle : PlayScreenState
    data class Solving(val board: Board) : PlayScreenState
    data class AutoSolving(val board: Board) : PlayScreenState
    data object Solved : PlayScreenState
}

/**
 * The ViewModel for the play screen.
 * It hosts the screen's state machine and exposes the current state as a mutable state property.
 */
internal class PlayScreenViewModel(initialState: PlayScreenState = PlayScreenState.Idle) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<PlayScreenState> = _state.asStateFlow()

    private var autoSolveJob: Job? = null

    /**
     * Starts the game with a random board.
     */
    fun start() {
        _state.value = PlayScreenState.Solving(Board.createRandom())
    }

    /**
     * Moves a tile on the board.
     * @param tile the tile to move.
     */
    fun move(tile: Int) {
        _state.update { currentState ->
            if (currentState is PlayScreenState.Solving) {
                PlayScreenState.Solving(currentState.board.move(tile))
            } else {
                currentState
            }
        }
    }

    /**
     * Starts the automatic solver.
     */
    fun autoSolve() {
        val currentState = _state.value
        if (currentState is PlayScreenState.Solving) {
            autoSolveJob = viewModelScope.launch {
                learningRealTimeAStar(currentState.board)
                    .onEach { delay(AUTO_SOLVE_DELAY_MS.milliseconds) }
                    .collect { board ->
                        _state.value = PlayScreenState.AutoSolving(board)
                        if (board.isSolved) {
                            onAnimationFinished()
                        }
                    }
            }
        }
    }

    /**
     * Stops the automatic solver.
     */
    fun stopAutoSolve() {
        val currentState = _state.value
        if (currentState is PlayScreenState.AutoSolving) {
            autoSolveJob?.cancel()
            autoSolveJob = null
            _state.value = PlayScreenState.Solving(currentState.board)
        }
    }

    /**
     * Called when the animation for the last move has finished.
     * If the board is solved, it transitions to the Solved state.
     */
    fun onAnimationFinished() {
        val isSolved = when (val currentState = _state.value) {
            is PlayScreenState.Solving -> currentState.board.isSolved
            is PlayScreenState.AutoSolving -> currentState.board.isSolved
            else -> false
        }

        if (isSolved) {
            autoSolveJob?.cancel()
            autoSolveJob = null
            viewModelScope.launch {
                _state.value = PlayScreenState.Solved
                delay(SOLVED_TIMEOUT_MS.milliseconds)
                _state.value = PlayScreenState.Idle
            }
        }
    }

    /**
     * Resets the game to the idle state.
     */
    fun reset() {
        autoSolveJob?.cancel()
        autoSolveJob = null
        _state.value = PlayScreenState.Idle
    }
}
