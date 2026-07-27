package isel.dei.pdm.puzzle.play.views

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val PuzzleViewTag = "PuzzleView"

/**
 * How long a tile takes to slide from its previous cell to its current one. The play screen's
 * state machine waits out this same duration before celebrating a solved board, so the last
 * slide always finishes before the screen changes.
 */
internal const val TileSlideDurationMillis = 200

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
 * This is a component that only renders the board and notifies of tile clicks.
 * Tiles slide from their previous cell to their current one whenever [board] changes; clicks are
 * ignored while that slide is in progress.
 *
 * ### Design & Implementation Notes:
 * - **Shared Coordinate Space**: Both cells and tiles are siblings within the same coordinate space,
 *  hence the shared parent [BoxWithConstraints]. This common coordinate system is essential for
 *  animating tiles using [Modifier.offset], used to compute the distance between the tile's current
 *  position and their intended position (represented by the sibling cell).
 * - **Layering**: By placing in the composition all cells first and then all tiles, we ensure that
 *  tiles always appear on top of the grid.
 * - **Role of Cells**: While tiles carry the puzzle state and handle interactions, cells provide the
 *  visual grid "slots", represent the blank spaces, and serve as a reference for position
 *  verification in automated tests.
 *
 * @param board The board state to display.
 * @param onAnimationFinished Callback invoked when the tile animation is finished.
 * @param onTileClick Callback invoked when a tile is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
internal fun PuzzleView(
    board: Board,
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onTileClick: ((Int) -> Unit)? = null,
) {
    val transition = updateTransition(targetState = board, label = "puzzleBoard")

    // Track whether this transition has actually started running.
    var hasAnimated by remember { mutableStateOf(false) }
    LaunchedEffect(transition.isRunning) {
        if (transition.isRunning) {
            hasAnimated = true
        } else if (hasAnimated && transition.currentState == transition.targetState) {
            onAnimationFinished()
            hasAnimated = false
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .padding(16.dp)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .testTag(PuzzleViewTag)
    ) {
        val cellSize = maxWidth / Board.BOARD_SIDE
        val cellSizePx = with(LocalDensity.current) { cellSize.roundToPx() }

        // 1. Draw the static grid cells (the board's background).
        // These are siblings to tiles so they share the same coordinate space.
        for (row in 0 until Board.BOARD_SIDE) {
            for (col in 0 until Board.BOARD_SIDE) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(cellSizePx * col, cellSizePx * row) }
                        .size(cellSize)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        .testTag(cellTag(row, col))
                )
            }
        }

        // 2. Draw the tiles on top of the cells.
        // Tiles are moved using an animated offset relative to the board's origin.
        for (tile in Board.TILE_RANGE) {
            val offset by transition.animateIntOffset(
                label = "tile-$tile-offset",
                transitionSpec = { tween(TileSlideDurationMillis, easing = FastOutSlowInEasing) }
            ) { state ->
                val coordinate = state.getCoordinateOf(tile)
                IntOffset(cellSizePx * coordinate.col, cellSizePx * coordinate.row)
            }
            TileView(
                tile = tile,
                onClick = if (!transition.isRunning && onTileClick != null) {
                    { onTileClick(tile) }
                } else null,
                modifier = Modifier
                    .offset { offset }
                    .size(cellSize)
                    .testTag(tileTag(tile))
            )
        }
    }
}

/**
 * A Composable that displays a single tile.
 *
 * @param tile The tile value.
 * @param onClick Callback invoked when the tile is clicked, or null if not clickable.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
private fun TileView(
    tile: Int,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = tile.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PuzzleViewPreviewDemo() {
    Demo8PuzzleTheme {
        PuzzleView(board = Board.SOLVED, onAnimationFinished = {}, onTileClick = {})
    }
}
