package isel.dei.pdm.demos.gamevault.search.games

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.core.graphics.createBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeImageStore
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GamePlatform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.ADD_PLATFORM_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.PLATFORM_FILTER_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.QUERY_KEY
import isel.dei.pdm.demos.gamevault.search.platforms.ScriptedPlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [SearchForGamesScreen], with an actual view model whose dependencies are scripted
 * fakes. Besides the screen's own responsibilities (the top bar, the choice between the search
 * view and the error views, and the snackbar), they check that the screen is wired to the view
 * model. The state of each screen is covered in detail by [SearchForGamesScreenViewTest].
 */
class SearchForGamesScreenTest {

    private companion object {
        val QUERY = SearchString("Elden Ring")

        val PS5 = Platform(1L, "PS5", "PlayStation 5", source = igdb("167"))
        val PC = Platform(2L, "PC", "PC (Microsoft Windows)", source = igdb("6"))

        val ELDEN_RING = GameInfo(
            id = 1L,
            name = "Elden Ring",
            platforms = listOf(gamePlatform(167L, "PlayStation 5", "PS5"), gamePlatform(6L, "PC", "PC"))
        )

        /**
         * Enough for the snackbar to be displayed, but not dismissed.
         */
        const val SNACKBAR_SHOW_TIME_MS = 500L

        /**
         * Longer than the search box's debounce delay.
         */
        const val PAST_DEBOUNCE_TIME_MS = 1_000L

        /**
         * How long to wait for asynchronous work driven by the real clock (e.g. the view model).
         */
        const val WAIT_TIMEOUT_MS = 5_000L

        fun igdb(id: String) = SourceReference(SourceReference.Provider.IGDB, NonBlankString(id))

        fun gamePlatform(id: Long, name: String, abbreviation: String) =
            GamePlatform(id, NonBlankString(name), NonBlankString(abbreviation))

        fun results(vararg games: GameInfo): Result<List<GameInfo>> = Result.success(games.toList())

        fun failure(error: SearchServiceException): Result<List<GameInfo>> = Result.failure(error)
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
     * Displays the screen, with a view model that uses the given dependencies.
     * @param initialQuery If not null, the query preserved in the view model's saved state. The
     * view model resumes its search when created (as it does after process death), which takes
     * the screen to Results or Error without having to type the query.
     */
    private fun setScreenContent(
        searchService: GameSearchService = ScriptedGameSearchService(listOf(results())),
        platformRepo: PlatformCollectionRepository =
            ScriptedPlatformCollectionRepository(initialPlatforms = listOf(PS5, PC)),
        gameRepo: GameCollectionRepository = ScriptedGameCollectionRepository(),
        initialQuery: SearchString? = null,
        onAddPlatform: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Created on the main thread, as the view model (and the fake repository) expects
            val viewModel = remember {
                val savedState = initialQuery?.let { mapOf(QUERY_KEY to it.value) } ?: emptyMap()
                SearchForGamesViewModel(
                    searchService, gameRepo, platformRepo, FakeImageStore(), SavedStateHandle(savedState)
                )
            }
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForGamesScreen(
                        viewModel = viewModel,
                        onAddPlatform = onAddPlatform,
                        onBack = onBack
                    )
                }
            }
        }
    }

    private fun textExists(text: String): Boolean =
        composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun assertTextIsDisplayed(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
    }

    private fun clickRetry() {
        composeTestRule
            .onNodeWithText(context.getString(R.string.search_game_retry_button_label))
            .performClick()
    }

    private fun hasEditableText(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    /**
     * Matches the result item displaying the given platform, as opposed to the platform's chip.
     */
    private fun itemOn(platformLabel: String): SemanticsMatcher =
        hasTestTag(GameSearchItemViewTestTags.ITEM) and hasText(platformLabel)

    // Top bar

    @Test
    fun screen_displays_title_back_button_search_box_and_filters() {
        setScreenContent()

        composeTestRule.onNodeWithTag(SCREEN_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_game_title)
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PLATFORM_FILTER_TAG).assertIsDisplayed()
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()

        assertTrue(backClicked)
    }

    // Platforms

    @Test
    fun collection_without_platforms_offers_adding_one() {
        var addPlatformClicked = false
        setScreenContent(
            platformRepo = ScriptedPlatformCollectionRepository(),
            onAddPlatform = { addPlatformClicked = true }
        )

        composeTestRule.onNodeWithTag(ADD_PLATFORM_BUTTON_TAG).performClick()

        assertTrue(addPlatformClicked)
    }

    @Test
    fun platforms_read_failure_displays_error_view_and_retry_reads_them_again() {
        val platformRepo = ScriptedPlatformCollectionRepository(
            readErrors = listOf(UnrecoverablePersistenceException()),
            initialPlatforms = listOf(PS5, PC)
        )
        setScreenContent(platformRepo = platformRepo)

        // Act & Assert
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_game_platforms_error_message)
        assertTextIsDisplayed(R.string.platform_collection_error_tip_unrecoverable)
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        // The user can still leave the screen
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()

        // Act & Assert
        clickRetry()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
        assertEquals(2, platformRepo.readCount)
    }

    // Searching

    @Test
    fun typing_a_query_displays_its_results_on_each_platform() {
        val searchService = ScriptedGameSearchService(listOf(results(ELDEN_RING)))
        setScreenContent(searchService = searchService)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput("Elden")

        composeTestRule.waitUntil(WAIT_TIMEOUT_MS) { textExists(ELDEN_RING.name.value) }
        composeTestRule.onAllNodesWithTag(GameSearchItemViewTestTags.ITEM).assertCountEquals(2)
        assertEquals(listOf(SearchString("Elden")), searchService.requestedQueries)
    }

    @Test
    fun failed_search_displays_error_view_instead_of_the_search_view() {
        setScreenContent(
            searchService = ScriptedGameSearchService(listOf(failure(NoConnectivityException()))),
            initialQuery = QUERY
        )

        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_game_error_message)
        assertTextIsDisplayed(R.string.search_error_tip_no_connectivity)
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun retrying_displays_the_results_with_the_failed_query_in_the_search_box() {
        val searchService = ScriptedGameSearchService(
            listOf(failure(NoConnectivityException()), results(ELDEN_RING))
        )
        setScreenContent(searchService = searchService, initialQuery = QUERY)

        clickRetry()
        composeTestRule.mainClock.advanceTimeBy(PAST_DEBOUNCE_TIME_MS)

        composeTestRule.onNode(itemOn("PC")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
        // The search box did not emit the query again, which would have searched a third time
        assertEquals(listOf(QUERY, QUERY), searchService.requestedQueries)
    }

    // Adding games

    @Test
    fun selecting_a_game_adds_it_on_that_platform() {
        setScreenContent(
            searchService = ScriptedGameSearchService(listOf(results(ELDEN_RING))),
            initialQuery = QUERY
        )

        composeTestRule.onNode(itemOn("PC")).performClick()

        // Added on the PC only
        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADDED_ICON, useUnmergedTree = true)
            .assertCountEquals(1)
        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADD_ICON, useUnmergedTree = true)
            .assertCountEquals(1)
    }

    @Test
    fun failed_addition_is_reported_in_a_snackbar_naming_the_game_and_platform() {
        setScreenContent(
            searchService = ScriptedGameSearchService(listOf(results(ELDEN_RING))),
            gameRepo = ScriptedGameCollectionRepository(addError = UnrecoverablePersistenceException()),
            initialQuery = QUERY
        )
        composeTestRule.onNode(itemOn("PC")).assertIsDisplayed()
        // Paused, so that the snackbar is not dismissed before it is checked
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNode(itemOn("PC")).performClick()
        composeTestRule.mainClock.advanceTimeBy(SNACKBAR_SHOW_TIME_MS)

        composeTestRule
            .onNodeWithText(context.getString(R.string.search_game_add_failed_message, "Elden Ring", "PC"))
            .assertIsDisplayed()
    }
}
