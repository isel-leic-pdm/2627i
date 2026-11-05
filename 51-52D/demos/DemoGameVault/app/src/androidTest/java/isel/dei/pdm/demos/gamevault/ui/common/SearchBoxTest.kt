package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.text.AnnotatedString
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [SearchBox]. The main clock is paused, so that the debounce delay is driven by the
 * tests, making them fast and deterministic.
 */
class SearchBoxTest {

    private companion object {
        val DEBOUNCE_DELAY = 300.milliseconds
        val DEBOUNCE_DELAY_MS = DEBOUNCE_DELAY.inWholeMilliseconds
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The events emitted by the search box, in the order they were emitted. Queries are recorded
     * by their value, and clears as null.
     */
    private val emitted = mutableListOf<String?>()

    private val emittedQueries: List<String>
        get() = emitted.filterNotNull()

    private val clearCount: Int
        get() = emitted.count { it == null }

    @Before
    fun pauseClock() {
        composeTestRule.mainClock.autoAdvance = false
    }

    @Composable
    private fun SearchBoxUnderTest(initialQuery: SearchString? = null) {
        DemoGameVaultTheme {
            SearchBox(
                onQueryChange = { emitted += it.value },
                onQueryCleared = { emitted += null },
                initialQuery = initialQuery,
                debounceDelay = DEBOUNCE_DELAY,
            )
        }
    }

    private fun setSearchBoxContent(initialQuery: SearchString? = null) {
        composeTestRule.setContent { SearchBoxUnderTest(initialQuery) }
    }

    /**
     * Displays the search box so that its state can be saved and restored, as happens on
     * configuration changes and process death.
     */
    private fun setRestorableSearchBoxContent(): StateRestorationTester =
        StateRestorationTester(composeTestRule).apply {
            setContent { SearchBoxUnderTest() }
        }

    private fun advancePastDebounce() {
        composeTestRule.mainClock.advanceTimeBy(DEBOUNCE_DELAY_MS * 2)
    }

    private fun input() = composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT)

