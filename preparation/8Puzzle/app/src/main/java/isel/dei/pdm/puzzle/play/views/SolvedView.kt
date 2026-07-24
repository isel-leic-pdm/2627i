package isel.dei.pdm.puzzle.play.views

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

internal const val SuccessMessageTag = "SuccessMessage"

/**
 * View displayed when the puzzle is solved.
 * @param modifier the modifier to be applied to the layout.
 */
@Composable
internal fun SolvedView(modifier: Modifier = Modifier) {
    AdaptivePlayLayout(
        modifier = modifier,
        puzzle = {
            PuzzleView(board = Board.SOLVED, onAnimationFinished = {}, onTileClick = null)
        },
        controls = {
            Text(
                text = stringResource(R.string.play_screen_congratulations),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(SuccessMessageTag)
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SolvedViewPreview() {
    Demo8PuzzleTheme {
        SolvedView()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun SolvedViewLandscapePreview() {
    Demo8PuzzleTheme {
        SolvedView()
    }
}
