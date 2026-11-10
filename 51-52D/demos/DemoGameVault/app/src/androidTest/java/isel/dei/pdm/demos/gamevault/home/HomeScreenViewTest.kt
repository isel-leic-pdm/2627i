package isel.dei.pdm.demos.gamevault.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.home.HomeScreenViewTestTags.COLLECTION_SECTION
import isel.dei.pdm.demos.gamevault.home.HomeScreenViewTestTags.NOW_PLAYING_SECTION
import isel.dei.pdm.demos.gamevault.home.HomeScreenViewTestTags.SUMMARY_SECTION
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeScreenViewTest {

    private companion object {
        val PLAYING = NowPlayingState.Loaded(HomeScreenSampleData.playing, hasMore = false)
        val NOTHING_PLAYING = NowPlayingState.Loaded(emptyList(), hasMore = false)
        val SUMMARY = SummaryState.Loaded(HomeScreenSampleData.summary)
        val NOW_PLAYING_ERROR = NowPlayingState.Error(RecoverablePersistenceException())
        val SUMMARY_ERROR = SummaryState.Error(RecoverablePersistenceException())
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setViewContent(
        nowPlaying: () -> NowPlayingState,
        summary: SummaryState = SUMMARY,
        onRetryNowPlaying: () -> Unit = {},
        onRetrySummary: () -> Unit = {},
        onFilterSelected: (CollectionFilter) -> Unit = {}
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                HomeScreenView(
                    nowPlaying = nowPlaying(),
                    summary = summary,
                    onRetryNowPlaying = onRetryNowPlaying,
                    onRetrySummary = onRetrySummary,
                    onGameSelected = {},
                    onFilterSelected = onFilterSelected,
                    onBrowseAll = {},
                    onAddGame = {}
                )
            }
        }
    }

    /**
     * The node with the given text, within the section with the given tag.
     */
    private fun textInSection(resId: Int, sectionTag: String): SemanticsNodeInteraction =
        composeTestRule.onNode(
            hasText(context.getString(resId)) and hasAnyAncestor(hasTestTag(sectionTag))
        )

    // Retrying

    @Test
    fun retrying_in_the_now_playing_region_only_retries_the_games_being_played() {
        var nowPlayingRetries = 0
        var summaryRetries = 0
        setViewContent(
            nowPlaying = { NOW_PLAYING_ERROR },
            summary = SUMMARY_ERROR,
            onRetryNowPlaying = { nowPlayingRetries++ },
            onRetrySummary = { summaryRetries++ }
        )

        textInSection(R.string.home_retry_button_label, NOW_PLAYING_SECTION).performClick()

        assertEquals(1, nowPlayingRetries)
        assertEquals(0, summaryRetries)
    }

    @Test
    fun retrying_in_the_summary_region_only_retries_the_summary() {
        var nowPlayingRetries = 0
        var summaryRetries = 0
        setViewContent(
            nowPlaying = { NOW_PLAYING_ERROR },
            summary = SUMMARY_ERROR,
            onRetryNowPlaying = { nowPlayingRetries++ },
            onRetrySummary = { summaryRetries++ }
        )

        textInSection(R.string.home_retry_button_label, SUMMARY_SECTION).performClick()

        assertEquals(0, nowPlayingRetries)
        assertEquals(1, summaryRetries)
    }

    // Regions independence

    @Test
    fun now_playing_error_does_not_hide_the_summary() {
        setViewContent(nowPlaying = { NOW_PLAYING_ERROR }, summary = SUMMARY)

        textInSection(R.string.home_region_error_message, NOW_PLAYING_SECTION).assertIsDisplayed()
        textInSection(R.string.home_stat_owned, SUMMARY_SECTION).assertIsDisplayed()
    }

    @Test
    fun summary_error_does_not_hide_the_games_being_played() {
        setViewContent(nowPlaying = { PLAYING }, summary = SUMMARY_ERROR)

        textInSection(R.string.home_region_error_message, SUMMARY_SECTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(GameCarouselTestTags.CAROUSEL).assertIsDisplayed()
    }

    // Nothing being played

    @Test
    fun picking_from_the_backlog_selects_the_backlog_filter() {
        var selectedFilter: CollectionFilter? = null
        setViewContent(nowPlaying = { NOTHING_PLAYING }, onFilterSelected = { selectedFilter = it })

        composeTestRule
            .onNodeWithText(context.getString(R.string.home_pick_from_backlog_label))
            .performClick()

        assertEquals(CollectionFilter.InState(PlayStatus.State.BACKLOG), selectedFilter)
    }

    // Layout stability

    @Test
    fun sections_below_now_playing_do_not_move_when_its_state_changes() {
        val nowPlaying = mutableStateOf<NowPlayingState>(NowPlayingState.Loading)
        setViewContent(nowPlaying = { nowPlaying.value })
        val collectionTop = {
            composeTestRule.onNodeWithTag(COLLECTION_SECTION).getUnclippedBoundsInRoot().top
        }
        val loadingTop = collectionTop()

        listOf(PLAYING, NOTHING_PLAYING, NOW_PLAYING_ERROR).forEach { state ->
            nowPlaying.value = state
            assertEquals("Collection section moved in $state", loadingTop, collectionTop())
        }
    }
}
