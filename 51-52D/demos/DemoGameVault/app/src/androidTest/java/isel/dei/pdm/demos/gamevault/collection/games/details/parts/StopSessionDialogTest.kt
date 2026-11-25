package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags.DISCARD_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags.DISMISS_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags.ELAPSED_TIME_MESSAGE
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags.REGISTER_BUTTON
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialogTestTags.STOP_BUTTON
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class StopSessionDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * The outcomes of the dialog: whether the session was registered, or null if dismissed.
     */
    private val outcomes = mutableListOf<Boolean?>()

    private fun setDialogContent(elapsedTime: Duration, isShort: Boolean, isLeaving: Boolean) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                StopSessionDialog(
                    elapsedTime = elapsedTime,
                    isShort = isShort,
                    isLeaving = isLeaving,
                    onStop = { outcomes += it },
                    onDismiss = { outcomes += null }
                )
            }
        }
    }

    private fun onText(resId: Int) = composeTestRule.onNodeWithText(context.getString(resId))

    // Stopping

    @Test
    fun stopping_tells_the_time_to_be_added_and_stops_registering_it() {
        setDialogContent(elapsedTime = 72.minutes, isShort = false, isLeaving = false)

        onText(R.string.game_details_stop_dialog_title).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ELAPSED_TIME_MESSAGE)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_message, "1 h 12 min"))
        composeTestRule.onNodeWithTag(DISMISS_BUTTON)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_keep_playing))
        composeTestRule.onNodeWithTag(DISCARD_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON).assertDoesNotExist()

        composeTestRule.onNodeWithTag(STOP_BUTTON)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_stop))
            .performClick()

        assertEquals(listOf<Boolean?>(true), outcomes)
    }

    @Test
    fun keeping_playing_dismisses_the_dialog() {
        setDialogContent(elapsedTime = 72.minutes, isShort = false, isLeaving = false)

        composeTestRule.onNodeWithTag(DISMISS_BUTTON).performClick()

        assertEquals(listOf<Boolean?>(null), outcomes)
    }

    // Short sessions

    @Test
    fun stopping_a_short_session_asks_whether_to_register_it() {
        setDialogContent(elapsedTime = 3.minutes, isShort = true, isLeaving = false)

        onText(R.string.game_details_stop_dialog_short_title).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ELAPSED_TIME_MESSAGE)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_short_message, "3 min"))
        composeTestRule.onNodeWithTag(STOP_BUTTON).assertDoesNotExist()
        composeTestRule.onNodeWithTag(DISMISS_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DISCARD_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON).assertIsDisplayed()
    }

    @Test
    fun registering_a_short_session_stops_registering_it() {
        setDialogContent(elapsedTime = 3.minutes, isShort = true, isLeaving = false)

        composeTestRule.onNodeWithTag(REGISTER_BUTTON).performClick()

        assertEquals(listOf<Boolean?>(true), outcomes)
    }

    @Test
    fun discarding_a_short_session_stops_without_registering_it() {
        setDialogContent(elapsedTime = 3.minutes, isShort = true, isLeaving = false)

        composeTestRule.onNodeWithTag(DISCARD_BUTTON).performClick()

        assertEquals(listOf<Boolean?>(false), outcomes)
    }

    @Test
    fun a_session_in_its_first_minute_lasted_less_than_a_minute() {
        setDialogContent(elapsedTime = 20.seconds, isShort = true, isLeaving = false)

        composeTestRule.onNodeWithTag(ELAPSED_TIME_MESSAGE)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_just_started_message))
    }

    // Leaving

    @Test
    fun leaving_tells_that_it_stops_the_session_and_offers_to_stay() {
        setDialogContent(elapsedTime = 72.minutes, isShort = false, isLeaving = true)

        onText(R.string.game_details_stop_dialog_leave_title).assertIsDisplayed()
        onText(R.string.game_details_stop_dialog_leave_message).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DISMISS_BUTTON)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_stay))

        composeTestRule.onNodeWithTag(STOP_BUTTON)
            .assertTextEquals(context.getString(R.string.game_details_stop_dialog_stop_and_leave))
            .performClick()

        assertEquals(listOf<Boolean?>(true), outcomes)
    }

    @Test
    fun leaving_during_a_short_session_asks_whether_to_register_it() {
        setDialogContent(elapsedTime = 3.minutes, isShort = true, isLeaving = true)

        onText(R.string.game_details_stop_dialog_leave_title).assertIsDisplayed()
        onText(R.string.game_details_stop_dialog_leave_message).assertIsDisplayed()
        composeTestRule.onNodeWithTag(DISCARD_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(REGISTER_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(STOP_BUTTON).assertDoesNotExist()

        composeTestRule.onNodeWithTag(DISMISS_BUTTON).performClick()

        assertEquals(listOf<Boolean?>(null), outcomes)
    }
}
