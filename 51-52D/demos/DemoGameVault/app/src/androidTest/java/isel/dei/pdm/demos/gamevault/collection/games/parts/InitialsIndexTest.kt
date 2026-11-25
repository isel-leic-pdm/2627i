package isel.dei.pdm.demos.gamevault.collection.games.parts

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InitialsIndexTest {

    private companion object {
        val INITIALS = listOf('C', 'E', 'H', 'S', 'Z', '#')
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The initials selected by the user, in the order they were selected.
     */
    private val selected = mutableListOf<Char>()

    private fun setIndexContent(initials: List<Char> = INITIALS) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                InitialsIndex(initials = initials, onInitialSelected = { selected += it })
            }
        }
    }

    private fun initial(initial: Char) = composeTestRule.onNode(
        hasTestTag(InitialsIndexTestTags.INITIAL) and hasText(initial.toString())
    )

    // Display

    @Test
    fun only_the_given_initials_are_displayed() {
        setIndexContent()

        composeTestRule
            .onAllNodesWithTag(InitialsIndexTestTags.INITIAL)
            .assertCountEquals(INITIALS.size)
        INITIALS.forEach { initial(it).assertIsDisplayed() }
    }

    @Test
    fun initials_are_labelled_for_jumping_to_their_section() {
        setIndexContent()

        initial('H').assert(
            SemanticsMatcher("has the 'Jump to H' click label") {
                it.config.getOrNull(SemanticsActions.OnClick)?.label == "Jump to H"
            }
        )
    }

    // Interaction

    @Test
    fun tapping_an_initial_selects_it() {
        setIndexContent()

        initial('H').performClick()
        initial('#').performClick()

        assertEquals(listOf('H', '#'), selected)
    }

    @Test
    fun dragging_along_the_index_selects_each_initial_once_in_order() {
        setIndexContent()

        composeTestRule.onNodeWithTag(InitialsIndexTestTags.INDEX).performTouchInput {
            swipe(start = topCenter, end = bottomCenter, durationMillis = 1_000)
        }

        assertEquals(INITIALS, selected)
    }

    @Test
    fun the_accessibility_click_action_selects_the_initial() {
        setIndexContent()

        initial('S').performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(listOf('S'), selected)
    }
}
