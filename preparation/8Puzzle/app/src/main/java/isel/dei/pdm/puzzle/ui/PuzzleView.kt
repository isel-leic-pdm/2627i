package isel.dei.pdm.puzzle.ui

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
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme

const val PuzzleViewTag = "PuzzleView"
fun tileTag(tile: Int) = "tile-$tile"

/**
 * A Composable that displays the 8-puzzle board.
 * This is a "dumb" component that only renders the board and notifies of tile clicks.
 *
 * @param board The board state to display.
 * @param onTileClick Callback invoked when a tile is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PuzzleView(
    board: Board,
    onTileClick: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
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
                    TileView(
                        tile = tile,
                        onClick = if (tile != null && onTileClick != null) {
                            { onTileClick(tile) }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .then(if (tile != null) Modifier.testTag(tileTag(tile)) else Modifier)
                    )
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
fun TileView(
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
fun PuzzleViewPreview() {
    _8PuzzleTheme {
        PuzzleView(board = Board.SOLVED, onTileClick = {})
    }
}
