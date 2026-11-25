package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenViewTestTags.BACK_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenViewTestTags.REMOVE_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsViewModel.Companion.GAME_ID_KEY
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDetailsHeaderTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlaySessionBarTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.StartSessionResult
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Tests for [GameDetailsScreen], with an actual view model whose dependencies are fakes. Besides
 * the screen's own responsibilities (the top bar, reporting that the game is not found, and
 * confirming that play sessions are stopped, including when leaving), they
 * check that the screen is wired to the view model. The views are covered in detail by
 * [GameDetailsScreenViewTest].
 */
class GameDetailsScreenTest {

    private companion object {
        val PS5 = Platform(1L, "PS5", "PlayStation 5")
        val ELDEN_RING = Game(1L, "Elden Ring")
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val entry = CollectionEntry(
        game = ELDEN_RING,
        platforms = setOf(PS5),
        playStatus = PlayStatus(PlayTime(12, 0), State.PLAYING)
    )

    private var notFoundReports = 0

    /**
     * Displays the screen of the given game, with a view model that uses the given repository.
     */
    private fun setScreenContent(
        gameRepository: GameCollectionRepository = FakeGameCollectionRepository(listOf(entry)),
        gameId: Long = ELDEN_RING.id,
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Created on the main thread, as the view model expects
            val viewModel = remember {
                GameDetailsViewModel(
                    gameRepo = gameRepository,
                    savedStateHandle = SavedStateHandle(mapOf(GAME_ID_KEY to gameId))
                )
            }
            DemoGameVaultTheme {
                GameDetailsScreen(
                    viewModel = viewModel,
                    onBack = onBack,
                    onGameNotFound = { notFoundReports++ }
                )
            }
        }
    }

    // Top bar

    @Test
    fun screen_displays_title_back_button_and_remove_button() {
        setScreenContent()

        composeTestRule.onNodeWithTag(SCREEN_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.game_details_title)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(REMOVE_BUTTON).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(BACK_BUTTON).performClick()

        assertTrue(backClicked)
    }

    // View model wiring

    @Test
    fun the_game_is_displayed() {
        setScreenContent()

        composeTestRule.onNodeWithTag(GameDetailsHeaderTestTags.NAME).assertTextEquals("Elden Ring")
        assertEquals(0, notFoundReports)
    }

