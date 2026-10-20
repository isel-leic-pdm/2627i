package isel.dei.pdm.demos.gamevault.search

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import isel.dei.pdm.demos.gamevault.core.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class SearchBoxTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun hasEditableText(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    @Test
    fun input_field_is_displayed() {
        // Arrange & Act
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = {},
                    onQueryCleared = {},
                )
            }
        }

        // Assert
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .assertIsDisplayed()
    }

    @Test
    fun entering_valid_search_query_triggers_onQueryChange_after_debounce() {
        // Arrange
        var capturedQuery: SearchString? = null
        val delayMS = 100L

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = { capturedQuery = it },
                    onQueryCleared = {},
                    debounceDelay = delayMS.milliseconds,
                )
            }
        }

        // Act
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .performTextInput("PlayStation")

        // Wait for debounce delay
        composeTestRule.waitUntil(timeoutMillis = delayMS * 2) {
            capturedQuery != null
        }

        // Assert
        assertEquals("PlayStation", capturedQuery?.value)
    }

    @Test
    fun entering_query_with_less_than_2_non_blank_characters_does_not_trigger_onQueryChange() {
        // Arrange
        var queryChangeTriggered = false
        val delayMS = 100L

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = { queryChangeTriggered = true },
                    onQueryCleared = {},
                    debounceDelay = delayMS.milliseconds,
                )
            }
        }

        // Act
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .performTextInput("P")

        // Wait a moment longer than the debounce delay
        composeTestRule.mainClock.advanceTimeBy(delayMS * 2)

        // Assert
        assertFalse(queryChangeTriggered)
    }

    @Test
    fun clear_button_is_displayed_only_when_input_is_not_empty() {
        // Arrange
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = {},
                    onQueryCleared = {},
                )
            }
        }

        // Assert - Initially clear button does not exist
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)
            .assertDoesNotExist()

        // Act - Type text
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .performTextInput("PS5")

        // Assert - Clear button is now displayed
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_clear_button_clears_input_text() {
        // Arrange
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = {},
                    onQueryCleared = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .performTextInput("PS5")

        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .assert(hasEditableText("PS5"))

        // Act
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)
            .performClick()

        // Assert
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .assert(hasEditableText(""))
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun clicking_clear_button_triggers_onQueryCleared() {
        // Arrange
        var queryClearedTriggered = false

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SearchBox(
                    onQueryChange = {},
                    onQueryCleared = { queryClearedTriggered = true },
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .performTextInput("PS5")

        // Act
        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)
            .performClick()

        // Assert
        assertTrue(queryClearedTriggered)
    }
}
