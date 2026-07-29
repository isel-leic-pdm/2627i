package isel.dei.pdm.puzzle

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.puzzle.about.GitHubSectionTag
import isel.dei.pdm.puzzle.play.views.InfoButtonTag
import isel.dei.pdm.puzzle.play.views.TerminationDialogConfirmTag
import isel.dei.pdm.puzzle.play.views.TerminationDialogTag
import isel.dei.pdm.puzzle.start.StartScreenTag
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_starts_at_StartScreen_and_navigates_to_PlayScreen() {
        // Assert: We are at StartScreen
        composeTestRule.onNodeWithTag(StartScreenTag).assertIsDisplayed()

        // Act: Click Start
        composeTestRule.onNodeWithTag(StartScreenTag).performClick()

        // Assert: We are at PlayScreen (InfoButton is visible)
        composeTestRule.onNodeWithTag(InfoButtonTag).assertIsDisplayed()
    }

    @Test
    fun navigation_to_AboutScreen_and_back() {
        // Arrange: Go to PlayScreen
        composeTestRule.onNodeWithTag(StartScreenTag).performClick()

        // Act: Click Info
        composeTestRule.onNodeWithTag(InfoButtonTag).performClick()

        // Assert: We are at AboutScreen
        composeTestRule.onNodeWithTag(GitHubSectionTag).assertIsDisplayed()

        // Act: Press back
        Espresso.pressBack()

        // Assert: We are back at PlayScreen
        composeTestRule.onNodeWithTag(InfoButtonTag).assertIsDisplayed()
    }

    @Test
    fun exit_confirmation_on_PlayScreen() {
        // Arrange: Go to PlayScreen
        composeTestRule.onNodeWithTag(StartScreenTag).performClick()

        // Act: Press back
        Espresso.pressBack()

        // Assert: Confirmation dialog is shown
        composeTestRule.onNodeWithTag(TerminationDialogTag).assertIsDisplayed()

        // Act: Confirm exit
        composeTestRule.onNodeWithTag(TerminationDialogConfirmTag).performClick()

        // Assert: Activity is finished
        assertTrue(composeTestRule.activity.isFinishing || composeTestRule.activity.isDestroyed)
    }
}
