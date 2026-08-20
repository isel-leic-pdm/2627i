package isel.dei.pdm.mygamevault

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import isel.dei.pdm.mygamevault.ui.common.SEARCH_BAR_TAG
import org.junit.Rule
import org.junit.Test

class MainActivityTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_launches_and_can_search() {
        // Act: Type a query
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).performTextInput("Elden")

        // Wait for debounce (2s) and search delay (0.5s)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Elden Ring").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert: Result is displayed
        composeTestRule.onNodeWithText("Elden Ring").assertIsDisplayed()
    }
}
