package isel.dei.pdm.puzzle.play.views

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val StopButtonTag = "StopButton"

/**
 * View displayed while the puzzle is being solved automatically.
 * @param board current board state.
 * @param onStopRequested callback to stop the automatic solver.
 * @param modifier the modifier to be applied to the layout.
 */
@Composable
internal fun AutoSolvingView(
    board: Board,
    onStopRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    AdaptivePlayLayout(
        modifier = modifier,
        puzzle = {
            PuzzleView(
                board = board,
                onAnimationFinished = { }
            )
        },
        controls = {
            Button(
                onClick = onStopRequested,
                modifier = Modifier.testTag(StopButtonTag)
            ) {
                Text(
                    text = stringResource(R.string.play_screen_stop_button),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AutoSolvingViewPreview() {
    Demo8PuzzleTheme {
        AutoSolvingView(
            board = Board.createRandom(),
            onStopRequested = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun AutoSolvingViewLandscapePreview() {
    Demo8PuzzleTheme {
        AutoSolvingView(
            board = Board.createRandom(),
            onStopRequested = {}
        )
    }
}
