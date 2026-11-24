package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import isel.dei.pdm.demos.gamevault.collection.games.PlayStateChipTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDatesRowTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameDetailsHeaderTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameHistoryTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.GameNotesTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlaySessionBarTestTags
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.PlayStatusPanelTestTags
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Tests for [GameDetailsTopBar] and [GameDetailsScreenView]. The view's parts are covered in
 * detail by their own tests: these check that the view composes them, from the game's entry.
 */
class GameDetailsScreenViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Top bar

    private var backClicks = 0
    private var removeClicks = 0

    private fun setTopBarContent(isRemoveEnabled: Boolean = true) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameDetailsTopBar(
                    onBack = { backClicks++ },
                    onRemove = { removeClicks++ },
                    isRemoveEnabled = isRemoveEnabled
                )
            }
        }
    }

    private fun backButton() = composeTestRule.onNodeWithTag(GameDetailsScreenViewTestTags.BACK_BUTTON)

    private fun removeButton() = composeTestRule.onNodeWithTag(GameDetailsScreenViewTestTags.REMOVE_BUTTON)

    @Test
    fun top_bar_displays_the_title_and_the_described_actions() {
        setTopBarContent()

        composeTestRule.onNodeWithText("Game Details").assertIsDisplayed()
        backButton().assertIsDisplayed().assertContentDescriptionEquals("Back")
        removeButton().assertIsDisplayed().assertContentDescriptionEquals("Remove from collection")
    }

    @Test
    fun clicking_back_triggers_onBack_only() {
        setTopBarContent()

        backButton().performClick()

        assertEquals(1, backClicks)
        assertEquals(0, removeClicks)
    }

    @Test
    fun clicking_remove_triggers_onRemove_only() {
        setTopBarContent()

        removeButton().performClick()

        assertEquals(0, backClicks)
        assertEquals(1, removeClicks)
    }

    @Test
    fun disabled_remove_does_nothing_while_back_remains_enabled() {
        setTopBarContent(isRemoveEnabled = false)

        removeButton().assertIsNotEnabled().performClick()
        backButton().assertIsEnabled().performClick()

        assertEquals(0, removeClicks)
        assertEquals(1, backClicks)
    }

    // Loading view

    private fun setLoadingViewContent() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameDetailsLoadingView()
            }
        }
    }

    @Test
    fun loading_view_is_described_as_loading() {
        setLoadingViewContent()

        node(GameDetailsScreenViewTestTags.LOADING)
            .assertContentDescriptionEquals("Loading game details…")
    }

    @Test
    fun loading_view_displays_the_labels_but_no_game_information() {
        setLoadingViewContent()

        composeTestRule.onNodeWithText("Play Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Notes").performScrollTo().assertIsDisplayed()
        listOf(
            GameDetailsHeaderTestTags.NAME,
            GameDetailsHeaderTestTags.PLATFORMS,
            GameDetailsScreenViewTestTags.COVER,
            PlayStateChipTestTags.CHIP,
            PlayStatusPanelTestTags.PLAY_TIME,
            PlayStatusPanelTestTags.COMPLETED_RUNS,
            PlayStateChipTestTags.ADD_PLATINUM_CHIP,
            PlayStateChipTestTags.PLATINUM_CHIP,
            GameHistoryTestTags.EMPTY,
            GameHistoryTestTags.ENTRY,
            GameNotesTestTags.NOTES,
            GameNotesTestTags.EMPTY,
            GameDatesRowTestTags.RELEASED,
            GameDatesRowTestTags.ADDED_ON
        ).forEach { node(it).assertDoesNotExist() }
    }

    @Test
    fun loading_view_disables_all_actions_and_offers_no_session_action() {
        setLoadingViewContent()

        changeStateButton().assertIsNotEnabled()
        editPlayTimeButton().assertIsNotEnabled()
        addRunButton().assertIsNotEnabled()
        node(GameNotesTestTags.EDIT_BUTTON).performScrollTo().assertIsNotEnabled()
        node(PlaySessionBarTestTags.PLACEHOLDER_BUTTON).assertIsDisplayed().assertIsNotEnabled()
        node(PlaySessionBarTestTags.START_BUTTON).assertDoesNotExist()
        node(PlaySessionBarTestTags.STOP_BUTTON).assertDoesNotExist()
    }

    // View

    private val game = Game(1L, "Elden Ring", LocalDate(2022, 2, 25))
    private val platforms = setOf(Platform(1L, "PS5", "PlayStation 5"))
    private val sessionStart = Instant.parse("2026-11-18T21:05:00Z")
    private val firstRun = Accomplishment(LocalDate(2024, 3, 10))

    private fun entry(
        playStatus: PlayStatus = PlayStatus(PlayTime(12, 0), PlayStatus.State.PLAYING),
        addedAt: Instant = Instant.parse("2024-03-01T10:00:00Z"),
        sessionStartTime: Instant? = null,
        notes: NonBlankString? = null
    ) = CollectionEntry(game, platforms, playStatus, addedAt, sessionStartTime, notes)

    private fun finishedPlayStatus() =
        PlayStatus(PlayTime(40, 0), PlayStatus.State.PLAYING, completedRuns = listOf(firstRun))

    private val clicks = mutableListOf<String>()

    private fun setViewContent(
        entry: CollectionEntry = entry(),
        sessionElapsedTime: Duration? = null,
        timeZone: TimeZone = TimeZone.UTC
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameDetailsScreenView(
                    entry = entry,
                    sessionElapsedTime = sessionElapsedTime,
                    onChangeState = { clicks += "changeState" },
                    onEditPlayTime = { clicks += "editPlayTime" },
                    onAddRun = { clicks += "addRun" },
                    onRegisterPlatinum = { clicks += "registerPlatinum" },
                    onEditNotes = { clicks += "editNotes" },
                    onStartSession = { clicks += "startSession" },
                    onStopSession = { clicks += "stopSession" },
                    timeZone = timeZone
                )
            }
        }
    }

    private fun node(tag: String) = composeTestRule.onNodeWithTag(tag)

    private fun changeStateButton() = node(PlayStatusPanelTestTags.CHANGE_STATE_BUTTON)
    private fun editPlayTimeButton() = node(PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON)
    private fun addRunButton() = node(PlayStatusPanelTestTags.ADD_RUN_BUTTON)
    private fun addPlatinumAction() = node(PlayStateChipTestTags.ADD_PLATINUM_CHIP).onChild()

    @Test
    fun displays_the_game_its_play_status_and_its_sections() {
        setViewContent(entry = entry(notes = NonBlankString("Stopped at Malenia.")))

        node(GameDetailsHeaderTestTags.NAME).assertTextEquals("Elden Ring")
        node(GameDetailsHeaderTestTags.PLATFORMS).assertTextEquals("PlayStation 5")
        node(GameDetailsScreenViewTestTags.COVER).assertIsDisplayed()
        node(PlayStatusPanelTestTags.PLAY_TIME).assertTextEquals("12 h")
        node(GameHistoryTestTags.EMPTY).performScrollTo().assertIsDisplayed()
        node(GameNotesTestTags.NOTES).performScrollTo().assertTextEquals("Stopped at Malenia.")
        node(GameDatesRowTestTags.RELEASED).performScrollTo().assertTextEquals("Released: 2022")
    }

    @Test
    fun the_date_the_game_was_added_on_is_that_of_the_given_time_zone() {
        // Late on the 1st in UTC is already the 2nd an hour ahead
        setViewContent(
            entry = entry(addedAt = Instant.parse("2024-03-01T23:30:00Z")),
            timeZone = TimeZone.of("UTC+01:00")
        )

        node(GameDatesRowTestTags.ADDED_ON).performScrollTo().assertTextEquals("Added on: 2024-03-02")
    }

    @Test
    fun without_a_session_all_actions_are_enabled_and_starting_is_offered() {
        setViewContent()

        changeStateButton().assertIsEnabled()
        editPlayTimeButton().assertIsEnabled()
        addRunButton().assertIsEnabled()
        addPlatinumAction().assertIsEnabled()
        node(PlaySessionBarTestTags.START_BUTTON).assertIsDisplayed()
    }

    @Test
    fun during_a_session_of_a_game_not_finished_yet_it_can_neither_leave_playing_nor_be_finished() {
        setViewContent(entry = entry(sessionStartTime = sessionStart), sessionElapsedTime = 30.minutes)

        changeStateButton().assertIsNotEnabled()
        addRunButton().assertIsNotEnabled()
        addPlatinumAction().assertIsNotEnabled()
        editPlayTimeButton().assertIsEnabled()
        node(PlaySessionBarTestTags.ELAPSED_TIME).assertTextEquals("Playing for 30 min")
        node(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun during_a_session_of_a_finished_game_runs_and_the_platinum_can_be_registered() {
        setViewContent(
            entry = entry(playStatus = finishedPlayStatus(), sessionStartTime = sessionStart),
            sessionElapsedTime = 30.minutes
        )

        changeStateButton().assertIsNotEnabled()
        addRunButton().assertIsEnabled()
        addPlatinumAction().assertIsEnabled()
    }

    @Test
    fun the_session_controls_remain_displayed_whatever_the_content_length() {
        val manyRuns = (1..20).map { Accomplishment(LocalDate(2024, 1, it)) }
        setViewContent(
            entry = entry(
                playStatus = PlayStatus(PlayTime(40, 0), PlayStatus.State.PLAYING, manyRuns),
                sessionStartTime = sessionStart
            ),
            sessionElapsedTime = 30.minutes
        )

        node(GameDatesRowTestTags.ADDED_ON).performScrollTo().assertIsDisplayed()
        node(PlaySessionBarTestTags.STOP_BUTTON).assertIsDisplayed()
    }

    @Test
    fun each_action_triggers_its_own_callback() {
        setViewContent()

        changeStateButton().performClick()
        editPlayTimeButton().performClick()
        addRunButton().performClick()
        addPlatinumAction().performClick()
        node(GameNotesTestTags.EDIT_BUTTON).performScrollTo().performClick()
        node(PlaySessionBarTestTags.START_BUTTON).performClick()

        assertEquals(
            listOf(
                "changeState", "editPlayTime", "addRun", "registerPlatinum", "editNotes",
                "startSession"
            ),
            clicks
        )
    }

    @Test
    fun stopping_the_session_triggers_onStopSession() {
        setViewContent(entry = entry(sessionStartTime = sessionStart), sessionElapsedTime = 1.minutes)

        node(PlaySessionBarTestTags.STOP_BUTTON).performClick()

        assertEquals(listOf("stopSession"), clicks)
    }
}
