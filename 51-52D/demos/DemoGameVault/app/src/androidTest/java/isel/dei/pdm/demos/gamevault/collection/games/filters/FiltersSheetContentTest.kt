package isel.dei.pdm.demos.gamevault.collection.games.filters

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FiltersSheetContentTest {

    private companion object {
        const val STATUS = "Status"
        const val COMPLETION = "Completion"
        const val PLATFORM = "Platform"

        val PS5 = Platform(1L, "PS5", "PlayStation 5")
        val SWITCH = Platform(2L, "Switch", "Nintendo Switch")
        val PLATFORMS = listOf(PS5, SWITCH)
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val selectedStates = mutableListOf<PlayStatus.State?>()
    private val selectedCompletions = mutableListOf<Completion?>()
    private val selectedPlatforms = mutableListOf<Platform?>()

    private fun setContent(
        state: PlayStatus.State? = null,
        completion: Completion? = null,
        platform: Platform? = null,
        platforms: List<Platform> = PLATFORMS
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                FiltersSheetContent(
                    state = state,
                    completion = completion,
                    platform = platform,
                    platforms = platforms,
                    onStateSelected = { selectedStates += it },
                    onCompletionSelected = { selectedCompletions += it },
                    onPlatformSelected = { selectedPlatforms += it }
                )
            }
        }
    }

    /**
     * The section with the given title. Sections are told apart by their titles, as they all
     * share the same test tag.
     */
    private fun sectionWithTitle(title: String) =
        hasTestTag(FilterSectionTestTags.SECTION) and hasAnyDescendant(hasText(title))

    /**
     * The option with the given label in the section with the given title. Needed because some
     * labels are in several sections (e.g. Any).
     */
    private fun option(section: String, label: String): SemanticsNodeInteraction =
        composeTestRule
            .onNode(
                hasTestTag(FilterSectionTestTags.OPTION) and hasText(label) and
                    hasAnyAncestor(sectionWithTitle(section))
            )
            .performScrollTo()

    private fun optionDoesNotExist(section: String, label: String) =
        composeTestRule
            .onNode(
                hasTestTag(FilterSectionTestTags.OPTION) and hasText(label) and
                    hasAnyAncestor(sectionWithTitle(section))
            )
            .assertDoesNotExist()

    private fun section(title: String) = composeTestRule.onNode(sectionWithTitle(title))

    private fun assertNothingSelected() {
        assertEquals(emptyList<PlayStatus.State?>(), selectedStates)
        assertEquals(emptyList<Completion?>(), selectedCompletions)
        assertEquals(emptyList<Platform?>(), selectedPlatforms)
    }

    // Display

    @Test
    fun title_and_the_status_completion_and_platform_sections_are_displayed() {
        setContent()

        composeTestRule.onNodeWithText("Filters").assertIsDisplayed()
        listOf(STATUS, COMPLETION, PLATFORM).forEach {
            section(it).performScrollTo().assertIsDisplayed()
        }
        composeTestRule.onAllNodesWithTag(FilterSectionTestTags.SECTION).assertCountEquals(3)
    }

    @Test
    fun status_section_offers_any_and_the_play_states_but_finished() {
        setContent()

        listOf("Any", "None", "Backlog", "Playing", "Paused", "Dropped")
            .forEach { option(STATUS, it).assertIsDisplayed() }
        optionDoesNotExist(STATUS, "Finished")
    }

    @Test
    fun completion_section_offers_any_not_finished_finished_and_platinum() {
        setContent()

        listOf("Any", "Not finished", "Finished", "Platinum")
            .forEach { option(COMPLETION, it).assertIsDisplayed() }
    }

    @Test
    fun platform_section_offers_any_and_the_platforms() {
        setContent()

        listOf("Any", "PS5", "Switch").forEach { option(PLATFORM, it).assertIsDisplayed() }
    }

    @Test
    fun any_is_selected_in_every_section_without_filters() {
        setContent()

        listOf(STATUS, COMPLETION, PLATFORM).forEach { option(it, "Any").assertIsSelected() }
    }

    @Test
    fun status_and_completion_are_selected_together() {
        setContent(
            state = PlayStatus.State.PLAYING,
            completion = Completion.FINISHED,
            platform = PS5
        )

        option(STATUS, "Playing").assertIsSelected()
        option(COMPLETION, "Finished").assertIsSelected()
        option(PLATFORM, "PS5").assertIsSelected()
        listOf(STATUS, COMPLETION, PLATFORM).forEach { option(it, "Any").assertIsNotSelected() }
    }

    @Test
    fun only_the_selected_completion_is_selected() {
        setContent(completion = Completion.PLATINUM)

        option(COMPLETION, "Platinum").assertIsSelected()
        listOf("Any", "Not finished", "Finished")
            .forEach { option(COMPLETION, it).assertIsNotSelected() }
    }

    @Test
    fun no_status_option_is_selected_for_the_finished_play_state() {
        setContent(state = PlayStatus.State.FINISHED)

        listOf("Any", "Playing").forEach { option(STATUS, it).assertIsNotSelected() }
    }

    // Platform section

    @Test
    fun platform_section_is_not_displayed_without_platforms_nor_a_selected_platform() {
        setContent(platforms = emptyList())

        section(PLATFORM).assertDoesNotExist()
    }

    @Test
    fun platform_section_is_displayed_without_platforms_when_a_platform_is_selected() {
        setContent(platform = PS5, platforms = emptyList())

        option(PLATFORM, "Any").assertIsNotSelected()
    }

    @Test
    fun selecting_any_platform_removes_a_selected_platform_that_is_not_offered() {
        setContent(platform = PS5, platforms = listOf(SWITCH))

        option(PLATFORM, "Any").performClick()

        assertEquals(listOf<Platform?>(null), selectedPlatforms)
    }

    // Interaction

    @Test
    fun selecting_a_state_triggers_onStateSelected_only() {
        setContent()

        option(STATUS, "Backlog").performClick()

        assertEquals(listOf<PlayStatus.State?>(PlayStatus.State.BACKLOG), selectedStates)
        assertEquals(emptyList<Completion?>(), selectedCompletions)
        assertEquals(emptyList<Platform?>(), selectedPlatforms)
    }

    @Test
    fun selecting_any_state_triggers_onStateSelected_with_null() {
        setContent(state = PlayStatus.State.PLAYING)

        option(STATUS, "Any").performClick()

        assertEquals(listOf<PlayStatus.State?>(null), selectedStates)
    }

    @Test
    fun selecting_a_completion_triggers_onCompletionSelected_only() {
        setContent(state = PlayStatus.State.PLAYING)

        option(COMPLETION, "Not finished").performClick()

        assertEquals(listOf<Completion?>(Completion.NOT_FINISHED), selectedCompletions)
        assertEquals(emptyList<PlayStatus.State?>(), selectedStates)
        assertEquals(emptyList<Platform?>(), selectedPlatforms)
    }

    @Test
    fun selecting_platinum_while_filtering_by_finished_triggers_onCompletionSelected_with_it() {
        setContent(completion = Completion.FINISHED)

        option(COMPLETION, "Platinum").performClick()

        assertEquals(listOf<Completion?>(Completion.PLATINUM), selectedCompletions)
    }

    @Test
    fun selecting_any_completion_triggers_onCompletionSelected_with_null() {
        setContent(completion = Completion.PLATINUM)

        option(COMPLETION, "Any").performClick()

        assertEquals(listOf<Completion?>(null), selectedCompletions)
    }

    @Test
    fun selecting_a_platform_triggers_onPlatformSelected_only() {
        setContent()

        option(PLATFORM, "Switch").performClick()

        assertEquals(listOf<Platform?>(SWITCH), selectedPlatforms)
        assertEquals(emptyList<PlayStatus.State?>(), selectedStates)
        assertEquals(emptyList<Completion?>(), selectedCompletions)
    }

    @Test
    fun selecting_the_selected_options_triggers_nothing() {
        setContent(
            state = PlayStatus.State.PLAYING,
            completion = Completion.PLATINUM,
            platform = PS5
        )

        option(STATUS, "Playing").performClick()
        option(COMPLETION, "Platinum").performClick()
        option(PLATFORM, "PS5").performClick()

        assertNothingSelected()
    }
}
