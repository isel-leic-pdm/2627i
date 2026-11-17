package isel.dei.pdm.demos.gamevault.collection.games

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.filters.ActiveFiltersRowTestTags
import isel.dei.pdm.demos.gamevault.collection.games.filters.FilterSectionTestTags
import isel.dei.pdm.demos.gamevault.collection.games.filters.FiltersSheetContentTestTags
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [GameCollectionScreenView]: how it composes its parts, and how the user's changes
 * are emitted as whole queries. The parts themselves are covered by their own tests.
 */
class GameCollectionScreenViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")

    private fun item(id: Long, name: String) = CollectionItem(
        gameId = id,
        name = NonBlankString(name),
        releaseYear = null,
        coverUri = null,
        thumbnailUri = null,
        platformAbbreviations = listOf(NonBlankString("PS5")),
        state = PlayStatus.State.BACKLOG,
        timeSpent = PlayTime(),
        isFinished = false,
        hasPlatinum = false,
        isSessionActive = false
    )

    private val sections = listOf(item(1L, "Celeste"), item(2L, "Hades")).toSections()

    /**
     * The queries emitted by the view, in the order they were emitted.
     */
    private val emitted = mutableListOf<CollectionQuery>()

    /**
     * Displays the view with the given inputs. The emitted queries are recorded, but not applied
     * (i.e. the view keeps displaying the given [query]).
     */
    private fun setViewContent(
        query: CollectionQuery = CollectionQuery(),
        sections: List<CollectionSection>? = this.sections,
        isFiltersSheetOpen: Boolean = false
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameCollectionScreenView(
                    query = query,
                    sections = sections,
                    platforms = listOf(ps5),
                    isFiltersSheetOpen = isFiltersSheetOpen,
                    onQueryChange = { emitted += it },
                    onItemSelected = {},
                    onFiltersSheetDismissed = {}
                )
            }
        }
    }

    private fun clearAllButton() =
        composeTestRule.onNodeWithText(context.getString(R.string.collection_clear_filters_label))

    // Display

    @Test
    fun search_box_and_games_are_displayed() {
        setViewContent()

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hades").assertIsDisplayed()
    }

    @Test
    fun without_filters_no_chips_are_displayed() {
        setViewContent()

        composeTestRule.onNodeWithTag(ActiveFiltersRowTestTags.ROW).assertDoesNotExist()
    }

    @Test
    fun loading_indicator_is_displayed_while_games_are_not_read() {
        setViewContent(sections = null)

        composeTestRule
            .onNodeWithTag(GameCollectionScreenViewTestTags.LOADING_INDICATOR)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
    }

    @Test
    fun empty_collection_does_not_offer_to_clear_the_filters() {
        setViewContent(sections = emptyList())

        composeTestRule.onNodeWithText(context.getString(R.string.collection_empty)).assertIsDisplayed()
        clearAllButton().assertDoesNotExist()
    }

    // Query changes

    @Test
    fun removing_a_chip_emits_the_query_without_its_filter() {
        setViewContent(
            query = CollectionQuery(state = PlayStatus.State.PLAYING, completion = Completion.FINISHED)
        )

        composeTestRule
            .onNode(hasTestTag(ActiveFiltersRowTestTags.CHIP) and hasText("Playing"))
            .performClick()

        assertEquals(listOf(CollectionQuery(completion = Completion.FINISHED)), emitted)
    }

    @Test
    fun clearing_all_without_matches_emits_the_query_without_criteria() {
        setViewContent(
            query = CollectionQuery(completion = Completion.PLATINUM, platform = ps5),
            sections = emptyList()
        )

        clearAllButton().performClick()

        assertEquals(listOf(CollectionQuery()), emitted)
    }

    @Test
    fun searching_emits_the_query_with_the_text_searched_for() {
        setViewContent(query = CollectionQuery(state = PlayStatus.State.BACKLOG))

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput("hades")
        composeTestRule.waitUntil(timeoutMillis = 5_000) { emitted.isNotEmpty() }

        assertEquals(
            listOf(CollectionQuery(state = PlayStatus.State.BACKLOG, nameContains = NonBlankString("hades"))),
            emitted
        )
    }

    @Test
    fun clearing_all_also_empties_the_search_box() {
        // The query is applied, as the screen does, so that the view reflects the cleared query
        composeTestRule.setContent {
            var query by remember {
                mutableStateOf(CollectionQuery(nameContains = NonBlankString("zelda")))
            }
            DemoGameVaultTheme {
                GameCollectionScreenView(
                    query = query,
                    sections = emptyList(),
                    platforms = emptyList(),
                    isFiltersSheetOpen = false,
                    onQueryChange = { query = it },
                    onItemSelected = {},
                    onFiltersSheetDismissed = {}
                )
            }
        }
        composeTestRule.onNodeWithText("zelda").assertIsDisplayed()

        clearAllButton().performClick()

        composeTestRule.onNodeWithText("zelda").assertDoesNotExist()
        composeTestRule.onNodeWithText(context.getString(R.string.collection_empty)).assertIsDisplayed()
    }

    // Filters sheet

    @Test
    fun filters_sheet_is_displayed_only_when_open() {
        setViewContent(isFiltersSheetOpen = false)

        composeTestRule.onNodeWithTag(FiltersSheetContentTestTags.CONTENT).assertDoesNotExist()
    }

    @Test
    fun selecting_an_option_in_the_filters_sheet_emits_the_query_with_it() {
        setViewContent(
            query = CollectionQuery(completion = Completion.FINISHED),
            isFiltersSheetOpen = true
        )

        composeTestRule
            .onNode(hasTestTag(FilterSectionTestTags.OPTION) and hasText("Playing"))
            .performClick()

        assertEquals(
            listOf(CollectionQuery(state = PlayStatus.State.PLAYING, completion = Completion.FINISHED)),
            emitted
        )
    }
}
