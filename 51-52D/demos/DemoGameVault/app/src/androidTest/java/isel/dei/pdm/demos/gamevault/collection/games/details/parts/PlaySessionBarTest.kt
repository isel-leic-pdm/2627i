package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PlaySessionBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var startClicks = 0
    private var stopClicks = 0

    private fun setContent(sessionElapsedTime: Duration?) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                PlaySessionBar(
                    sessionElapsedTime = sessionElapsedTime,
                    onStart = { startClicks++ },
                    onStop = { stopClicks++ }
                )
            }
        }
    }

    private fun startButton() = composeTestRule.onNodeWithTag(PlaySessionBarTestTags.START_BUTTON)

    private fun stopButton() = composeTestRule.onNodeWithTag(PlaySessionBarTestTags.STOP_BUTTON)

    private fun elapsedTime() = composeTestRule.onNodeWithTag(PlaySessionBarTestTags.ELAPSED_TIME)

    @Test
    fun without_active_session_only_start_is_offered() {
        setContent(sessionElapsedTime = null)

        startButton().assertIsDisplayed()
        composeTestRule.onNodeWithText("Start Playing").assertIsDisplayed()
        stopButton().assertDoesNotExist()
        elapsedTime().assertDoesNotExist()
    }

    @Test
    fun with_active_session_only_stop_is_offered_with_the_elapsed_time() {
        setContent(sessionElapsedTime = 1.hours + 12.minutes)

        stopButton().assertIsDisplayed()
        composeTestRule.onNodeWithText("Stop Playing").assertIsDisplayed()
        elapsedTime().assertIsDisplayed().assertTextEquals("Playing for 1 h 12 min")
        startButton().assertDoesNotExist()
    }

    @Test
    fun elapsed_time_under_an_hour_is_displayed_in_minutes() {
        setContent(sessionElapsedTime = 12.minutes)

        elapsedTime().assertTextEquals("Playing for 12 min")
    }

    @Test
    fun elapsed_time_is_displayed_in_whole_minutes() {
        setContent(sessionElapsedTime = 12.minutes + 59.seconds)

        elapsedTime().assertTextEquals("Playing for 12 min")
    }

    @Test
    fun during_the_first_minute_the_session_is_displayed_as_just_started() {
        setContent(sessionElapsedTime = 59.seconds)

        elapsedTime().assertTextEquals("Just started")
    }

    @Test
    fun after_the_first_minute_the_elapsed_time_is_displayed() {
        setContent(sessionElapsedTime = 1.minutes)

        elapsedTime().assertTextEquals("Playing for 1 min")
    }

    @Test
    fun clicking_start_triggers_onStart_only() {
        setContent(sessionElapsedTime = null)

        startButton().performClick()

        assertEquals(1, startClicks)
        assertEquals(0, stopClicks)
    }

    @Test
    fun clicking_stop_triggers_onStop_only() {
        setContent(sessionElapsedTime = 1.minutes)

        stopButton().performClick()

        assertEquals(0, startClicks)
        assertEquals(1, stopClicks)
    }
}
