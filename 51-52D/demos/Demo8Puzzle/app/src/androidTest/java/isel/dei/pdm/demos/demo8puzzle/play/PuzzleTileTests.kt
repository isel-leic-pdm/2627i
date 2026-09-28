package isel.dei.pdm.demos.demo8puzzle.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PuzzleTileTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun tile_with_value_displays_correct_text() {
        // Arrange
        val value = 5
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleTile(tile = Tile(value))
            }
        }

        // Act & Assert
        composeTestRule
            .onNodeWithTag(puzzleTileTag(value))
            .assertIsDisplayed()
            .assertTextEquals(value.toString())
    }

    @Test
    fun tile_with_null_value_displays_empty_tile() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleTile(tile = null)
            }
        }

        // Act & Assert
        composeTestRule
            .onNodeWithTag(EMPTY_PUZZLE_TILE_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun tile_with_value_triggers_click_event() {
        // Arrange
        val value = 5
        var clickedValue: Tile? = null
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleTile(
                    tile = Tile(value),
                    onTileClicked = { clickedValue = it }
                )
            }
        }

        // Act
        composeTestRule
            .onNodeWithTag(puzzleTileTag(value))
            .performClick()

        // Assert
        assertEquals(value, clickedValue?.value)
    }
}
