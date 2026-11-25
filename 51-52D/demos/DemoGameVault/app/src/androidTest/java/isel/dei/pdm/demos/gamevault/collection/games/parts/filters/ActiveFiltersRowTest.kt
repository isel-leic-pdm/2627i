package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ActiveFiltersRowTest {

    private companion object {
        val PLAYING = CollectionFilter.InState(PlayStatus.State.PLAYING)
        val PLATINUM = CollectionFilter.WithCompletion(Completion.PLATINUM)
        val ON_PS5 = CollectionFilter.OnPlatform(Platform(1L, "PS5", "PlayStation 5"))
        val ALL_KINDS = listOf(PLAYING, PLATINUM, ON_PS5)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The filters removed by the user, in the order they were removed.
     */
    private val removed = mutableListOf<CollectionFilter>()

    private fun setRowContent(filters: List<CollectionFilter>) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ActiveFiltersRow(filters = filters, onRemoveFilter = { removed += it })
            }
        }
    }

    private fun chips() = composeTestRule.onAllNodesWithTag(ActiveFiltersRowTestTags.CHIP)

    // Display

    @Test
    fun nothing_is_displayed_without_filters() {
        setRowContent(emptyList())

        composeTestRule.onNodeWithTag(ActiveFiltersRowTestTags.ROW).assertDoesNotExist()
    }

    @Test
    fun one_chip_is_displayed_per_filter() {
        setRowContent(ALL_KINDS)

        composeTestRule.onNodeWithTag(ActiveFiltersRowTestTags.ROW).assertIsDisplayed()
        chips().assertCountEquals(ALL_KINDS.size)
    }

    @Test
    fun chips_are_labelled_by_state_platinum_and_platform_abbreviation() {
        setRowContent(ALL_KINDS)

        listOf("Playing", "Platinum", "PS5").forEach {
            composeTestRule.onNodeWithText(it, useUnmergedTree = true).assertIsDisplayed()
        }
    }

    @Test
    fun chips_are_described_as_removing_their_filter() {
        setRowContent(listOf(ON_PS5))

        composeTestRule
            .onNodeWithContentDescription("Remove PS5 filter", useUnmergedTree = true)
            .assertExists()
    }

    @Test
    fun chips_follow_changes_to_the_filters() {
        var filters by mutableStateOf(ALL_KINDS)
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ActiveFiltersRow(filters = filters, onRemoveFilter = {})
            }
        }

        filters = listOf(PLATINUM)
        chips().assertCountEquals(1)
        composeTestRule.onNodeWithText("Platinum", useUnmergedTree = true).assertIsDisplayed()

        filters = emptyList()
        composeTestRule.onNodeWithTag(ActiveFiltersRowTestTags.ROW).assertDoesNotExist()
    }

    // Interaction

    @Test
    fun clicking_a_chip_triggers_onRemoveFilter_with_its_filter() {
        setRowContent(ALL_KINDS)

        composeTestRule.onNodeWithText("PS5", useUnmergedTree = true).performClick()
        composeTestRule.onNodeWithText("Playing", useUnmergedTree = true).performClick()

        assertEquals(listOf(ON_PS5, PLAYING), removed)
    }
}
