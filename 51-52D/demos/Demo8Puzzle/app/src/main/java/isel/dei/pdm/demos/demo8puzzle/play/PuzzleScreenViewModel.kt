package isel.dei.pdm.demos.demo8puzzle.play

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import isel.dei.pdm.demos.demo8puzzle.APP_TAG
import isel.dei.pdm.demos.demo8puzzle.buildLLogTag
import isel.dei.pdm.demos.demo8puzzle.core.Puzzle
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.core.isSolved
import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle

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
 * to produce the admissible state transitions.
 */
class PuzzleScreenViewModel : ViewModel() {

    private val logTag = buildLLogTag(this::class.java.simpleName)

    init {
        Log.v(logTag, "init on ${hashCode()}")
    }

    var state by mutableStateOf<PuzzleScreenState>(
        PuzzleScreenState.Idle(solvedPuzzle)
    )
        private set

    fun moveTile(tile: Tile) {
        val current = state
        if (current is PuzzleScreenState.Solving) {
            val newPuzzle = current.puzzle.move(tile)
            state = if (newPuzzle.isSolved()) {
                PuzzleScreenState.Solved(newPuzzle)
            } else {
                PuzzleScreenState.Solving(newPuzzle)
            }
        }
    }

    fun start() {
        val current = state
        if (current is PuzzleScreenState.Idle) {
            state = PuzzleScreenState.Solving(current.puzzle.shuffle())
        }
    }

    fun reset() {
        val current = state
        if (current is PuzzleScreenState.Solving || current is PuzzleScreenState.Solved) {
            state = PuzzleScreenState.Idle(solvedPuzzle)
        }
    }

    fun solve() {
        val current = state
        if (current is PuzzleScreenState.Solving) {
            state = PuzzleScreenState.Solved(solvedPuzzle)
        }
    }

    override fun onCleared() {
        Log.v(APP_TAG, "PuzzleScreenViewModel.onCleared() on ${hashCode()}")
    }
}
