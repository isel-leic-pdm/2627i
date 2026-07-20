package isel.dei.pdm.puzzle.ui.play

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.AdaptivePlayLayout
import isel.dei.pdm.puzzle.ui.PuzzleView
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme

const val SuccessMessageTag = "SuccessMessage"

/**
 * View displayed when the puzzle is solved.
 */
@Composable
fun SolvedView() {
    AdaptivePlayLayout(
        puzzle = {
            PuzzleView(board = Board.SOLVED, onTileClick = null)
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
fun SolvedViewPreview() {
    _8PuzzleTheme {
        SolvedView()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SolvedViewLandscapePreview() {
    _8PuzzleTheme {
        SolvedView()
    }
}
