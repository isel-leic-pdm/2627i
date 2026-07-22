package isel.dei.pdm.puzzle.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.puzzle.about.AboutActivity
import isel.dei.pdm.puzzle.play.views.InfoButtonTag
import isel.dei.pdm.puzzle.play.views.TerminationDialogConfirmTag
import isel.dei.pdm.puzzle.play.views.TerminationDialogDismissTag
import isel.dei.pdm.puzzle.play.views.TerminationDialogTag
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayActivityTests {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun playActivity_navigates_to_AboutActivity_when_info_button_is_clicked() {
        Intents.init()
        try {
            ActivityScenario.launch(PlayActivity::class.java).use {
                // Act
                composeTestRule.onNodeWithTag(InfoButtonTag).performClick()

                // Assert
                intended(hasComponent(AboutActivity::class.java.name))
            }
        } finally {
            Intents.release()
        }
    }

    @Test
    fun playActivity_shows_termination_dialog_when_back_is_pressed() {
        ActivityScenario.launch(PlayActivity::class.java).use {
            // Act
            Espresso.pressBack()

            // Assert
            composeTestRule.onNodeWithTag(TerminationDialogTag).assertIsDisplayed()
        }
    }

    @Test
    fun playActivity_finishes_when_exit_is_confirmed() {
        ActivityScenario.launch(PlayActivity::class.java).use { scenario ->
            // Arrange
            Espresso.pressBack()
            composeTestRule.onNodeWithTag(TerminationDialogTag).assertIsDisplayed()

            // Act
            composeTestRule.onNodeWithTag(TerminationDialogConfirmTag).performClick()

            // Assert
            assertTrue(scenario.state.isAtLeast(androidx.lifecycle.Lifecycle.State.DESTROYED))
        }
    }

    @Test
    fun playActivity_remains_when_exit_is_dismissed() {
        ActivityScenario.launch(PlayActivity::class.java).use { scenario ->
            // Arrange
            Espresso.pressBack()
            composeTestRule.onNodeWithTag(TerminationDialogTag).assertIsDisplayed()

            // Act
            composeTestRule.onNodeWithTag(TerminationDialogDismissTag).performClick()

            // Assert
            composeTestRule.onNodeWithTag(TerminationDialogTag).assertDoesNotExist()
            assertTrue(scenario.state.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))
        }
    }
}
