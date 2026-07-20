package isel.dei.pdm.puzzle.ui.play

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme

/**
 * The main gameplay screen for the 8-puzzle.
 * @param viewModel the ViewModel for the screen.
 */
@Composable
fun PlayScreen(viewModel: PlayScreenViewModel = viewModel(factory = PlayScreenViewModel.Factory)) {
    when (val currentState = viewModel.state) {
        is PlayScreenState.Idle -> IdleView(onStartRequested = viewModel::start)
        is PlayScreenState.Solving -> SolvingView(
            board = currentState.board,
            onMoveRequested = viewModel::move,
            onResetRequested = viewModel::reset
        )
        is PlayScreenState.Solved -> SolvedView()
    }
}

@Preview(showBackground = true)
@Composable
fun PlayScreenPreview() {
    _8PuzzleTheme {
        PlayScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun PlayScreenLandscapePreview() {
    _8PuzzleTheme {
        PlayScreen()
    }
}
