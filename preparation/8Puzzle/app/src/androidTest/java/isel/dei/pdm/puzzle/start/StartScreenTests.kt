package isel.dei.pdm.puzzle.start

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StartScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startScreen_displays_touch_to_play_text() {
        // Arrange
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.start_screen_touch_to_play)

        composeTestRule.setContent {
            Demo8PuzzleTheme {
                StartScreen(onStartRequested = {})
            }
        }

        // Assert
        composeTestRule.onNodeWithText(expectedText).assertIsDisplayed()
    }

    @Test
    fun startScreen_triggers_onStartRequested_when_clicked() {
        // Arrange
        var onStartRequestedCalled = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                StartScreen(onStartRequested = { onStartRequestedCalled = true })
            }
        }

        // Act
        composeTestRule.onNodeWithTag(StartScreenTag).performClick()

        // Assert
        assertTrue(onStartRequestedCalled)
    }
}
