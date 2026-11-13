package isel.dei.pdm.demos.gamevault.collection.platforms

import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.ADD_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.LOADING_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.search.platforms.ScriptedPlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [PlatformCollectionScreen], with an actual view model whose dependencies are scripted
 * fakes. Besides the screen's own responsibilities (the top bar and the add button), they check
 * that the screen is wired to the view model. The state of each screen is covered in detail by
 * [PlatformCollectionScreenViewTest].
 */
class PlatformCollectionScreenTest {

    private companion object {
        val SWITCH = Platform(1L, "Switch", "Nintendo Switch", "https://fake.url/switch.png")
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Displays the screen, with a view model that uses the given dependencies.
     */
    private fun setScreenContent(
        repository: PlatformCollectionRepository = ScriptedPlatformCollectionRepository(),
        onAddPlatform: () -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            // Created on the main thread, as the view model expects
            val viewModel = remember { PlatformCollectionViewModel(repository) }
            DemoGameVaultTheme {
                PlatformCollectionScreen(
                    viewModel = viewModel,
                    onAddPlatform = onAddPlatform,
                    onBack = onBack
                )
            }
        }
    }

    private fun assertTextIsDisplayed(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
    }

    private fun clickRetry() {
        composeTestRule
            .onNodeWithText(context.getString(R.string.platform_collection_retry_button_label))
            .performClick()
    }

    // Top bar and add button

    @Test
    fun screen_displays_title_back_button_and_add_button() {
        setScreenContent()

        composeTestRule.onNodeWithTag(SCREEN_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.platform_collection_title)
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ADD_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(onBack = { backClicked = true })

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()

        assertTrue(backClicked)
    }

    @Test
    fun clicking_add_button_triggers_onAddPlatform() {
        var addClicked = false
        setScreenContent(onAddPlatform = { addClicked = true })

        composeTestRule.onNodeWithTag(ADD_BUTTON_TAG).performClick()

        assertTrue(addClicked)
    }

    // View model wiring

    @Test
    fun empty_collection_displays_the_empty_collection_message() {
        setScreenContent()

        assertTextIsDisplayed(R.string.platform_collection_empty)
        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertDoesNotExist()
    }

    @Test
    fun platforms_in_the_collection_are_displayed() {
        setScreenContent(
            repository = ScriptedPlatformCollectionRepository(initialPlatforms = listOf(SWITCH))
        )

        composeTestRule.onNodeWithText(SWITCH.name.value).assertIsDisplayed()
    }

    // Errors

    @Test
    fun failed_read_displays_error_view_instead_of_the_collection_view() {
        setScreenContent(
            repository = ScriptedPlatformCollectionRepository(
                readErrors = listOf(UnrecoverablePersistenceException())
            )
        )

        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.platform_collection_error_message)
        assertTextIsDisplayed(R.string.platform_collection_retry_button_label)
        assertTextIsDisplayed(R.string.platform_collection_error_tip_unrecoverable)
        composeTestRule
            .onNodeWithText(context.getString(R.string.platform_collection_empty))
            .assertDoesNotExist()
        // The user can still leave the screen or add platforms
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ADD_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun retrying_displays_the_platforms_in_the_collection() {
        val repository = ScriptedPlatformCollectionRepository(
            readErrors = listOf(RecoverablePersistenceException()),
            initialPlatforms = listOf(SWITCH)
        )
        setScreenContent(repository = repository)

        clickRetry()

        composeTestRule.onNodeWithText(SWITCH.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertDoesNotExist()
        assertEquals(2, repository.readCount)
    }

    @Test
    fun retrying_displays_the_error_view_again_if_the_read_fails_again() {
        val repository = ScriptedPlatformCollectionRepository(
            readErrors = listOf(
                RecoverablePersistenceException(),
                UnrecoverablePersistenceException()
            )
        )
        setScreenContent(repository = repository)
        assertTextIsDisplayed(R.string.platform_collection_error_tip_recoverable)

        clickRetry()

        assertTextIsDisplayed(R.string.platform_collection_error_tip_unrecoverable)
        assertEquals(2, repository.readCount)
    }
}
