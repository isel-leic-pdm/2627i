package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.RateLimitExceededException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnauthenticatedException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformViewModel.Companion.QUERY_KEY
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemViewTestTags
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [SearchForPlatformScreen], with an actual view model whose dependencies are scripted
 * fakes. Besides the screen's own responsibilities (the top bar, the choice between the search
 * view and the error view, and the snackbar), they check that the screen is wired to the view
 * model. The state of each screen is covered in detail by [SearchForPlatformScreenViewTest].
 */
class SearchForPlatformScreenTest {

    private companion object {
        val QUERY = SearchString("PlayStation")
        val PS5_PRO = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Pro")
        val PS4 = PlatformInfo(4L, "PS4", "PlayStation 4", "https://fake.url/ps4.png")

        /**
         * Enough for the snackbar to be displayed, but not dismissed.
         */
        const val SNACKBAR_SHOW_TIME_MS = 500L

        /**
         * Longer than the duration of a short snackbar.
         */
        const val SNACKBAR_DISMISS_TIME_MS = 10_000L

        /**
         * Longer than the search box's debounce delay.
         */
        const val PAST_DEBOUNCE_TIME_MS = 1_000L

        /**
         * How long to wait for asynchronous work driven by the real clock (e.g. the view model).
         */
        const val WAIT_TIMEOUT_MS = 5_000L

        fun results(vararg platforms: PlatformInfo): Result<List<PlatformInfo>> =
            Result.success(platforms.toList())

        fun failure(error: SearchServiceException): Result<List<PlatformInfo>> =
            Result.failure(error)
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
        searchService: PlatformSearchService = ScriptedPlatformSearchService(listOf(results())),
        repository: PlatformCollectionRepository = ScriptedPlatformCollectionRepository(),
        initialQuery: SearchString? = null,
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Created on the main thread, as the view model expects
            val viewModel = remember {
                val savedState = initialQuery?.let { mapOf(QUERY_KEY to it.value) } ?: emptyMap()
                SearchForPlatformViewModel(searchService, repository, SavedStateHandle(savedState))
            }
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreen(viewModel = viewModel, onBack = onBack)
                }
            }
        }
    }

    /**
     * Advances the (paused) main clock, one frame at a time, until the given condition holds.
     * @throws IllegalStateException if the condition does not hold within [timeoutMs].
     */
    private fun advanceClockUntil(
        timeoutMs: Long = SNACKBAR_DISMISS_TIME_MS,
        condition: () -> Boolean
    ) {
        val start = composeTestRule.mainClock.currentTime
        while (!condition()) {
            check(composeTestRule.mainClock.currentTime - start < timeoutMs) {
                "Condition not met within $timeoutMs ms"
            }
            composeTestRule.mainClock.advanceTimeByFrame()
        }
    }

    private fun textExists(text: String): Boolean =
        composeTestRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()

    private fun assertTextIsDisplayed(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
    }

    private fun clickRetry() {
        composeTestRule
            .onNodeWithText(context.getString(R.string.search_platform_retry_button_label))
            .performClick()
    }

    private fun hasEditableText(text: String): SemanticsMatcher =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(text))

    // Top bar

    @Test
    fun screen_displays_title_back_button_and_search_box() {
        setScreenContent()

        composeTestRule.onNodeWithTag(SCREEN_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_platform_title)
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertIsDisplayed()
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()

        assertTrue(backClicked)
    }

    // Searching

    @Test
    fun typing_a_query_displays_its_results() {
        val searchService = ScriptedPlatformSearchService(listOf(results(PS4)))
        setScreenContent(searchService = searchService)

        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput("PS4")

        composeTestRule.waitUntil(WAIT_TIMEOUT_MS) { textExists(PS4.name.value) }
        assertEquals(listOf(SearchString("PS4")), searchService.requestedQueries)
    }

    @Test
    fun clearing_the_query_displays_the_start_hint() {
        // The query is typed, because only the search box's own state gives it a clear button
        setScreenContent(searchService = ScriptedPlatformSearchService(listOf(results(PS4))))
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).performTextInput("PS4")
        composeTestRule.waitUntil(WAIT_TIMEOUT_MS) { textExists(PS4.name.value) }

        composeTestRule.onNodeWithTag(SearchBoxTestTags.CLEAR_BUTTON).performClick()

        assertTextIsDisplayed(R.string.search_platform_start_hint)
        composeTestRule.onNodeWithText(PS4.name.value).assertDoesNotExist()
    }

    // Errors

    @Test
    fun failed_search_displays_error_view_instead_of_the_search_view() {
        setScreenContent(
            searchService = ScriptedPlatformSearchService(listOf(failure(NoConnectivityException()))),
            initialQuery = QUERY
        )

        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.search_platform_error_message)
        assertTextIsDisplayed(R.string.search_platform_retry_button_label)
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assertDoesNotExist()
        // The user can still leave the screen
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun error_view_displays_a_tip_that_depends_on_the_error() {
        val expectedTips = listOf(
            NoConnectivityException() to R.string.search_error_tip_no_connectivity,
            UnauthenticatedException() to R.string.search_error_tip_unauthenticated,
            ServiceUnavailableException() to R.string.search_error_tip_service_unavailable,
            RateLimitExceededException() to R.string.search_error_tip_rate_limit_exceeded,
            UnexpectedServiceException() to R.string.search_error_tip_unexpected
        )
        // Each retry fails with the next error
        setScreenContent(
            searchService = ScriptedPlatformSearchService(expectedTips.map { failure(it.first) }),
            initialQuery = QUERY
        )

        expectedTips.forEachIndexed { index, (_, tipResId) ->
            if (index > 0) clickRetry()

            composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.TIP_TAG).assertIsDisplayed()
            assertTextIsDisplayed(tipResId)
        }
    }

    @Test
    fun retrying_displays_the_results_with_the_failed_query_in_the_search_box() {
        val searchService = ScriptedPlatformSearchService(
            listOf(failure(NoConnectivityException()), results(PS4))
        )
        setScreenContent(searchService = searchService, initialQuery = QUERY)

        clickRetry()
        composeTestRule.mainClock.advanceTimeBy(PAST_DEBOUNCE_TIME_MS)

        composeTestRule.onNodeWithText(PS4.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SearchBoxTestTags.INPUT).assert(hasEditableText(QUERY.value))
        // The search box did not emit the query again, which would have searched a third time
        assertEquals(listOf(QUERY, QUERY), searchService.requestedQueries)
    }

    // Adding platforms

    @Test
    fun selecting_a_platform_adds_it_to_the_collection() {
        setScreenContent(
            searchService = ScriptedPlatformSearchService(listOf(results(PS4))),
            initialQuery = QUERY
        )

        composeTestRule.onNodeWithText(PS4.name.value).performClick()

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ADDED_ICON, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun failed_addition_is_reported_in_a_snackbar() {
        setScreenContent(
            searchService = ScriptedPlatformSearchService(listOf(results(PS4))),
            repository = ScriptedPlatformCollectionRepository(UnrecoverablePersistenceException()),
            initialQuery = QUERY
        )
        composeTestRule.onNodeWithText(PS4.name.value).assertIsDisplayed()
        // Paused, so that the snackbar is not dismissed before it is checked
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.onNodeWithText(PS4.name.value).performClick()
        composeTestRule.mainClock.advanceTimeBy(SNACKBAR_SHOW_TIME_MS)

        composeTestRule
            .onNodeWithText(context.getString(R.string.search_platform_add_failed_message, "PlayStation 4"))
            .assertIsDisplayed()
    }

    @Test
    fun addition_outcomes_are_reported_one_at_a_time() {
        setScreenContent(
            searchService = ScriptedPlatformSearchService(listOf(results(PS4, PS5_PRO))),
            repository = ScriptedPlatformCollectionRepository(UnrecoverablePersistenceException()),
            initialQuery = QUERY
        )
        composeTestRule.onNodeWithText(PS4.name.value).assertIsDisplayed()
        composeTestRule.mainClock.autoAdvance = false
        val firstMessage =
            context.getString(R.string.search_platform_add_failed_message, "PlayStation 4")
        val secondMessage =
            context.getString(R.string.search_platform_add_failed_message, "PlayStation 5 Pro")

        composeTestRule.onNodeWithText(PS4.name.value).performClick()
        // The version is displayed as a suffix (e.g. "· Pro") next to the abbreviation
        composeTestRule.onNodeWithText(PS5_PRO.version!!.value, substring = true).performClick()

        // Act & Assert - The second is only displayed once the first is dismissed
        composeTestRule.mainClock.advanceTimeBy(SNACKBAR_SHOW_TIME_MS)
        composeTestRule.onNodeWithText(firstMessage).assertIsDisplayed()
        composeTestRule.onNodeWithText(secondMessage).assertDoesNotExist()

        advanceClockUntil { !textExists(firstMessage) }
        composeTestRule.mainClock.advanceTimeBy(SNACKBAR_SHOW_TIME_MS)
        composeTestRule.onNodeWithText(secondMessage).assertIsDisplayed()
    }
}
