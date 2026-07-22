package isel.dei.pdm.puzzle.play.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val PuzzleViewTag = "PuzzleView"

/**
 * Function that returns the test tag for a specific tile.
 * @param tile the tile value.
 * @return the test tag.
 */
internal fun tileTag(tile: Int) = "tile-$tile"

/**
 * Function that returns the test tag for a specific cell.
 * @param row the row index.
 * @param col the column index.
 * @return the test tag.
 */
internal fun cellTag(row: Int, col: Int) = "cell-$row-$col"

/**
 * A Composable that displays the 8-puzzle board.
 * This is a "dumb" component that only renders the board and notifies of tile clicks.
 *
 * @param board The board state to display.
 * @param onTileClick Callback invoked when a tile is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
internal fun PuzzleView(
    board: Board,
    modifier: Modifier = Modifier,
    onTileClick: ((Int) -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .testTag(PuzzleViewTag)
    ) {
        for (row in 0 until Board.BOARD_SIDE) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until Board.BOARD_SIDE) {
                    val tile = board.getTileAt(Board.Coordinate(row, col))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .testTag(cellTag(row, col))
                    ) {
                        TileView(
                            tile = tile,
                            onClick = if (tile != null && onTileClick != null) {
                                { onTileClick(tile) }
                            } else null,
                            modifier = Modifier
                                .then(if (tile != null) Modifier.testTag(tileTag(tile)) else Modifier)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A Composable that displays a single tile.
 *
 * @param tile The tile value, or null if it's the blank space.
 * @param onClick Callback invoked when the tile is clicked, or null if not clickable.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
private fun TileView(
    tile: Int?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(if (tile != null) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
    ) {
        if (tile != null) {
            Text(
                text = tile.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun PuzzleViewPreviewDemo() {
    Demo8PuzzleTheme {
        PuzzleView(board = Board.SOLVED, onTileClick = {})
    }
}
