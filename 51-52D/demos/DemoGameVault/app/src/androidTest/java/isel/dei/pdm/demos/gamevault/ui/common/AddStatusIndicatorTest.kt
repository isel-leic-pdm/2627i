package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADDED_ICON
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADDING_INDICATOR
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADD_ICON
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddStatusIndicatorTest {

    private companion object {
        const val ADD_DESCRIPTION = "Add item"
        const val ADDED_DESCRIPTION = "Item in collection"
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Displays the indicator for the given status.
     */
    private fun setIndicatorContent(status: AddStatus) {
        composeTestRule.setContent {
            AddStatusIndicator(
                status = status,
                addDescription = ADD_DESCRIPTION,
                addedDescription = ADDED_DESCRIPTION
            )
        }
    }

    private fun assertDoesNotExist(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun not_added_status_displays_the_add_icon_only() {
        setIndicatorContent(AddStatus.NOT_ADDED)

        composeTestRule.onNodeWithTag(ADD_ICON).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(ADD_DESCRIPTION).assertIsDisplayed()
        assertDoesNotExist(ADDING_INDICATOR)
        assertDoesNotExist(ADDED_ICON)
    }

    @Test
    fun adding_status_displays_the_progress_indicator_only() {
        setIndicatorContent(AddStatus.ADDING)

        composeTestRule.onNodeWithTag(ADDING_INDICATOR).assertIsDisplayed()
        assertDoesNotExist(ADD_ICON)
        assertDoesNotExist(ADDED_ICON)
    }

    @Test
    fun added_status_displays_the_check_mark_only() {
        setIndicatorContent(AddStatus.ADDED)

        composeTestRule.onNodeWithTag(ADDED_ICON).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(ADDED_DESCRIPTION).assertIsDisplayed()
        assertDoesNotExist(ADD_ICON)
        assertDoesNotExist(ADDING_INDICATOR)
    }

    @Test
    fun add_icon_is_not_interactive() {
        setIndicatorContent(AddStatus.NOT_ADDED)

        composeTestRule.onNodeWithTag(ADD_ICON).assertHasNoClickAction()
    }

    @Test
    fun pressing_the_add_icon_is_handled_by_the_hosting_item() {
        // The indicator is meant to be displayed by an item that is clickable as a whole
        var hostClicks = 0
        composeTestRule.setContent {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { hostClicks++ }
            ) {
                AddStatusIndicator(
                    status = AddStatus.NOT_ADDED,
                    addDescription = ADD_DESCRIPTION,
                    addedDescription = ADDED_DESCRIPTION
                )
            }
        }

        composeTestRule.onNodeWithTag(ADD_ICON, useUnmergedTree = true).performClick()

        assertEquals(1, hostClicks)
    }
}
