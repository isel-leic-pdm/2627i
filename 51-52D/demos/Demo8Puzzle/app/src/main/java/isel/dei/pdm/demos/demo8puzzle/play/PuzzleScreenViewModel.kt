package isel.dei.pdm.demos.demo8puzzle.play

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.demos.demo8puzzle.core.Puzzle
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.core.isSolved
import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

const val SOLVED_TIMEOUT_MS = 3000L

/**
 * The set of states that the puzzle screen can be in.
 */
sealed class PuzzleScreenState {
    abstract val puzzle: Puzzle
    data class Idle(override val puzzle: Puzzle) : PuzzleScreenState()
    data class Solving(override val puzzle: Puzzle) : PuzzleScreenState()
    data class Solved(override val puzzle: Puzzle) : PuzzleScreenState()
}

/**
 * The view model for the puzzle screen. It holds the screen's state and provides methods
 * to produce the admissible state transitions. The state machine is depicted in the file
 * puzzle-screen-state-machine.png of the documentation folder.
 */
class PuzzleScreenViewModel(
    private val solvedTimeoutMs: Long = SOLVED_TIMEOUT_MS
) : ViewModel() {

    private val logTag = buildLLogTag(this::class.java.simpleName)
    private var idleTransitionJob: Job? = null

    init {
        Log.v(logTag, "init on ${hashCode()}")
    }

    /**
     * The current state of the screen's state machine.
     */
    var state by mutableStateOf<PuzzleScreenState>(
        PuzzleScreenState.Idle(solvedPuzzle)
    )
        private set

    /**
     * Moves the given tile to the empty space. If the move solves the puzzle, a transition to the
     * Solved state occurs. The transition to the Idle state occurs automatically after a timeout.
     * The move is only performed if the current state is Solving. Otherwise, the call is ignored.
     */
    fun moveTile(tile: Tile) {
        val current = state
        if (current is PuzzleScreenState.Solving) {
            Log.v(logTag, "moving tile $tile")
            val newPuzzle = current.puzzle.move(tile)
            state = if (newPuzzle.isSolved()) {
                Log.v(logTag, "Transitioning to Solved")
                PuzzleScreenState.Solved(newPuzzle).also { scheduleTransitionToIdle() }
            } else {
                PuzzleScreenState.Solving(newPuzzle)
            }
        }
    }

    /**
     * Starts a new game. If the current state is Idle, a transition to the Solving state occurs.
     * Otherwise, the call is ignored.
     */
    fun start() {
        val current = state
        if (current is PuzzleScreenState.Idle) {
            Log.v(logTag, "Transitioning to Solving")
            state = PuzzleScreenState.Solving(current.puzzle.shuffle())
        }
    }

    /**
     * Resets the puzzle to its initial state. If the current state is Solving, a transition to
     * the Idle state occurs. Otherwise, the call is ignored.
     */
    fun reset() {
        val current = state
        if (current is PuzzleScreenState.Solving) {
            Log.v(logTag, "Resetting the puzzle")
            idleTransitionJob?.cancel()
            state = PuzzleScreenState.Idle(solvedPuzzle)
        }
    }

    /**
     * Solves the puzzle.
     * For now, this is a fake implementation. We will implement it later.
     */
    fun solve() {
        val current = state
        if (current is PuzzleScreenState.Solving) {
            Log.v(logTag, "Transitioning to Solved")
            state = PuzzleScreenState.Solved(solvedPuzzle).also { scheduleTransitionToIdle() }
        }
    }

    /**
     * Helper function used to schedule the transition to the Idle state after a timeout.
     */
    private fun scheduleTransitionToIdle() {
        idleTransitionJob?.cancel()
        idleTransitionJob = viewModelScope.launch {
            delay(solvedTimeoutMs.milliseconds)
            if (state is PuzzleScreenState.Solved) {
                Log.v(logTag, "Auto-resetting after timeout")
                state = PuzzleScreenState.Idle(solvedPuzzle)
            }
        }
    }

    /**
     * Called when the view model is no longer used and will be destroyed.
     */
    override fun onCleared() {
        Log.v(logTag, "onCleared() on ${hashCode()}")
    }
}
