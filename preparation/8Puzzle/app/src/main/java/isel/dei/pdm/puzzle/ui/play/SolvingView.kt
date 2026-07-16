package isel.dei.pdm.puzzle.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

const val ResetButtonTag = "ResetButton"

/**
 * View displayed while the user is solving the puzzle.
 * @param board current board state.
 * @param onMoveRequested callback when a tile is clicked.
 * @param onResetRequested callback to reset the game.
 */
@Composable
fun SolvingView(
    board: Board,
    onMoveRequested: (Int) -> Unit,
    onResetRequested: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PuzzleView(board = board, onTileClick = onMoveRequested)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onResetRequested,
            modifier = Modifier.testTag(ResetButtonTag)
        ) {
            Text(
                text = stringResource(R.string.play_screen_reset_button),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SolvingViewPreview() {
    _8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onResetRequested = {}
        )
    }
}
