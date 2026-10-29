package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SecretTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "SecretTextInput"

    @Test
    fun secret_text_is_displayed() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = "my-secret-value",
                    onValueChange = {},
                    modifier = Modifier.testTag(testTag)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(testTag)
            .assertIsDisplayed()
    }

    @Test
    fun show_toggle_button_is_displayed_initially() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val showDescription = context.getString(R.string.secret_text_show)

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = "my-secret-value",
                    onValueChange = {},
                    modifier = Modifier.testTag(testTag)
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_toggle_button_shows_secret() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val showDescription = context.getString(R.string.secret_text_show)
        val hideDescription = context.getString(R.string.secret_text_hide)

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = "my-secret-value",
                    onValueChange = {},
                    modifier = Modifier.testTag(testTag)
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(hideDescription)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_toggle_button_twice_hides_secret() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val showDescription = context.getString(R.string.secret_text_show)
        val hideDescription = context.getString(R.string.secret_text_hide)

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = "my-secret-value",
                    onValueChange = {},
                    modifier = Modifier.testTag(testTag)
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(hideDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .assertIsDisplayed()
    }

    @Test
    fun secret_text_triggers_onValueChange() {
        var value = ""

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.testTag(testTag)
                )
            }
        }

        composeTestRule
            .onNodeWithTag(testTag)
            .performTextInput("new-secret")

        assertEquals("new-secret", value)
    }
}
