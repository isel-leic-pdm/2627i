package isel.dei.pdm.puzzle.ui.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import isel.dei.pdm.puzzle.ui.PuzzleViewTag
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class SolvedViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreenSolvedView_shows_solvedBoard() {
        // Arrange & Act
        composeTestRule.setContent {
            _8PuzzleTheme {
                SolvedView()
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(PuzzleViewTag).assertIsDisplayed()
    }

    @Test
    fun playScreenSolvedView_shows_successMessage() {
        // Arrange & Act
        composeTestRule.setContent {
            _8PuzzleTheme {
                SolvedView()
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(SuccessMessageTag).assertIsDisplayed()
    }
}
