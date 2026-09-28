package pt.isel.lei.pdm.counter

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyMaxEditor
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyView
import pt.isel.lei.pdm.counter.ui.crowdtally.TestTags

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("pt.isel.lei.pdm.counter", appContext.packageName)
    }

    @Test
    fun `CrowdTallyViewIncrementButtonReflectsOnUI`() {
        // carregar no butao
        // ver texto

        var incrementClicked = false
        composeTestRule.setContent {
            CrowdTallyView(
                state = CrowdTallyInfo(123, 200),
                increment = {
                    incrementClicked = true
                },
                decrement = { }
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CounterText)
            .assertTextEquals("123")

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.IncrementButton)
            .performClick()


        assertTrue(incrementClicked)
    }

    @Test
    fun `CrowdTallyViewCounterExists`() {
        // carregar no butao
        // ver texto

        composeTestRule.setContent {
            CrowdTallyView(
                state = CrowdTallyInfo(123, 200),
                increment = { },
                decrement = { }
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CounterText)
            .assertTextEquals("123")


    }

    @Test
    fun `CrowdTallyEditorCorrectlyEditsMaxValue`() {

        var newMax = -1
        composeTestRule.setContent {
            CrowdTallyMaxEditor(
                arg = CrowdTallyInfo(10, 10),
                onNewMax = {
                    newMax = it
                },
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.EditorTextBox)
            .performTextReplacement("123")

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.EditorSaveButton)
            .performClick()

        assertEquals(123, newMax)


    }


    @Test
    fun `CrowdTallyScreenStateSurvivesScreenRotation`() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            CrowdTallyScreen(
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CounterText)
            .assertTextEquals("0")

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.IncrementButton)
            .performClick()

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CounterText)
            .assertTextEquals("1")

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CounterText)
            .assertTextEquals("1")

    }

    @Test
    fun `CrowdTallyMaxEditorTextSurvivesReconfiguration`() {
        val restorationTester = StateRestorationTester(composeTestRule)

        restorationTester.setContent {
            CrowdTallyMaxEditor(
                arg = CrowdTallyInfo(0,10),
                onNewMax = {},
            )

        }


        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.EditorTextBox)
            .performTextReplacement("123")

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.EditorTextBox)
            .assertTextEquals("123")



    }
}