    private fun clearButton() = composeTestRule.onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON)

    private fun hasEditableText(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    /**
     * Enters the given text and lets the debounce delay elapse.
     */
    private fun enter(text: String) {
        input().performTextReplacement(text)
        advancePastDebounce()
    }

    // Display

    @Test
    fun input_field_is_displayed() {
        setSearchBoxContent()

        input().assertIsDisplayed()
    }

    @Test
    fun clear_button_is_displayed_only_when_input_is_not_empty() {
        setSearchBoxContent()
        clearButton().assertDoesNotExist()

        input().performTextInput("P")
        composeTestRule.mainClock.advanceTimeByFrame()

        clearButton().assertIsDisplayed()
    }

    // Query changes

    @Test
    fun valid_query_triggers_onQueryChange_only_after_the_debounce_delay() {
        setSearchBoxContent()

        input().performTextInput("PlayStation")
        composeTestRule.mainClock.advanceTimeBy(DEBOUNCE_DELAY_MS / 2)
        assertEquals(emptyList<String>(), emittedQueries)

        composeTestRule.mainClock.advanceTimeBy(DEBOUNCE_DELAY_MS)
        assertEquals(listOf("PlayStation"), emittedQueries)
    }

    @Test
    fun typing_faster_than_the_debounce_delay_triggers_onQueryChange_only_for_the_final_query() {
        setSearchBoxContent()

        input().performTextInput("PS")
        composeTestRule.mainClock.advanceTimeBy(DEBOUNCE_DELAY_MS / 2)
        input().performTextInput("5")
        advancePastDebounce()

        assertEquals(listOf("PS5"), emittedQueries)
    }

    @Test
    fun query_with_less_than_2_non_blank_characters_does_not_trigger_onQueryChange() {
        setSearchBoxContent()

        enter("P")

        assertEquals(emptyList<String?>(), emitted)
    }

    @Test
    fun entering_a_different_query_triggers_onQueryChange_again() {
        setSearchBoxContent()
        enter("PS5")

        enter("PS4")

        assertEquals(listOf("PS5", "PS4"), emittedQueries)
    }

    @Test
    fun entering_the_same_query_does_not_trigger_onQueryChange_again() {
        setSearchBoxContent()
        enter("PS5")

        // Same query, differing only in surrounding whitespace
        enter(" PS5 ")

        assertEquals(listOf("PS5"), emittedQueries)
    }

    // Query cleared

    @Test
    fun deleting_all_text_after_a_valid_query_triggers_onQueryCleared() {
        setSearchBoxContent()
        enter("PS5")

        input().performTextClearance()
        advancePastDebounce()

        assertEquals(listOf("PS5", null), emitted)
    }

    @Test
    fun deleting_text_until_query_becomes_invalid_triggers_onQueryCleared() {
        setSearchBoxContent()
        enter("PS5")

        enter("P")

        assertEquals(listOf("PS5", null), emitted)
    }

    @Test
    fun invalid_query_without_a_previous_valid_query_does_not_trigger_onQueryCleared() {
        setSearchBoxContent()

        enter("P")
        enter("")

        assertEquals(0, clearCount)
    }

    @Test
    fun onQueryCleared_is_not_triggered_again_until_a_new_query_is_emitted() {
        setSearchBoxContent()
        enter("PS5")
        enter("P")

        // Clear button pressed after the query was already cleared
        clearButton().performClick()
        advancePastDebounce()

        assertEquals(listOf("PS5", null), emitted)
    }

    // Clear button

    @Test
    fun clicking_clear_button_clears_input_text() {
        setSearchBoxContent()
        enter("PS5")
        input().assert(hasEditableText("PS5"))

        clearButton().performClick()
        composeTestRule.mainClock.advanceTimeByFrame()

        input().assert(hasEditableText(""))
        clearButton().assertDoesNotExist()
    }

    @Test
    fun clicking_clear_button_triggers_onQueryCleared_without_debounce() {
        setSearchBoxContent()
        enter("PS5")

        // The clock is not advanced: the event is emitted on click
        clearButton().performClick()

        assertEquals(listOf("PS5", null), emitted)
    }

    @Test
    fun clicking_clear_button_before_any_query_is_emitted_does_not_trigger_onQueryCleared() {
        setSearchBoxContent()
        input().performTextInput("P")
        composeTestRule.mainClock.advanceTimeByFrame()

        clearButton().performClick()
        advancePastDebounce()

        assertEquals(0, clearCount)
    }

    // Initial query

    @Test
    fun initial_query_is_displayed_but_not_emitted() {
        setSearchBoxContent(initialQuery = SearchString("PS5"))
        advancePastDebounce()

        input().assert(hasEditableText("PS5"))
        assertEquals(emptyList<String?>(), emitted)
    }

    @Test
    fun initial_query_with_surrounding_whitespace_is_not_emitted() {
        setSearchBoxContent(initialQuery = SearchString(" PS5 "))
        advancePastDebounce()

        assertEquals(emptyList<String?>(), emitted)
    }

    @Test
    fun initial_query_is_considered_as_emitted_when_clearing() {
        setSearchBoxContent(initialQuery = SearchString("PS5"))
        advancePastDebounce()

        clearButton().performClick()

        assertEquals(listOf<String?>(null), emitted)
    }

    @Test
    fun changing_the_initial_query_triggers_onQueryChange() {
        setSearchBoxContent(initialQuery = SearchString("PS5"))
        advancePastDebounce()

        enter("PS4")

        assertEquals(listOf("PS4"), emittedQueries)
    }

    // State restoration (e.g. configuration changes and process death)

    @Test
    fun last_emitted_query_is_not_emitted_again_after_state_restoration() {
        val restorationTester = setRestorableSearchBoxContent()
        enter("PS5")

        restorationTester.emulateSavedInstanceStateRestore()
        advancePastDebounce()

        input().assert(hasEditableText("PS5"))
        assertEquals(listOf("PS5"), emittedQueries)
    }

    @Test
    fun query_entered_after_state_restoration_is_emitted() {
        val restorationTester = setRestorableSearchBoxContent()
        enter("PS5")
        restorationTester.emulateSavedInstanceStateRestore()

        enter("PS4")

        assertEquals(listOf("PS5", "PS4"), emittedQueries)
    }
}
