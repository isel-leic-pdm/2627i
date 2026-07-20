package isel.dei.pdm.puzzle.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PuzzleViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testBoard = Board.SOLVED.move(8)

    @Test
    fun puzzleView_should_display_tiles_at_correct_positions() {
        // Arrange
        // Act
        composeTestRule.setContent {
            _8PuzzleTheme {
                PuzzleView(board = testBoard, onTileClick = {})
            }
        }

        // Assert
        for (row in 0 until Board.BOARD_SIDE) {
            for (col in 0 until Board.BOARD_SIDE) {
                val tile = testBoard.getTileAt(Board.Coordinate(row, col))
                val cellSelector = hasTestTag(cellTag(row, col))
                if (tile != null) {
                    composeTestRule
                        .onNode(cellSelector and hasAnyDescendant(hasTestTag(tileTag(tile))))
                        .assertIsDisplayed()
                } else {
                    // Verify cell is empty (blank space)
                    composeTestRule
                        .onNode(cellSelector)
                        .assertIsDisplayed()
                    
                    // Ensure it doesn't contain any other tiles
                    Board.TILE_RANGE.forEach { t ->
                        composeTestRule
                            .onNode(cellSelector and hasAnyDescendant(hasTestTag(tileTag(t))))
                            .assertDoesNotExist()
                    }
                }
            }
        }
    }

    @Test
    fun puzzleView_should_notify_on_tile_click() {
        // Arrange
        var clickedTile: Int? = null
        val expectedTile = 1

        composeTestRule.setContent {
            _8PuzzleTheme {
                PuzzleView(
                    board = testBoard,
                    onTileClick = { clickedTile = it }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(expectedTile)).performClick()

        // Assert
        assertEquals(expectedTile, clickedTile)
    }
}
