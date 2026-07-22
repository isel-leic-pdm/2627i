package isel.dei.pdm.puzzle.about

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Intent
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutActivityTests {

    @get:Rule
    val composeTestRule = createEmptyComposeRule()

    @Test
    fun aboutActivity_opens_browser_when_github_section_is_clicked() {
        Intents.init()
        try {
            ActivityScenario.launch(AboutActivity::class.java).use {
                // Act
                composeTestRule.onNodeWithTag(GitHubSectionTag).performClick()

                // Assert
                intended(
                    allOf(
                        hasAction(Intent.ACTION_VIEW),
                        hasData("https://github.com/isel-leic-pdm")
                    )
                )
            }
        } finally {
            Intents.release()
        }
    }
}
