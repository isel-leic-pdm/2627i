package isel.dei.pdm.puzzle.ui.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class PlayScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreen_starts_in_Idle_and_can_transition_to_Solving() {
        // Arrange
        composeTestRule.setContent {
            _8PuzzleTheme {
                PlayScreen()
            }
        }

        // Assert: We are in Idle
        composeTestRule.onNodeWithTag(StartButtonTag).assertIsDisplayed()

        // Act: Start the game
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Assert: We are in Solving
        composeTestRule.onNodeWithTag(ResetButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_can_transition_from_Solving_back_to_Idle_via_Reset() {
        // Arrange
        composeTestRule.setContent {
            _8PuzzleTheme {
                PlayScreen()
            }
        }
        // Transition to Solving
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Act: Reset the game
        composeTestRule.onNodeWithTag(ResetButtonTag).performClick()
        // Confirm reset in the dialog
        composeTestRule.onNodeWithTag(ResetDialogConfirmTag).performClick()

        // Assert: We are back in Idle
        composeTestRule.onNodeWithTag(StartButtonTag).assertIsDisplayed()
    }
}
