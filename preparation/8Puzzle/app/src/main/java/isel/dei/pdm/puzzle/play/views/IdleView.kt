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

internal const val StartButtonTag = "StartButton"

/**
 * View displayed when the game is in the Idle state.
 * @param onStartRequested callback to start the game.
 * @param modifier the modifier to be applied to the layout.
 */
@Composable
internal fun IdleView(onStartRequested: () -> Unit, modifier: Modifier = Modifier) {
    AdaptivePlayLayout(
        modifier = modifier,
        puzzle = {
            PuzzleView(board = Board.SOLVED, onTileClick = null)
        },
        controls = {
            Button(
                onClick = onStartRequested,
                modifier = Modifier.testTag(StartButtonTag)
            ) {
                Text(
                    text = stringResource(R.string.play_screen_start_button),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
internal fun IdleViewPreview() {
    Demo8PuzzleTheme {
        IdleView(onStartRequested = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
internal fun IdleViewLandscapePreview() {
    Demo8PuzzleTheme {
        IdleView(onStartRequested = {})
    }
}
