package isel.dei.pdm.demos.demo8puzzle.play

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Tags used to identify the tiles for testing purposes.
 */
const val EMPTY_PUZZLE_TILE_TAG = "EmptyPuzzleTile"
fun puzzleTileTag(value: Int) = "PuzzleTile-$value"

@Composable
fun PuzzleTile(tile: Tile?, onTileClicked: (Tile) -> Unit = { }, enabled: Boolean = true) {
    if (tile != null) {
        PuzzleTileWithValue(tile = tile, onClick = { onTileClicked(tile) }, enabled = enabled)
    } else {
        EmptyPuzzleTile()
    }
}

@Composable
private fun PuzzleTileWithValue(
    tile: Tile,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val shape = MaterialTheme.shapes.small
    Box(
        modifier = Modifier
            .size(80.dp)
            .testTag(puzzleTileTag(tile.value))
            .clip(shape)
            .clickable(onClick = onClick, enabled = enabled)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            )
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tile.value.toString(),
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun EmptyPuzzleTile() {
    Box(
        modifier = Modifier
            .size(80.dp)
            .testTag(EMPTY_PUZZLE_TILE_TAG)
            .background(Color.Transparent)
    )
}



@Preview(showBackground = true)
@Composable
fun PuzzleTilePreview() {
    Demo8PuzzleTheme {
        PuzzleTile(tile = Tile(5))
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyPuzzleTilePreview() {
    Demo8PuzzleTheme {
        PuzzleTile(tile = null)
    }
}
