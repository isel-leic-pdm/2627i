package pt.isel.lei.pdm.counter

import androidx.compose.ui.test.assertTextEquals
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
import pt.isel.lei.pdm.counter.ui.TestTags
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyContent
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyMaxConfiguratorContent

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
    fun `CrowdTallyContent increment button calls increment callback`() {

        var incrementedCalled = false
        composeTestRule.setContent {
            CrowdTallyContent(
                state = CrowdTallyInfo(0, 10),
                increment = {
                    incrementedCalled = true
                },
                decrement = {},
            )
        }

        composeTestRule.onNodeWithTag(TestTags.CrowdTally.INCREMENT_BUTTON)
            .performClick()
        //localizar butao
        //carregar butao
        //ver se incrementou
        assertTrue(incrementedCalled)
    }

    @Test
    fun `CrowdTallyContent counter text displays correct value`() {
        composeTestRule.setContent {
            CrowdTallyContent(
                state = CrowdTallyInfo(123, 200),
                increment = {
                },
                decrement = {},
            )
        }

        composeTestRule.onNodeWithTag(TestTags.CrowdTally.COUNTER_TEXT)
            .assertTextEquals("123")
    }

    @Test
    fun `CrowdTallyMaxConfiguratorContent does send correct textfield value`() {
        var newMax = -1
        composeTestRule.setContent {
            CrowdTallyMaxConfiguratorContent(
                arg = CrowdTallyInfo(0, 10),
                onCapacityChanged = {
                    newMax = it
                },
            )
        }

        composeTestRule
            .onNodeWithTag(TestTags.CrowdTally.CONFIGURATOR_VALUE_TEXT)
            .performTextReplacement("123")

        composeTestRule.onNodeWithTag(TestTags.CrowdTally.CONFIGURATOR_VALUE_TEXT)
            .assertTextEquals("123")

        composeTestRule.onNodeWithTag(TestTags.CrowdTally.CONFIGURATOR_SAVE_BUTTON)
            .performClick()

        assertEquals(123, newMax)

    }

}