    @Test
    fun while_the_game_is_read_the_loading_view_is_displayed_and_it_cannot_be_removed() {
        setScreenContent(gameRepository = PendingRepository())

        composeTestRule.onNodeWithTag(GameDetailsScreenViewTestTags.LOADING).assertIsDisplayed()
        composeTestRule.onNodeWithTag(REMOVE_BUTTON).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BACK_BUTTON).assertIsEnabled()
    }

    @Test
    fun game_not_in_the_collection_is_reported_once() {
        setScreenContent(gameId = 99L)

        composeTestRule.waitForIdle()

        assertEquals(1, notFoundReports)
    }

    @Test
    fun removing_the_game_elsewhere_is_reported_as_not_found() {
        val repository = FakeGameCollectionRepository(listOf(entry))
        setScreenContent(gameRepository = repository)
        composeTestRule.onNodeWithTag(GameDetailsHeaderTestTags.NAME).assertIsDisplayed()

        // On the main thread, as the fake repository expects (its operations do not suspend)
        composeTestRule.runOnUiThread { runBlocking { repository.remove(ELDEN_RING.id) } }
        composeTestRule.waitForIdle()

        assertEquals(1, notFoundReports)
    }

    // Play sessions

    private fun entryInSession(elapsed: Duration) =
        entry.startSession(now = Clock.System.now() - elapsed)

    @Test
    fun starting_a_session_turns_start_into_stop() {
        setScreenContent()

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).performClick()

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).assertDoesNotExist()
    }

    @Test
    fun stopping_a_session_is_confirmed_first() {
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(72.minutes)))
        )

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).performClick()
        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DIALOG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.STOP_BUTTON).performClick()

        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DIALOG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).assertIsDisplayed()
    }

    @Test
    fun keeping_playing_keeps_the_session() {
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(72.minutes)))
        )

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).performClick()
        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DISMISS_BUTTON).performClick()

        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DIALOG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun stopping_a_short_session_asks_whether_to_register_it() {
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(2.minutes)))
        )

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).performClick()
        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DISCARD_BUTTON).performClick()

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).assertIsDisplayed()
    }

    @Test
    fun going_back_during_a_session_asks_to_stop_it_and_leaves_once_stopped() {
        var backReports = 0
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(72.minutes))),
            onBack = { backReports++ }
        )

        composeTestRule.onNodeWithTag(BACK_BUTTON).performClick()
        composeTestRule
            .onNodeWithText(context.getString(R.string.game_details_stop_dialog_leave_title))
            .assertIsDisplayed()
        assertEquals(0, backReports)

        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.STOP_BUTTON).performClick()
        composeTestRule.waitForIdle()

        assertEquals(1, backReports)
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).assertIsDisplayed()
    }

    @Test
    fun staying_does_not_leave_nor_stop_the_session() {
        var backReports = 0
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(72.minutes))),
            onBack = { backReports++ }
        )

        composeTestRule.onNodeWithTag(BACK_BUTTON).performClick()
        composeTestRule.onNodeWithTag(StopSessionDialogTestTags.DISMISS_BUTTON).performClick()
        composeTestRule.waitForIdle()

        assertEquals(0, backReports)
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun system_back_during_a_session_asks_to_stop_it() {
        setScreenContent(
            gameRepository = FakeGameCollectionRepository(listOf(entryInSession(72.minutes)))
        )
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()

        Espresso.pressBack()

        composeTestRule
            .onNodeWithText(context.getString(R.string.game_details_stop_dialog_leave_title))
            .assertIsDisplayed()
    }

    @Test
    fun failure_to_start_a_session_is_shown() {
        setScreenContent(gameRepository = FailingSessionsRepository(listOf(entry)))

        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).performClick()

        composeTestRule
            .onNodeWithText(context.getString(R.string.game_details_session_failure))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON).assertIsDisplayed()
    }

    // Errors

    @Test
    fun failed_read_displays_error_view_and_retrying_displays_the_game() {
        setScreenContent(gameRepository = FailingOnceRepository(listOf(entry)))
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.game_details_error_message))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(BACK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(REMOVE_BUTTON).assertIsNotEnabled()

        composeTestRule
            .onNodeWithText(context.getString(R.string.game_details_retry_button_label))
            .performClick()

        composeTestRule.onNodeWithTag(GameDetailsHeaderTestTags.NAME).assertTextEquals("Elden Ring")
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertDoesNotExist()
    }
}

/**
 * A repository whose entries are never read, i.e. whose observation never emits. Otherwise, it
 * behaves as an empty [FakeGameCollectionRepository].
 */
private class PendingRepository(
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository()
) : GameCollectionRepository by delegate {

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> = flow { awaitCancellation() }
}

/**
 * A repository where play sessions cannot be started. Otherwise, it behaves as a
 * [FakeGameCollectionRepository] with the given entries.
 */
private class FailingSessionsRepository(
    entries: List<CollectionEntry>,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    override suspend fun startSession(gameId: Long, now: Instant): StartSessionResult =
        throw RecoverablePersistenceException("Simulated failure")
}

/**
 * A repository whose first observation of an entry fails. Otherwise, it behaves as a
 * [FakeGameCollectionRepository] with the given entries.
 */
private class FailingOnceRepository(
    entries: List<CollectionEntry>,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    private var observations = 0

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> = flow {
        if (observations++ == 0) throw UnrecoverablePersistenceException()
        emitAll(delegate.observeEntry(gameId))
    }
}
