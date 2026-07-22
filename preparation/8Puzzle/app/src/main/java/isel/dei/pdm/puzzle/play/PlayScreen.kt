package isel.dei.pdm.puzzle.play

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import isel.dei.pdm.puzzle.play.views.IdleView
import isel.dei.pdm.puzzle.play.views.PlayScreenTopBar
import isel.dei.pdm.puzzle.play.views.SolvedView
import isel.dei.pdm.puzzle.play.views.SolvingView
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The main gameplay screen for the 8-puzzle.
 * @param onInfoRequested callback to navigate to the About screen.
 * @param viewModel the ViewModel for the screen.
 */
@Composable
internal fun PlayScreen(
    onInfoRequested: () -> Unit,
    viewModel: PlayScreenViewModel = viewModel(factory = PlayScreenViewModel.Factory)
) {
    Scaffold(
        topBar = { PlayScreenTopBar(onInfoClick = onInfoRequested) }
    ) { innerPadding ->

        val modifier = Modifier.padding(innerPadding)
        when (val currentState = viewModel.state) {
            is PlayScreenState.Idle -> IdleView(
                onStartRequested = viewModel::start,
                modifier = modifier
            )
            is PlayScreenState.Solving -> SolvingView(
                board = currentState.board,
                onMoveRequested = viewModel::move,
                onResetRequested = viewModel::reset,
                modifier = modifier
            )
            is PlayScreenState.Solved -> SolvedView(
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun PlayScreenPreview() {
    Demo8PuzzleTheme {
        PlayScreen(onInfoRequested = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
internal fun PlayScreenLandscapePreview() {
    Demo8PuzzleTheme {
        PlayScreen(onInfoRequested = {})
    }
}
