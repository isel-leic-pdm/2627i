package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.PLATFORM_LIST_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.SEARCH_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class SearchForPlatformScreenViewTest {

    private companion object {
        val QUERY = SearchString("PlayStation")
        val PS5 = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Standard")
        val PS5_PRO = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Pro")
        val PS4 = PlatformInfo(4L, "PS4", "PlayStation 4", "https://fake.url/ps4.png")

        /**
         * Longer than the search box's debounce delay.
         */
        const val PAST_DEBOUNCE_TIME_MS = 1_000L
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    /**
     * Displays the view for the given state. The state is read during composition, so tests can
     * change it to drive the view through state transitions.
     */
    private fun setViewContent(
        state: MutableState<SearchForPlatformScreenState>,
        onSearchQueryChange: (SearchString) -> Unit = {},
        onSearchQueryCleared: () -> Unit = {},
        onAddPlatform: (PlatformInfo) -> Unit = {}
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        state = state.value,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchQueryCleared = onSearchQueryCleared,
                        onAddPlatform = onAddPlatform
                    )
                }
            }
        }
    }

    /**
     * Displays the view for the given (fixed) state.
     */
    private fun setViewContent(
        state: SearchForPlatformScreenState,
        onSearchQueryChange: (SearchString) -> Unit = {},
        onAddPlatform: (PlatformInfo) -> Unit = {}
    ) = setViewContent(
        state = mutableStateOf(state),
        onSearchQueryChange = onSearchQueryChange,
        onAddPlatform = onAddPlatform
    )

    private fun assertTextIsDisplayed(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
    }

    private fun assertTextDoesNotExist(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertDoesNotExist()
    }

    private fun hasEditableText(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    // Search box

    @Test
    fun search_box_is_displayed() {
        setViewContent(SearchForPlatformScreenState.Start)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
    }

    @Test
    fun search_box_text_is_preserved_across_state_transitions() {
        val state = mutableStateOf<SearchForPlatformScreenState>(SearchForPlatformScreenState.Start)
        setViewContent(state)
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput(QUERY.value)

        state.value = SearchForPlatformScreenState.Search(QUERY, isIndicatorVisible = true)
        composeTestRule.waitForIdle()
        state.value = SearchForPlatformScreenState.Results(QUERY, items = listOf(PlatformSearchItemUiState(PS4)))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
    }

    @Test
    fun search_box_displays_the_query_of_the_initial_state_without_emitting_it() {
        // e.g. when the view is displayed again, after retrying a failed search
        val emittedQueries = mutableListOf<SearchString>()
        setViewContent(
            state = SearchForPlatformScreenState.Search(QUERY),
            onSearchQueryChange = { emittedQueries += it }
        )

        composeTestRule.mainClock.advanceTimeBy(PAST_DEBOUNCE_TIME_MS)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
        assertEquals(emptyList<SearchString>(), emittedQueries)
    }

    // Start

    @Test
    fun start_state_displays_hint_only() {
        setViewContent(SearchForPlatformScreenState.Start)

        assertTextIsDisplayed(R.string.search_platform_start_hint)
        assertTextDoesNotExist(R.string.search_results_label)
        assertTextDoesNotExist(R.string.search_no_platforms_found)
        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertDoesNotExist()
    }

    // Search

    @Test
    fun search_state_without_indicator_displays_nothing_below_the_search_box() {
        setViewContent(SearchForPlatformScreenState.Search(QUERY, isIndicatorVisible = false))

        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertDoesNotExist()
        assertTextDoesNotExist(R.string.search_platform_start_hint)
        assertTextDoesNotExist(R.string.search_results_label)
        assertTextDoesNotExist(R.string.search_no_platforms_found)
    }

    @Test
    fun search_state_with_indicator_displays_the_indicator() {
        setViewContent(SearchForPlatformScreenState.Search(QUERY, isIndicatorVisible = true))

        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_platform_searching)
    }

    // Results

    @Test
    fun results_state_displays_header_and_one_item_per_platform_version() {
        setViewContent(state =
            SearchForPlatformScreenState.Results(
                QUERY,
                items = listOf(
                    PlatformSearchItemUiState(PS5),
                    PlatformSearchItemUiState(PS5_PRO),
                    PlatformSearchItemUiState(PS4)
                )
            )
        )

        assertTextIsDisplayed(R.string.search_results_label)
        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(PS5.name.value).assertCountEquals(2)
        composeTestRule.onNodeWithText(PS4.name.value).assertIsDisplayed()
        assertTextDoesNotExist(R.string.search_no_platforms_found)
    }

    @Test
    fun empty_results_state_displays_no_platforms_found_message() {
        setViewContent(SearchForPlatformScreenState.Results(QUERY, emptyList()))

        assertTextIsDisplayed(R.string.search_results_label)
        assertTextIsDisplayed(R.string.search_no_platforms_found)
        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertDoesNotExist()
    }

    @Test
    fun items_are_displayed_according_to_their_add_status() {
        setViewContent(state =
            SearchForPlatformScreenState.Results(
                QUERY,
                listOf(
                    PlatformSearchItemUiState(PS5, AddStatus.NOT_ADDED),
                    PlatformSearchItemUiState(PS5_PRO, AddStatus.ADDING),
                    PlatformSearchItemUiState(PS4, AddStatus.ADDED)
                )
            )
        )

        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADD_ICON, useUnmergedTree = true)
            .assertCountEquals(1)
        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADDING_INDICATOR)
            .assertCountEquals(1)
        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADDED_ICON, useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun selecting_a_platform_version_triggers_onAddPlatform_with_that_version() {
        var addedPlatform: PlatformInfo? = null
        setViewContent(
            state = SearchForPlatformScreenState.Results(
                query = QUERY,
                items = listOf(PlatformSearchItemUiState(PS5), PlatformSearchItemUiState(PS5_PRO))
            ),
            onAddPlatform = { addedPlatform = it }
        )

        // The version is displayed as a suffix (e.g. "· Pro") next to the abbreviation
        composeTestRule.onNodeWithText(PS5_PRO.version!!.value, substring = true).performClick()

        assertEquals(PS5_PRO, addedPlatform)
    }

    @Test
    fun selecting_an_added_platform_does_not_trigger_onAddPlatform() {
        var addedPlatform: PlatformInfo? = null
        setViewContent(
            state = SearchForPlatformScreenState.Results(QUERY, listOf(PlatformSearchItemUiState(PS4, AddStatus.ADDED))),
            onAddPlatform = { addedPlatform = it }
        )

        composeTestRule.onNodeWithText(PS4.name.value).performClick()

        assertNull(addedPlatform)
    }
}
