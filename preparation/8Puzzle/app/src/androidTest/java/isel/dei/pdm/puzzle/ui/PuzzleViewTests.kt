package isel.dei.pdm.puzzle.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PuzzleViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun puzzleView_should_display_tiles() {
        // Arrange
        val board = Board.SOLVED

        // Act
        composeTestRule.setContent {
            _8PuzzleTheme {
                PuzzleView(board = board, onTileClick = {})
            }
        }

        // Assert
        Board.TILE_RANGE.forEach { tile ->
            composeTestRule.onNodeWithTag(tileTag(tile)).assertIsDisplayed()
        }
    }

    @Test
    fun puzzleView_should_notify_on_tile_click() {
        // Arrange
        val board = Board.SOLVED
        var clickedTile: Int? = null

        composeTestRule.setContent {
            _8PuzzleTheme {
                PuzzleView(
                    board = board,
                    onTileClick = { clickedTile = it }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert
        assertEquals(1, clickedTile)
    }

    @Test
    fun puzzleView_should_not_notify_when_callback_is_null() {
        // Arrange
        val board = Board.SOLVED
        val clickedTile: Int? = null

        composeTestRule.setContent {
            _8PuzzleTheme {
                PuzzleView(
                    board = board,
                    onTileClick = null
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert
        assertNull(clickedTile)
    }
}
