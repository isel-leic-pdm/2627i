package isel.dei.pdm.demos.gamevault.collection.games

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenViewTestTags.BACK_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.STATE_KEY
import isel.dei.pdm.demos.gamevault.collection.games.filters.ActiveFiltersRowTestTags
import isel.dei.pdm.demos.gamevault.collection.games.filters.FiltersButtonTestTags
import isel.dei.pdm.demos.gamevault.collection.games.filters.FiltersSheetContentTestTags
import isel.dei.pdm.demos.gamevault.domain.LimitedResult
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.search.platforms.ScriptedPlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [GameCollectionScreen], with an actual view model whose dependencies are fakes.
 * Besides the screen's own responsibilities (the top bar and the filters sheet's visibility),
 * they check that the screen is wired to the view model. The view is covered in detail by
 * [GameCollectionScreenViewTest].
 */
class GameCollectionScreenTest {

    private companion object {
        val PS5 = Platform(1L, "PS5", "PlayStation 5")
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun entry(id: Long, name: String, state: State) =
        CollectionEntry(
            game = Game(id, name),
            platforms = setOf(PS5),
            playStatus = PlayStatus(PlayTime(1, 0), state)
        )

    private val sampleEntries = listOf(
        entry(1L, "Celeste", State.BACKLOG),
        entry(2L, "Hades", State.PLAYING)
    )

    /**
     * Displays the screen, with a view model that uses the given dependencies.
     */
    private fun setScreenContent(
        gameRepository: GameCollectionRepository = FakeGameCollectionRepository(sampleEntries),
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Created on the main thread, as the view model expects
            val viewModel = remember {
                GameCollectionViewModel(
                    gameRepo = gameRepository,
                    platformRepo = ScriptedPlatformCollectionRepository(initialPlatforms = listOf(PS5)),
                    savedStateHandle = savedStateHandle
                )
            }
            DemoGameVaultTheme {
                GameCollectionScreen(viewModel = viewModel, onBack = onBack)
            }
        }
    }

    // Top bar

    @Test
    fun screen_displays_title_back_button_and_filters_button() {
        setScreenContent()

        composeTestRule.onNodeWithTag(SCREEN_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.game_collection_title)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FiltersButtonTestTags.BUTTON).assertIsDisplayed()
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(BACK_BUTTON).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun clicking_the_filters_button_opens_the_filters_sheet() {
        setScreenContent()

        composeTestRule.onNodeWithTag(FiltersButtonTestTags.BUTTON).performClick()

        composeTestRule.onNodeWithTag(FiltersSheetContentTestTags.CONTENT).assertIsDisplayed()
    }

    // View model wiring

    @Test
    fun games_in_the_collection_are_displayed() {
        setScreenContent()

        composeTestRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hades").assertIsDisplayed()
    }

    @Test
    fun initial_query_is_displayed_as_filters() {
        setScreenContent(savedStateHandle = SavedStateHandle(mapOf(STATE_KEY to State.PLAYING.name)))

        composeTestRule.onNodeWithTag(FiltersButtonTestTags.BADGE, useUnmergedTree = true)
            .onChild()
            .assertTextEquals("1")
        composeTestRule
            .onNode(hasTestTag(ActiveFiltersRowTestTags.CHIP) and hasText("Playing"))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Hades").assertIsDisplayed()
        composeTestRule.onNodeWithText("Celeste").assertDoesNotExist()
    }

    @Test
    fun removing_a_filter_displays_the_games_that_meet_the_new_query() {
        setScreenContent(savedStateHandle = SavedStateHandle(mapOf(STATE_KEY to State.PLAYING.name)))

        composeTestRule
            .onNode(hasTestTag(ActiveFiltersRowTestTags.CHIP) and hasText("Playing"))
            .performClick()

        composeTestRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeTestRule.onNodeWithTag(ActiveFiltersRowTestTags.ROW).assertDoesNotExist()
        composeTestRule.onNodeWithTag(FiltersButtonTestTags.BADGE, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    // Errors

    @Test
    fun failed_read_displays_error_view_and_retrying_displays_the_games() {
        val repository = FailingOnceRepository(sampleEntries)
        setScreenContent(gameRepository = repository)
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.game_collection_error_message))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(BACK_BUTTON).assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.game_collection_retry_button_label))
            .performClick()

        composeTestRule.onNodeWithText("Celeste").assertIsDisplayed()
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertDoesNotExist()
    }
}

/**
 * A repository whose first observation of the items fails. Otherwise, it behaves as a
 * [FakeGameCollectionRepository] with the given entries.
 */
private class FailingOnceRepository(
    entries: List<CollectionEntry>,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    private var observations = 0

    override fun observeItems(
        query: CollectionQuery,
        limit: Int
    ): Flow<LimitedResult<CollectionItem>> = flow {
        if (observations++ == 0) throw UnrecoverablePersistenceException()
        emitAll(delegate.observeItems(query, limit))
    }
}
