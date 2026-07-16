package isel.dei.pdm.puzzle.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.PuzzleView
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme

const val SuccessMessageTag = "SuccessMessage"

/**
 * View displayed when the puzzle is solved.
 */
@Composable
fun SolvedView() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PuzzleView(board = Board.SOLVED, onTileClick = null)
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.play_screen_congratulations),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(SuccessMessageTag)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SolvedViewPreview() {
    _8PuzzleTheme {
        SolvedView()
    }
}
