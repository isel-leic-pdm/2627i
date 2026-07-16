package isel.dei.pdm.puzzle.ui.play

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import isel.dei.pdm.puzzle.domain.Board
import kotlinx.coroutines.delay
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
 * The main gameplay screen for the 8-puzzle.
 */
@Composable
fun PlayScreen() {
    var state by remember { mutableStateOf<PlayScreenState>(PlayScreenState.Idle) }

    LaunchedEffect(state) {
        if (state is PlayScreenState.Solved) {
            delay(SOLVED_TIMEOUT_MS.milliseconds)
            state = PlayScreenState.Idle
        }
    }

    when (val currentState = state) {
        is PlayScreenState.Idle -> IdleView(onStartRequested = { state = PlayScreenState.Solving(Board.createRandom()) })
        is PlayScreenState.Solving -> SolvingView(
            board = currentState.board,
            onMoveRequested = { tile ->
                val nextBoard = currentState.board.move(tile)
                state = if (nextBoard.isSolved) PlayScreenState.Solved else PlayScreenState.Solving(nextBoard)
            },
            onResetRequested = { state = PlayScreenState.Idle }
        )
        is PlayScreenState.Solved -> SolvedView()
    }
}
