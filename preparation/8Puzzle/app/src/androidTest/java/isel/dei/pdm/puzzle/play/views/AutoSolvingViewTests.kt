package isel.dei.pdm.puzzle.play.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class AutoSolvingViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreenAutoSolvingView_shows_providedBoard() {
        // Arrange
        val board = Board.SOLVED
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                AutoSolvingView(
                    board = board,
                    onStopRequested = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(PuzzleViewTag).assertIsDisplayed()
    }

    @Test
    fun playScreenAutoSolvingView_shows_stopButton() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                AutoSolvingView(
                    board = Board.SOLVED,
                    onStopRequested = {}
                )
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(StopButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreenAutoSolvingView_pressingStopButton_callsOnStopRequested() {
        // Arrange
        var stopRequested = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                AutoSolvingView(
                    board = Board.SOLVED,
                    onStopRequested = { stopRequested = true }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(StopButtonTag).performClick()

        // Assert
        assert(stopRequested) { "onStopRequested should have been called." }
    }
}
