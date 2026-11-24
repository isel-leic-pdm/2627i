package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameNotesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var editClicks = 0

    private fun setContent(notes: NonBlankString?) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameNotes(notes = notes, onEdit = { editClicks++ })
            }
        }
    }

    private fun editButton() = composeTestRule.onNodeWithTag(GameNotesTestTags.EDIT_BUTTON)

    @Test
    fun displays_the_notes() {
        setContent(notes = NonBlankString("Stopped at Malenia."))

        composeTestRule.onNodeWithText("Notes").assertIsDisplayed()
        composeTestRule.onNodeWithTag(GameNotesTestTags.NOTES)
            .assertIsDisplayed()
            .assertTextEquals("Stopped at Malenia.")
        composeTestRule.onNodeWithTag(GameNotesTestTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun without_notes_a_placeholder_is_displayed() {
        setContent(notes = null)

        composeTestRule.onNodeWithTag(GameNotesTestTags.EMPTY)
            .assertIsDisplayed()
            .assertTextEquals("Add notes about your playthrough")
        composeTestRule.onNodeWithTag(GameNotesTestTags.NOTES).assertDoesNotExist()
    }

    @Test
    fun clicking_edit_without_notes_triggers_onEdit() {
        setContent(notes = null)

        editButton().assertIsDisplayed().assertContentDescriptionEquals("Edit notes")
        editButton().performClick()

        assertEquals(1, editClicks)
    }

    @Test
    fun clicking_edit_with_notes_triggers_onEdit() {
        setContent(notes = NonBlankString("Stopped at Malenia."))

        editButton().assertIsDisplayed().assertContentDescriptionEquals("Edit notes")
        editButton().performClick()

        assertEquals(1, editClicks)
    }
}
