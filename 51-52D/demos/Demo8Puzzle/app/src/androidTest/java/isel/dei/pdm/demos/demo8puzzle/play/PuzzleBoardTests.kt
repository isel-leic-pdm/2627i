package isel.dei.pdm.demos.demo8puzzle.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.demo8puzzle.core.MAX_TILE_VALUE
import isel.dei.pdm.demos.demo8puzzle.core.MIN_TILE_VALUE
import isel.dei.pdm.demos.demo8puzzle.core.Puzzle
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

class PuzzleBoardTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun board_displays_all_tiles() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleBoard(puzzle = puzzle)
            }
        }

        // Act & Assert
        composeTestRule.onNodeWithTag(PUZZLE_BOARD_TAG).assertIsDisplayed()
        
        for (value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            composeTestRule.onNodeWithTag(puzzleTileTag(value)).assertIsDisplayed()
        }
        composeTestRule.onNodeWithTag(EMPTY_PUZZLE_TILE_TAG).assertIsDisplayed()
    }

    @Test
    fun board_triggers_click_event_when_tile_is_clicked() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        var clickedValue: Tile? = null
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleBoard(
                    puzzle = puzzle,
                    onTileClicked = { clickedValue = it }
                )
            }
        }

        // Act
        val tileToClick = 5
        val expectedTile = Tile(tileToClick)
        composeTestRule.onNodeWithTag(puzzleTileTag(tileToClick)).performClick()

        // Assert
        assertNotNull(clickedValue)
        assertEquals(expectedTile.value, clickedValue?.value)
    }

    @Test
    fun board_tiles_are_disabled_when_enabled_is_false() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleBoard(
                    puzzle = puzzle,
                    enabled = false
                )
            }
        }

        // Act & Assert
        for (value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            composeTestRule.onNodeWithTag(puzzleTileTag(value)).assertIsNotEnabled()
        }
    }
}
