package isel.dei.pdm.demos.gamevault.collection.games

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CollectionListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Items have no images, so nothing is loaded; the loader is provided just in case.
     */
    private val noImageLoader: ImageLoader = { Result.failure(IllegalStateException()) }

    private var nextId = 1L

    private fun item(name: String) = CollectionItem(
        gameId = nextId++,
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

    /**
     * Items for the given initials, [perInitial] of each, in the order of a repository sorting by
     * name.
     */
    private fun itemsFor(initials: Iterable<Char>, perInitial: Int): List<CollectionItem> =
        initials.flatMap { initial -> (1..perInitial).map { item("$initial Game $it") } }

    /**
     * A listing spanning many screens, with an initial per letter (A to Z).
     */
    private fun longListing() = itemsFor('A'..'Z', perInitial = 3)

    /**
     * The items selected by the user, in the order they were selected.
     */
    private val selected = mutableListOf<CollectionItem>()

    private fun setListContent(items: List<CollectionItem>) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides noImageLoader) {
                DemoGameVaultTheme {
                    CollectionList(sections = items.toSections(), onItemSelected = { selected += it })
                }
            }
        }
    }

    private fun header(initial: Char) = composeTestRule.onNode(
        hasTestTag(CollectionListTestTags.HEADER) and hasAnyDescendant(hasText(initial.toString()))
    )

    private fun index() = composeTestRule.onNodeWithTag(InitialsIndexTestTags.INDEX)

    // Display

    @Test
    fun nothing_is_displayed_without_sections() {
        setListContent(emptyList())

        composeTestRule.onNodeWithTag(CollectionListTestTags.LIST).assertDoesNotExist()
    }

    @Test
    fun items_are_displayed_under_the_headers_of_their_initials() {
        setListContent(listOf(item("Celeste"), item("Control"), item("Hades")))

        header('C').assertIsDisplayed()
        header('H').assertIsDisplayed()
        listOf("Celeste", "Control", "Hades").forEach {
            composeTestRule.onNodeWithText(it).assertIsDisplayed()
        }
    }

    @Test
    fun selecting_an_item_triggers_onItemSelected_with_it() {
        val hades = item("Hades")
        setListContent(listOf(item("Celeste"), hades))

        composeTestRule.onNodeWithText("Hades").performClick()

        assertEquals(listOf(hades), selected)
    }

    // Initials index

    @Test
    fun index_is_not_displayed_for_short_listings() {
        setListContent(itemsFor('A'..'F', perInitial = 1))
        composeTestRule.waitForIdle()

        index().assertDoesNotExist()
    }

    @Test
    fun index_is_not_displayed_for_long_listings_with_few_initials() {
        setListContent(itemsFor('A'..'D', perInitial = 20))
        composeTestRule.waitForIdle()

        index().assertDoesNotExist()
    }

    @Test
    fun index_is_displayed_for_long_listings_with_enough_initials() {
        setListContent(longListing())

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(InitialsIndexTestTags.INDEX))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        index().assertIsDisplayed()
    }

    @Test
    fun selecting_an_initial_of_the_index_jumps_to_its_section() {
        setListContent(longListing())
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasTestTag(InitialsIndexTestTags.INDEX))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        val initial = 'T'
        composeTestRule
            .onNode(hasTestTag(InitialsIndexTestTags.INITIAL) and hasText(initial.toString()))
            .performClick()

        header(initial).assertIsDisplayed()
        composeTestRule.onNodeWithText("T Game 1").assertIsDisplayed()
        assertTrue(selected.isEmpty())
    }

    // Empty view

    private fun setEmptyContent(isFiltered: Boolean, onClearFilters: () -> Unit = {}) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                CollectionEmptyView(isFiltered = isFiltered, onClearFilters = onClearFilters)
            }
        }
    }

    @Test
    fun empty_collection_is_explained_without_offering_to_clear_filters() {
        setEmptyContent(isFiltered = false)

        composeTestRule.onNodeWithText("Your collection has no games yet.").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(CollectionListTestTags.CLEAR_FILTERS_BUTTON)
            .assertDoesNotExist()
    }

    @Test
    fun filtered_listing_without_matches_offers_to_clear_the_filters() {
        var clears = 0
        setEmptyContent(isFiltered = true, onClearFilters = { clears++ })

        composeTestRule
            .onNodeWithText("No games match the current search and filters.")
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CollectionListTestTags.CLEAR_FILTERS_BUTTON).performClick()

        assertEquals(1, clears)
    }
}
