package isel.dei.pdm.demos.demo8puzzle.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun main_screen_displays_all_elements() {
        // Arrange & Act
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                MainScreen(onPlay = { })
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(MAIN_IMAGE_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TOUCH_TO_PLAY_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun clicking_touch_to_play_triggers_navigation_callback() {
        // Arrange
        var navigateToPlayCalled = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                MainScreen(onPlay = { navigateToPlayCalled = true })
            }
        }

        // Act
        composeTestRule.onNodeWithTag(TOUCH_TO_PLAY_BUTTON_TAG).performClick()

        // Assert
        assertTrue(navigateToPlayCalled)
    }
}
