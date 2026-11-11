package isel.dei.pdm.demos.gamevault.search.games

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.ADD_PLATFORM_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.CATEGORY_FILTER_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.GAME_LIST_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.PLATFORM_FILTER_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.SEARCH_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SearchForGamesScreenViewTest {

    private companion object {
        val QUERY = SearchString("Zelda")

        val PS5 = Platform(1L, "PS5", "PlayStation 5")
        val PC = Platform(2L, "PC", "PC (Microsoft Windows)")
        val SWITCH = Platform(3L, "Switch", "Nintendo Switch")

        val FILTERS = GameSearchFilters(
            platforms = listOf(PS5, PC, SWITCH),
            selectedPlatform = SWITCH
        )

        val TOTK = GameInfo(1L, "Tears of the Kingdom", LocalDate.of(2023, 5, 12))
        val BOTW = GameInfo(2L, "Breath of the Wild", LocalDate.of(2017, 3, 3))
        val ECHOES = GameInfo(3L, "Echoes of Wisdom", LocalDate.of(2024, 9, 26))

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
        state: MutableState<SearchForGamesScreenState>,
        onSearchQueryChange: (SearchString) -> Unit = {},
        onSearchQueryCleared: () -> Unit = {},
        onPlatformSelected: (Platform?) -> Unit = {},
        onCategorySelected: (GameInfo.Category?) -> Unit = {},
        onAddGame: (GameInfo, Platform) -> Unit = { _, _ -> },
        onAddPlatform: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForGamesScreenView(
                        state = state.value,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchQueryCleared = onSearchQueryCleared,
                        onPlatformSelected = onPlatformSelected,
                        onCategorySelected = onCategorySelected,
                        onAddGame = onAddGame,
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
        state: SearchForGamesScreenState,
        onSearchQueryChange: (SearchString) -> Unit = {},
        onPlatformSelected: (Platform?) -> Unit = {},
        onCategorySelected: (GameInfo.Category?) -> Unit = {},
        onAddGame: (GameInfo, Platform) -> Unit = { _, _ -> },
        onAddPlatform: () -> Unit = {}
    ) = setViewContent(
        state = mutableStateOf(state),
        onSearchQueryChange = onSearchQueryChange,
        onPlatformSelected = onPlatformSelected,
        onCategorySelected = onCategorySelected,
        onAddGame = onAddGame,
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

    /**
     * The chip with the given label, in the filter with the given tag. Chips are scrolled into
     * view, as the filters scroll horizontally when they do not fit.
     */
    private fun chip(filterTag: String, label: String): SemanticsNodeInteraction =
        composeTestRule
            .onNode(hasText(label) and hasAnyAncestor(hasTestTag(filterTag)))
            .performScrollTo()

    private fun platformChip(label: String) = chip(PLATFORM_FILTER_TAG, label)

    /**
     * Matches the result item displaying the given platform, as opposed to the platform's chip.
     */
    private fun itemOn(platformLabel: String): SemanticsMatcher =
        hasTestTag(GameSearchItemViewTestTags.ITEM) and hasText(platformLabel)

    private fun categoryChip(resId: Int) = chip(CATEGORY_FILTER_TAG, context.getString(resId))

    // Search box

    @Test
    fun search_box_is_displayed() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS))

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
    }

    @Test
    fun search_box_text_is_preserved_across_state_transitions() {
        val state = mutableStateOf<SearchForGamesScreenState>(SearchForGamesScreenState.Start(FILTERS))
        setViewContent(state)
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput(QUERY.value)

        state.value = SearchForGamesScreenState.Search(QUERY, FILTERS, isIndicatorVisible = true)
        composeTestRule.waitForIdle()
        state.value = SearchForGamesScreenState.Results(QUERY, FILTERS, listOf(GameSearchItemUiState(TOTK, SWITCH)))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
    }

    @Test
    fun search_box_displays_the_query_of_the_initial_state_without_emitting_it() {
        // e.g. when the view is displayed again, after retrying a failed search
        val emittedQueries = mutableListOf<SearchString>()
        setViewContent(
            state = SearchForGamesScreenState.Search(QUERY, FILTERS),
            onSearchQueryChange = { emittedQueries += it }
        )

        composeTestRule.mainClock.advanceTimeBy(PAST_DEBOUNCE_TIME_MS)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
        assertEquals(emptyList<SearchString>(), emittedQueries)
    }

    // Loading & No platforms

    @Test
    fun loading_state_displays_nothing() {
        setViewContent(SearchForGamesScreenState.Loading)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PLATFORM_FILTER_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CATEGORY_FILTER_TAG).assertDoesNotExist()
        assertTextDoesNotExist(R.string.search_game_no_platforms)
        assertTextDoesNotExist(R.string.search_game_start_hint)
    }

    @Test
    fun platforms_unavailable_state_displays_nothing() {
        // Displayed by the screen, in place of this view
        setViewContent(SearchForGamesScreenState.PlatformsUnavailable(UnrecoverablePersistenceException()))

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PLATFORM_FILTER_TAG).assertDoesNotExist()
        assertTextDoesNotExist(R.string.search_game_no_platforms)
    }

    @Test
    fun no_platforms_state_displays_message_and_add_platform_button_only() {
        setViewContent(SearchForGamesScreenState.NoPlatforms)

        assertTextIsDisplayed(R.string.search_game_no_platforms)
        composeTestRule.onNodeWithTag(ADD_PLATFORM_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PLATFORM_FILTER_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CATEGORY_FILTER_TAG).assertDoesNotExist()
    }

    @Test
    fun clicking_add_platform_button_triggers_onAddPlatform() {
        var addPlatformClicked = false
        setViewContent(SearchForGamesScreenState.NoPlatforms, onAddPlatform = { addPlatformClicked = true })

        composeTestRule.onNodeWithTag(ADD_PLATFORM_BUTTON_TAG).performClick()

        assertTrue(addPlatformClicked)
    }

    @Test
    fun add_platform_button_is_only_displayed_when_there_are_no_platforms() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS))

        composeTestRule.onNodeWithTag(ADD_PLATFORM_BUTTON_TAG).assertDoesNotExist()
    }

    // Filters

    @Test
    fun platform_filter_displays_the_collection_platforms_with_the_selected_one_selected() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS))

        platformChip(context.getString(R.string.search_game_platform_all)).assertIsNotSelected()
        platformChip("PS5").assertIsNotSelected()
        platformChip("PC").assertIsNotSelected()
        platformChip("Switch").assertIsSelected()
    }

    @Test
    fun platform_chips_do_not_display_the_platforms_versions() {
        // Games are oblivious of versions, so the platform is searched through any of them
        val ps5Pro = PS5.copy(version = NonBlankString("Pro"))
        setViewContent(SearchForGamesScreenState.Start(GameSearchFilters(listOf(ps5Pro, SWITCH))))

        platformChip("PS5").assertIsNotSelected()
    }

    @Test
    fun all_platforms_is_selected_when_there_is_no_selected_platform() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS.copy(selectedPlatform = null)))

        platformChip(context.getString(R.string.search_game_platform_all)).assertIsSelected()
        platformChip("PS5").assertIsNotSelected()
        platformChip("PC").assertIsNotSelected()
        platformChip("Switch").assertIsNotSelected()
    }

    @Test
    fun category_filter_displays_any_and_each_category_with_the_selected_one_selected() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS.copy(category = GameInfo.Category.DLC)))

        categoryChip(R.string.search_game_category_any).assertIsNotSelected()
        categoryChip(R.string.search_game_category_main_game).assertIsNotSelected()
        categoryChip(R.string.search_game_category_dlc).assertIsSelected()
        categoryChip(R.string.search_game_category_bundle).assertIsNotSelected()
        categoryChip(R.string.search_game_category_remake).assertIsNotSelected()
        categoryChip(R.string.search_game_category_remaster).assertIsNotSelected()
    }

    @Test
    fun any_category_is_selected_when_there_is_no_category() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS.copy(category = null)))

        categoryChip(R.string.search_game_category_any).assertIsSelected()
    }

    @Test
    fun filters_are_displayed_in_every_search_state() {
        val states = listOf(
            SearchForGamesScreenState.Start(FILTERS),
            SearchForGamesScreenState.Search(QUERY, FILTERS, isIndicatorVisible = true),
            SearchForGamesScreenState.Results(QUERY, FILTERS, listOf(GameSearchItemUiState(TOTK, SWITCH)))
        )
        val state = mutableStateOf<SearchForGamesScreenState>(states.first())
        setViewContent(state)

        states.forEach {
            state.value = it
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag(PLATFORM_FILTER_TAG).assertIsDisplayed()
            composeTestRule.onNodeWithTag(CATEGORY_FILTER_TAG).assertIsDisplayed()
        }
    }

    @Test
    fun selecting_a_platform_triggers_onPlatformSelected_with_that_platform() {
        var selectedPlatform: Platform? = null
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS),
            onPlatformSelected = { selectedPlatform = it }
        )

        platformChip("PC").performClick()

        assertEquals(PC, selectedPlatform)
    }

    @Test
    fun selecting_all_platforms_triggers_onPlatformSelected_with_null() {
        val selectedPlatforms = mutableListOf<Platform?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS),
            onPlatformSelected = { selectedPlatforms += it }
        )

        platformChip(context.getString(R.string.search_game_platform_all)).performClick()

        assertEquals(listOf<Platform?>(null), selectedPlatforms)
    }

    @Test
    fun selecting_the_selected_platform_does_not_trigger_onPlatformSelected() {
        // null is a valid selection, so the calls are recorded instead
        val selectedPlatforms = mutableListOf<Platform?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS),
            onPlatformSelected = { selectedPlatforms += it }
        )

        platformChip("Switch").performClick()

        assertEquals(emptyList<Platform?>(), selectedPlatforms)
    }

    @Test
    fun selecting_all_platforms_when_already_selected_does_not_trigger_onPlatformSelected() {
        val selectedPlatforms = mutableListOf<Platform?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS.copy(selectedPlatform = null)),
            onPlatformSelected = { selectedPlatforms += it }
        )

        platformChip(context.getString(R.string.search_game_platform_all)).performClick()

        assertEquals(emptyList<Platform?>(), selectedPlatforms)
    }

    @Test
    fun selecting_a_category_triggers_onCategorySelected_with_that_category() {
        val selectedCategories = mutableListOf<GameInfo.Category?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS.copy(category = null)),
            onCategorySelected = { selectedCategories += it }
        )

        categoryChip(R.string.search_game_category_remaster).performClick()

        assertEquals(listOf(GameInfo.Category.REMASTER), selectedCategories)
    }

    @Test
    fun selecting_any_category_triggers_onCategorySelected_with_null() {
        val selectedCategories = mutableListOf<GameInfo.Category?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS.copy(category = GameInfo.Category.DLC)),
            onCategorySelected = { selectedCategories += it }
        )

        categoryChip(R.string.search_game_category_any).performClick()

        assertEquals(listOf<GameInfo.Category?>(null), selectedCategories)
    }

    @Test
    fun selecting_the_selected_category_does_not_trigger_onCategorySelected() {
        val selectedCategories = mutableListOf<GameInfo.Category?>()
        setViewContent(
            state = SearchForGamesScreenState.Start(FILTERS.copy(category = GameInfo.Category.DLC)),
            onCategorySelected = { selectedCategories += it }
        )

        categoryChip(R.string.search_game_category_dlc).performClick()

        assertEquals(emptyList<GameInfo.Category?>(), selectedCategories)
    }

    // Start

    @Test
    fun start_state_displays_hint_only() {
        setViewContent(SearchForGamesScreenState.Start(FILTERS))

        assertTextIsDisplayed(R.string.search_game_start_hint)
        assertTextDoesNotExist(R.string.search_game_results_label)
        assertTextDoesNotExist(R.string.search_no_games_found)
        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertDoesNotExist()
    }

    // Search

    @Test
    fun search_state_without_indicator_displays_nothing_below_the_filters() {
        setViewContent(SearchForGamesScreenState.Search(QUERY, FILTERS, isIndicatorVisible = false))

        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertDoesNotExist()
        assertTextDoesNotExist(R.string.search_game_start_hint)
        assertTextDoesNotExist(R.string.search_game_results_label)
        assertTextDoesNotExist(R.string.search_no_games_found)
    }

    @Test
    fun search_state_with_indicator_displays_the_indicator() {
        setViewContent(SearchForGamesScreenState.Search(QUERY, FILTERS, isIndicatorVisible = true))

        composeTestRule.onNodeWithTag(SEARCH_INDICATOR_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_game_searching)
    }

    // Results

    @Test
    fun results_state_displays_header_and_one_item_per_game() {
        setViewContent(
            SearchForGamesScreenState.Results(
                QUERY,
                FILTERS,
                items = listOf(
                    GameSearchItemUiState(TOTK, SWITCH),
                    GameSearchItemUiState(BOTW, SWITCH),
                    GameSearchItemUiState(ECHOES, SWITCH)
                )
            )
        )

        assertTextIsDisplayed(R.string.search_game_results_label)
        composeTestRule.onNodeWithTag(GAME_LIST_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(GameSearchItemViewTestTags.ITEM).assertCountEquals(3)
        composeTestRule.onNodeWithText(TOTK.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithText(BOTW.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithText(ECHOES.name.value).assertIsDisplayed()
        assertTextDoesNotExist(R.string.search_no_games_found)
    }

    @Test
    fun results_state_displays_one_item_per_platform_of_the_same_game() {
        setViewContent(
            SearchForGamesScreenState.Results(
                QUERY,
                FILTERS.copy(selectedPlatform = null),
                items = listOf(
                    GameSearchItemUiState(TOTK, PS5),
                    GameSearchItemUiState(TOTK, PC),
                    GameSearchItemUiState(TOTK, SWITCH)
                )
            )
        )

        composeTestRule.onAllNodesWithTag(GameSearchItemViewTestTags.ITEM).assertCountEquals(3)
        composeTestRule.onNode(itemOn("PS5")).assertIsDisplayed()
        composeTestRule.onNode(itemOn("PC")).assertIsDisplayed()
        composeTestRule.onNode(itemOn("Switch")).assertIsDisplayed()
    }

    @Test
    fun empty_results_state_displays_no_games_found_message() {
        setViewContent(SearchForGamesScreenState.Results(QUERY, FILTERS, emptyList()))

        assertTextIsDisplayed(R.string.search_game_results_label)
        assertTextIsDisplayed(R.string.search_no_games_found)
        composeTestRule.onNodeWithTag(GAME_LIST_TAG).assertDoesNotExist()
    }

    @Test
    fun items_are_displayed_according_to_their_add_status() {
        setViewContent(
            SearchForGamesScreenState.Results(
                QUERY,
                FILTERS,
                listOf(
                    GameSearchItemUiState(TOTK, SWITCH, AddStatus.NOT_ADDED),
                    GameSearchItemUiState(BOTW, SWITCH, AddStatus.ADDING),
                    GameSearchItemUiState(ECHOES, SWITCH, AddStatus.ADDED)
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
    fun selecting_a_game_triggers_onAddGame_with_that_game_and_platform() {
        var added: Pair<GameInfo, Platform>? = null
        setViewContent(
            state = SearchForGamesScreenState.Results(
                QUERY,
                FILTERS,
                listOf(GameSearchItemUiState(TOTK, SWITCH), GameSearchItemUiState(BOTW, SWITCH))
            ),
            onAddGame = { game, platform -> added = game to platform }
        )

        composeTestRule.onNodeWithText(BOTW.name.value).performClick()

        assertEquals(BOTW to SWITCH, added)
    }

    @Test
    fun selecting_a_game_on_a_platform_triggers_onAddGame_with_that_platform() {
        var added: Pair<GameInfo, Platform>? = null
        setViewContent(
            state = SearchForGamesScreenState.Results(
                QUERY,
                FILTERS.copy(selectedPlatform = null),
                listOf(GameSearchItemUiState(TOTK, PS5), GameSearchItemUiState(TOTK, PC))
            ),
            onAddGame = { game, platform -> added = game to platform }
        )

        composeTestRule.onNode(itemOn("PC")).performClick()

        assertEquals(TOTK to PC, added)
    }

    @Test
    fun selecting_an_added_game_does_not_trigger_onAddGame() {
        var added: Pair<GameInfo, Platform>? = null
        setViewContent(
            state = SearchForGamesScreenState.Results(
                QUERY,
                FILTERS,
                listOf(GameSearchItemUiState(TOTK, SWITCH, AddStatus.ADDED))
            ),
            onAddGame = { game, platform -> added = game to platform }
        )

        composeTestRule.onNodeWithText(TOTK.name.value).performClick()

        assertNull(added)
    }
}
