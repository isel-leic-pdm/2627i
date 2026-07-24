package isel.dei.pdm.puzzle.play

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    viewModel: PlayScreenViewModel = viewModel()
) {
    Scaffold(
        topBar = { PlayScreenTopBar(onInfoClick = onInfoRequested) }
    ) { innerPadding ->

        val modifier = Modifier.padding(innerPadding)
        AnimatedContent(
            targetState = viewModel.state,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            contentKey = { state ->
                when (state) {
                    is PlayScreenState.Idle -> "Idle"
                    is PlayScreenState.Solving -> "Solving"
                    is PlayScreenState.Solved -> "Solved"
                }
            },
            label = "PlayScreenStateTransition"
        ) { state ->
            when (state) {
                is PlayScreenState.Idle -> IdleView(
                    onStartRequested = viewModel::start,
                    modifier = modifier
                )
                is PlayScreenState.Solving -> SolvingView(
                    board = state.board,
                    onMoveRequested = viewModel::move,
                    onAnimationFinished = viewModel::onAnimationFinished,
                    onResetRequested = viewModel::reset,
                    modifier = modifier
                )
                is PlayScreenState.Solved -> SolvedView(
                    modifier = modifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlayScreenPreview() {
    Demo8PuzzleTheme {
        PlayScreen(onInfoRequested = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun PlayScreenLandscapePreview() {
    Demo8PuzzleTheme {
        PlayScreen(onInfoRequested = {})
    }
}

// 800x400 leaves the puzzle exactly as much height as width after the top bar and paddings are
// subtracted, so it can't catch a puzzle that's sized wider than it is tall. A real phone's
// landscape aspect ratio is far more elongated than 2:1, so this preview trades width for height
// to reproduce that squeeze.
@Preview(showBackground = true, widthDp = 900, heightDp = 360)
@Composable
private fun PlayScreenLandscapeNarrowPreview() {
    Demo8PuzzleTheme {
        PlayScreen(onInfoRequested = {})
    }
}
