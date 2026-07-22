package isel.dei.pdm.puzzle.start

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.puzzle.play.PlayActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartActivityTests {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun startActivity_navigates_to_PlayActivity_when_clicked() {
        Intents.init()
        try {
            ActivityScenario.launch(StartActivity::class.java).use {
                // Act: Perform a click on the StartScreen (using its test tag)
                composeTestRule.onNodeWithTag(StartScreenTag).performClick()

                // Assert: Verify that an intent to PlayActivity was sent
                intended(hasComponent(PlayActivity::class.java.name))
            }
        } finally {
            Intents.release()
        }
    }
}
