package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
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
import isel.dei.pdm.demos.gamevault.collection.games.parts.PlayStateChipTestTags
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlayStatusPanelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val twoRuns = listOf(Accomplishment(null), Accomplishment(null))

    private var stateChanges = 0
    private var playTimeEdits = 0
    private var runAdditions = 0
    private var platinumRegistrations = 0

    private val finishedWithoutPlatinum =
        PlayStatus(state = PlayStatus.State.FINISHED, completedRuns = twoRuns)

    private val finishedWithPlatinum = finishedWithoutPlatinum.copy(platinum = Accomplishment(null))

    private fun setPanelContent(
        playStatus: PlayStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        isStateChangeEnabled: Boolean = true,
        isAddRunEnabled: Boolean = true,
        isRegisterPlatinumEnabled: Boolean = true
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                PlayStatusPanel(
                    playStatus = playStatus,
                    onChangeState = { stateChanges++ },
                    onEditPlayTime = { playTimeEdits++ },
                    onAddRun = { runAdditions++ },
                    onRegisterPlatinum = { platinumRegistrations++ },
                    isStateChangeEnabled = isStateChangeEnabled,
                    isAddRunEnabled = isAddRunEnabled,
                    isRegisterPlatinumEnabled = isRegisterPlatinumEnabled
                )
            }
        }
    }

    private fun node(tag: String) = composeTestRule.onNodeWithTag(tag)

    private fun platinumChip() = node(PlayStateChipTestTags.PLATINUM_CHIP)

    private fun addPlatinumChip() = node(PlayStateChipTestTags.ADD_PLATINUM_CHIP)

    /**
     * The clickable part of the given chip, which holds its action (e.g. its description).
     */
    private fun SemanticsNodeInteraction.action() = onChild()

    private fun hasClickLabel(label: String) = SemanticsMatcher("has click label '$label'") {
        it.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

    // Display

    @Test
    fun labels_are_displayed() {
        setPanelContent()

        composeTestRule.onNodeWithText("Play Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play Time").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed Runs").assertIsDisplayed()
    }

    @Test
    fun play_state_is_displayed_as_a_chip() {
        setPanelContent(playStatus = PlayStatus(state = PlayStatus.State.PAUSED))

        node(PlayStateChipTestTags.CHIP).assertIsDisplayed()
        composeTestRule.onNodeWithText("Paused").assertIsDisplayed()
    }

    @Test
    fun play_time_in_whole_hours_is_displayed_without_minutes() {
        setPanelContent(playStatus = PlayStatus(PlayTime(hours = 3), PlayStatus.State.PLAYING))

        node(PlayStatusPanelTestTags.PLAY_TIME).assertTextEquals("3 h")
    }

    @Test
    fun play_time_with_minutes_is_displayed_with_them() {
        setPanelContent(playStatus = PlayStatus(PlayTime(hours = 2, minutes = 45), PlayStatus.State.PLAYING))

        node(PlayStatusPanelTestTags.PLAY_TIME).assertTextEquals("2 h 45 min")
    }

    @Test
    fun play_time_under_an_hour_is_displayed_in_minutes() {
        setPanelContent(playStatus = PlayStatus(PlayTime(hours = 0, minutes = 45), PlayStatus.State.PLAYING))

        node(PlayStatusPanelTestTags.PLAY_TIME).assertTextEquals("45 min")
    }

    @Test
    fun no_play_time_is_displayed_as_not_recorded() {
        // e.g. a game finished before the user adopted the app
        setPanelContent(playStatus = finishedWithoutPlatinum)

        node(PlayStatusPanelTestTags.PLAY_TIME).assertTextEquals("Not recorded")
    }

    @Test
    fun number_of_completed_runs_is_displayed() {
        setPanelContent(playStatus = PlayStatus(state = PlayStatus.State.FINISHED, completedRuns = twoRuns))

        node(PlayStatusPanelTestTags.COMPLETED_RUNS).assertTextEquals("2")
    }

    @Test
    fun platinum_not_obtained_is_offered_to_be_registered() {
        setPanelContent(playStatus = finishedWithoutPlatinum)

        platinumChip().assertDoesNotExist()
        addPlatinumChip().assertIsDisplayed()
        addPlatinumChip().action().assert(hasClickLabel("Register platinum"))
    }

    @Test
    fun platinum_obtained_is_displayed_and_can_be_edited() {
        setPanelContent(playStatus = finishedWithPlatinum)

        addPlatinumChip().assertDoesNotExist()
        platinumChip().assertIsDisplayed()
        platinumChip().action().assert(hasClickLabel("Edit platinum"))
    }

    @Test
    fun actions_are_described() {
        setPanelContent()

        node(PlayStatusPanelTestTags.CHANGE_STATE_BUTTON).assertContentDescriptionEquals("Change play status")
        node(PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON).assertContentDescriptionEquals("Edit play time")
        node(PlayStatusPanelTestTags.ADD_RUN_BUTTON).assertContentDescriptionEquals("Add completed run")
    }

    // Interaction

    @Test
    fun each_action_triggers_its_own_callback() {
        setPanelContent()

        node(PlayStatusPanelTestTags.CHANGE_STATE_BUTTON).performClick()
        node(PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON).performClick()
        node(PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON).performClick()
        node(PlayStatusPanelTestTags.ADD_RUN_BUTTON).performClick()
        node(PlayStatusPanelTestTags.ADD_RUN_BUTTON).performClick()
        node(PlayStatusPanelTestTags.ADD_RUN_BUTTON).performClick()
        repeat(4) { addPlatinumChip().action().performClick() }

        assertEquals(1, stateChanges)
        assertEquals(2, playTimeEdits)
        assertEquals(3, runAdditions)
        assertEquals(4, platinumRegistrations)
    }

    @Test
    fun clicking_the_obtained_platinum_triggers_onRegisterPlatinum() {
        setPanelContent(playStatus = finishedWithPlatinum)

        platinumChip().action().performClick()

        assertEquals(1, platinumRegistrations)
    }

    @Test
    fun disabled_actions_are_not_enabled_and_do_not_trigger_their_callbacks() {
        setPanelContent(
            isStateChangeEnabled = false,
            isAddRunEnabled = false,
            isRegisterPlatinumEnabled = false
        )

        node(PlayStatusPanelTestTags.CHANGE_STATE_BUTTON).assertIsNotEnabled().performClick()
        node(PlayStatusPanelTestTags.ADD_RUN_BUTTON).assertIsNotEnabled().performClick()
        addPlatinumChip().action().assertIsNotEnabled().performClick()
        node(PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON).assertIsEnabled()

        assertEquals(0, stateChanges)
        assertEquals(0, runAdditions)
        assertEquals(0, platinumRegistrations)
    }
}
