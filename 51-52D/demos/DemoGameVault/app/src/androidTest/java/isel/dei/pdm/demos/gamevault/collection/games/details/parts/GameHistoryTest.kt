package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GameHistoryTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val march = LocalDate(2024, 3, 10)
    private val august = LocalDate(2025, 8, 2)

    private fun setContent(completedRuns: List<Accomplishment>, platinum: Accomplishment? = null) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameHistory(completedRuns = completedRuns, platinum = platinum)
            }
        }
    }

    /**
     * The displayed entries, in order, as (name, date) pairs.
     */
    private fun displayedEntries(): List<Pair<String, String>> {
        val names = composeTestRule.onAllNodesWithTag(GameHistoryTestTags.ENTRY_NAME, useUnmergedTree = true)
            .fetchSemanticsNodes().map { it.text() }
        val dates = composeTestRule.onAllNodesWithTag(GameHistoryTestTags.ENTRY_DATE, useUnmergedTree = true)
            .fetchSemanticsNodes().map { it.text() }
        return names.zip(dates)
    }

    private fun SemanticsNode.text(): String =
        config[SemanticsProperties.Text].joinToString { it.text }

    @Test
    fun displays_the_title() {
        setContent(completedRuns = emptyList())

        composeTestRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun without_completed_runs_a_placeholder_is_displayed() {
        setContent(completedRuns = emptyList())

        composeTestRule.onNodeWithTag(GameHistoryTestTags.EMPTY)
            .assertIsDisplayed()
            .assertTextEquals("No completed runs yet")
        composeTestRule.onAllNodesWithTag(GameHistoryTestTags.ENTRY).assertCountEquals(0)
    }

    @Test
    fun runs_are_numbered_in_chronological_order() {
        setContent(completedRuns = listOf(Accomplishment(august), Accomplishment(march)))

        assertEquals(
            listOf("Run 1" to "2024-03-10", "Run 2" to "2025-08-02"),
            displayedEntries()
        )
        composeTestRule.onNodeWithTag(GameHistoryTestTags.EMPTY).assertDoesNotExist()
    }

    @Test
    fun undated_runs_are_displayed_first_as_such() {
        setContent(completedRuns = listOf(Accomplishment(march), Accomplishment(null)))

        assertEquals(
            listOf("Run 1" to "Date unknown", "Run 2" to "2024-03-10"),
            displayedEntries()
        )
    }

    @Test
    fun platinum_is_displayed_in_chronological_order() {
        setContent(
            completedRuns = listOf(Accomplishment(march), Accomplishment(august)),
            platinum = Accomplishment(LocalDate(2024, 12, 1))
        )

        assertEquals(
            listOf(
                "Run 1" to "2024-03-10",
                "Platinum" to "2024-12-01",
                "Run 2" to "2025-08-02"
            ),
            displayedEntries()
        )
    }

    @Test
    fun platinum_follows_the_run_completed_on_the_same_date() {
        setContent(completedRuns = listOf(Accomplishment(august)), platinum = Accomplishment(august))

        assertEquals(
            listOf("Run 1" to "2025-08-02", "Platinum" to "2025-08-02"),
            displayedEntries()
        )
    }

    @Test
    fun undated_platinum_follows_the_undated_runs() {
        setContent(
            completedRuns = listOf(Accomplishment(march), Accomplishment(null)),
            platinum = Accomplishment(null)
        )

        assertEquals(
            listOf(
                "Run 1" to "Date unknown",
                "Platinum" to "Date unknown",
                "Run 2" to "2024-03-10"
            ),
            displayedEntries()
        )
    }
}
