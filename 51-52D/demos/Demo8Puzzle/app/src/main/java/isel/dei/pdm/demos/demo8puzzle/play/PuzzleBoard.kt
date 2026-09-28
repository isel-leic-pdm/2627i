package isel.dei.pdm.demos.demo8puzzle.play

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.demo8puzzle.core.BOARD_SIDE
import isel.dei.pdm.demos.demo8puzzle.core.Puzzle
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Tags used to identify the board for testing purposes.
 */
const val PUZZLE_BOARD_TAG = "PuzzleBoard"

/**
 * Composable function that displays a puzzle board.
 * @param puzzle The puzzle to be displayed.
 * @param onTileClicked Callback function to be invoked when a tile is clicked.
 * @param enabled Whether the board is enabled or not.
 */
@Composable
fun PuzzleBoard(puzzle: Puzzle, onTileClicked: (Tile) -> Unit = { }, enabled: Boolean = true) {
    Column(
        modifier = Modifier
            .testTag(PUZZLE_BOARD_TAG)
            .border(2.dp, MaterialTheme.colorScheme.primary)
            .padding(4.dp),
    ) {
        repeat(BOARD_SIDE) { row ->
            Row {
                repeat(BOARD_SIDE) { col ->
                    PuzzleTile(
                        tile = puzzle[row, col],
                        onTileClicked = onTileClicked,
                        enabled = enabled
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzleBoardPreview() {
    Demo8PuzzleTheme {
        PuzzleBoard(
            puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        )
    }
}
