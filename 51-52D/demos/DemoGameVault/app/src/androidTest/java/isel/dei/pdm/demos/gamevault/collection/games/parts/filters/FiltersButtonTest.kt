package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertContentDescriptionContains
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FiltersButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setButtonContent(activeFilterCount: Int, onClick: () -> Unit = {}) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                FiltersButton(activeFilterCount = activeFilterCount, onClick = onClick)
            }
        }
    }

    private fun button() = composeTestRule.onNodeWithTag(FiltersButtonTestTags.BUTTON)

    private fun badge() =
        composeTestRule.onNodeWithTag(FiltersButtonTestTags.BADGE, useUnmergedTree = true)

    // Display

    @Test
    fun button_is_displayed_and_described() {
        setButtonContent(activeFilterCount = 0)

        button()
            .assertIsDisplayed()
            .assertContentDescriptionContains("Filters")
    }

    @Test
    fun badge_is_not_displayed_without_active_filters() {
        setButtonContent(activeFilterCount = 0)

        badge().assertDoesNotExist()
    }

    @Test
    fun badge_displays_the_number_of_active_filters() {
        setButtonContent(activeFilterCount = 2)

        badge().assertIsDisplayed()
        badge().onChild().assertTextEquals("2")
    }

    @Test
    fun badge_is_announced_as_the_number_of_active_filters() {
        setButtonContent(activeFilterCount = 2)

        button().assertContentDescriptionContains("2 active")
    }

    @Test
    fun badge_follows_changes_to_the_number_of_active_filters() {
        var activeFilterCount by mutableIntStateOf(1)
        composeTestRule.setContent {
            DemoGameVaultTheme {
                FiltersButton(activeFilterCount = activeFilterCount, onClick = {})
            }
        }
        badge().onChild().assertTextEquals("1")

        activeFilterCount = 3
        badge().onChild().assertTextEquals("3")

        activeFilterCount = 0
        badge().assertDoesNotExist()
    }

    // Interaction

    @Test
    fun clicking_the_button_triggers_onClick() {
        var clicks = 0
        setButtonContent(activeFilterCount = 1, onClick = { clicks++ })

        button().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun badge_is_not_displayed_for_a_negative_number_of_active_filters() {
        setButtonContent(activeFilterCount = -1)

        button().assertIsDisplayed()
        badge().assertDoesNotExist()
    }
}
