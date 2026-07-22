package isel.dei.pdm.puzzle.play.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class IdleViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreenIdleView_shows_solvedPuzzle() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                IdleView(onStartRequested = {})
            }
        }

        // Act & Assert
        composeTestRule.onNodeWithTag(PuzzleViewTag).assertIsDisplayed()
    }

    @Test
    fun playScreenIdleView_pressingStartButton_callsOnStartRequested() {
        // Arrange
        var startRequested = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                IdleView(onStartRequested = { startRequested = true })
            }
        }

        // Act
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Assert
        assert(startRequested) { "onStartRequested should have been called when Start button is clicked." }
    }

    @Test
    fun playScreenIdleView_pressingTile_doesNotCallOnStartRequested() {
        // Arrange
        var startRequested = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                IdleView(onStartRequested = { startRequested = true })
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert
        assert(!startRequested) { "onStartRequested should not have been called when a tile is clicked." }
    }
}