package isel.dei.pdm.demos.gamevault.collection.games.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.collection.games.icon
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [FilterSection], using the play states as options, where null stands for any state,
 * as in the collection's filters.
 */
class FilterSectionTest {

    private companion object {
        val OPTIONS: List<PlayStatus.State?> = listOf(null) + PlayStatus.State.entries
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The options selected by the user, in the order they were selected.
     */
    private val selections = mutableListOf<PlayStatus.State?>()

    private fun setSectionContent(selected: PlayStatus.State?) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SectionUnderTest(selected)
            }
        }
    }

    @Composable
    private fun SectionUnderTest(selected: PlayStatus.State?) {
        FilterSection(
            title = "Status",
            options = OPTIONS,
            selected = selected,
            onSelected = { selections += it },
            optionLabel = { it?.name ?: "Any" },
            optionIcon = { it?.icon() }
        )
    }

    private fun option(label: String) = composeTestRule.onNodeWithText(label)

    // Display

    @Test
    fun title_is_displayed_as_a_heading() {
        setSectionContent(selected = null)

        composeTestRule
            .onNodeWithTag(FilterSectionTestTags.TITLE)
            .assertTextEquals("Status")
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
    }

    @Test
    fun one_chip_is_displayed_per_option() {
        setSectionContent(selected = null)

        composeTestRule
            .onAllNodesWithTag(FilterSectionTestTags.OPTION)
            .assertCountEquals(OPTIONS.size)
    }

    @Test
    fun all_options_are_displayed_at_once_by_wrapping_onto_new_lines() {
        setSectionContent(selected = null)

        listOf("Any", "NONE", "BACKLOG", "PLAYING", "PAUSED", "FINISHED", "DROPPED")
            .forEach { option(it).assertIsDisplayed() }
    }

    @Test
    fun only_the_selected_option_is_selected() {
        setSectionContent(selected = PlayStatus.State.PLAYING)

        option("PLAYING").assertIsSelected()
        option("Any").assertIsNotSelected()
        option("BACKLOG").assertIsNotSelected()
    }

    @Test
    fun selection_follows_changes_to_the_selected_option() {
        var selected by mutableStateOf<PlayStatus.State?>(null)
        composeTestRule.setContent {
            DemoGameVaultTheme { SectionUnderTest(selected) }
        }
        option("Any").assertIsSelected()

        selected = PlayStatus.State.FINISHED

        option("FINISHED").assertIsSelected()
        option("Any").assertIsNotSelected()
    }

    // Interaction

    @Test
    fun selecting_another_option_triggers_onSelected_with_it() {
        setSectionContent(selected = null)

        option("BACKLOG").performClick()

        assertEquals(listOf(PlayStatus.State.BACKLOG), selections)
    }

    @Test
    fun selecting_the_option_that_stands_for_not_filtering_triggers_onSelected_with_it() {
        setSectionContent(selected = PlayStatus.State.PLAYING)

        option("Any").performClick()

        assertEquals(listOf<PlayStatus.State?>(null), selections)
    }

    @Test
    fun selecting_the_selected_option_does_not_trigger_onSelected() {
        setSectionContent(selected = PlayStatus.State.PLAYING)

        option("PLAYING").performClick()

        assertEquals(emptyList<PlayStatus.State?>(), selections)
    }

    // Selected option that is not one of the options

    private fun setSectionWithoutTheSelectedOption() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                FilterSection(
                    title = "Status",
                    options = listOf(null, PlayStatus.State.PLAYING),
                    selected = PlayStatus.State.FINISHED,
                    onSelected = { selections += it },
                    optionLabel = { it?.name ?: "Any" }
                )
            }
        }
    }

    @Test
    fun no_option_is_selected_when_the_selected_option_is_not_one_of_the_options() {
        setSectionWithoutTheSelectedOption()

        option("Any").assertIsNotSelected()
        option("PLAYING").assertIsNotSelected()
    }

    @Test
    fun selecting_any_option_triggers_onSelected_when_the_selected_option_is_not_one_of_the_options() {
        setSectionWithoutTheSelectedOption()

        option("Any").performClick()
        option("PLAYING").performClick()

        assertEquals(listOf(null, PlayStatus.State.PLAYING), selections)
    }
}
