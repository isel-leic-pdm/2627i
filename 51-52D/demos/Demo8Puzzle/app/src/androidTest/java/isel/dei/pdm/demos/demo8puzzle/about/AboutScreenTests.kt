package isel.dei.pdm.demos.demo8puzzle.about

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AboutScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun about_screen_displays_all_elements() {
        // Arrange & Act
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                AboutScreen(onNavigateToGitHub = { })
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(ABOUT_LOGO_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ABOUT_DESCRIPTION_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ABOUT_GITHUB_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun clicking_github_button_triggers_navigation_callback() {
        // Arrange
        var navigateToGitHubCalled = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                AboutScreen(onNavigateToGitHub = { navigateToGitHubCalled = true })
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ABOUT_GITHUB_BUTTON_TAG).performClick()

        // Assert
        assertTrue(navigateToGitHubCalled)
    }
}
