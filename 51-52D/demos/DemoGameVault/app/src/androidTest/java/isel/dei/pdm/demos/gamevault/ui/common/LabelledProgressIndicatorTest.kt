package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Rule
import org.junit.Test

class LabelledProgressIndicatorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun labelled_progress_indicator_displays_indicator_and_text() {
        val loadingText = "Loading settings…"

        composeTestRule.setContent {
            DemoGameVaultTheme {
                LabelledProgressIndicator(text = loadingText)
            }
        }

        composeTestRule
            .onNodeWithTag(LabelledProgressIndicatorTestTags.INDICATOR)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LabelledProgressIndicatorTestTags.TEXT)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(loadingText)
            .assertIsDisplayed()
    }
}
