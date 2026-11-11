package isel.dei.pdm.demos.gamevault.collection.platforms

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.EMPTY_MESSAGE_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.LOADING_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.PLATFORM_LIST_TAG
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemViewTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlatformCollectionScreenViewTest {

    private companion object {
        val PS5_PRO = Platform(1L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", 2021, "Pro")
        val PS5 = Platform(2L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", 2020)
        val SWITCH = Platform(3L, "Switch", "Nintendo Switch", "https://fake.url/switch.png")
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    /**
     * Displays the view for the given state. The state is read during composition, so tests can
     * change it to drive the view through state transitions.
     */
    private fun setViewContent(
        state: MutableState<PlatformCollectionScreenState>,
        onPlatformSelected: (Platform) -> Unit = {}
    ) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    PlatformCollectionScreenView(
                        state = state.value,
                        onPlatformSelected = onPlatformSelected
                    )
                }
            }
        }
    }

    /**
     * Displays the view for the given (fixed) state.
     */
    private fun setViewContent(
        state: PlatformCollectionScreenState,
        onPlatformSelected: (Platform) -> Unit = {}
    ) = setViewContent(mutableStateOf(state), onPlatformSelected)

    private fun assertTextIsDisplayed(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertIsDisplayed()
    }

    private fun assertTextDoesNotExist(resId: Int) {
        composeTestRule.onNodeWithText(context.getString(resId)).assertDoesNotExist()
    }

    // Loading

    @Test
    fun loading_state_displays_the_indicator_only() {
        setViewContent(PlatformCollectionScreenState.Loading)

        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.platform_collection_loading)
        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(EMPTY_MESSAGE_TAG).assertDoesNotExist()
    }

    // Results

    @Test
    fun results_state_displays_one_item_per_platform() {
        setViewContent(PlatformCollectionScreenState.Results(listOf(PS5_PRO, PS5, SWITCH)))

        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(PlatformListItemViewTestTags.ITEM).assertCountEquals(3)
        composeTestRule.onNodeWithText(SWITCH.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(EMPTY_MESSAGE_TAG).assertDoesNotExist()
    }

    @Test
    fun items_are_displayed_as_navigable() {
        setViewContent(PlatformCollectionScreenState.Results(listOf(PS5_PRO, SWITCH)))

        composeTestRule
            .onAllNodesWithTag(PlatformListItemViewTestTags.CHEVRON_ICON, useUnmergedTree = true)
            .assertCountEquals(2)
        composeTestRule
            .onAllNodesWithTag(AddStatusIndicatorTestTags.ADD_ICON, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun empty_results_state_displays_the_empty_collection_message() {
        setViewContent(PlatformCollectionScreenState.Results(emptyList()))

        composeTestRule.onNodeWithTag(EMPTY_MESSAGE_TAG).assertIsDisplayed()
        assertTextIsDisplayed(R.string.platform_collection_empty)
        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertDoesNotExist()
    }

    @Test
    fun selecting_a_platform_version_triggers_onPlatformSelected_with_that_version() {
        var selectedPlatform: Platform? = null
        setViewContent(
            state = PlatformCollectionScreenState.Results(listOf(PS5, PS5_PRO)),
            onPlatformSelected = { selectedPlatform = it }
        )

        // The version is displayed as a suffix (e.g. "· Pro") next to the abbreviation
        composeTestRule.onNodeWithText(PS5_PRO.version!!.value, substring = true).performClick()

        assertEquals(PS5_PRO, selectedPlatform)
    }

    // Error

    @Test
    fun error_state_displays_nothing() {
        // The error is displayed by the screen, in place of this view
        setViewContent(PlatformCollectionScreenState.Error(UnrecoverablePersistenceException()))

        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PLATFORM_LIST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(EMPTY_MESSAGE_TAG).assertDoesNotExist()
    }

    // State transitions

    @Test
    fun loading_to_results_replaces_the_indicator_with_the_list() {
        val state = mutableStateOf<PlatformCollectionScreenState>(PlatformCollectionScreenState.Loading)
        setViewContent(state)
        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertIsDisplayed()

        state.value = PlatformCollectionScreenState.Results(listOf(SWITCH))

        composeTestRule.onNodeWithTag(LOADING_INDICATOR_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithText(SWITCH.name.value).assertIsDisplayed()
    }

    @Test
    fun results_are_updated_when_the_collection_changes() {
        val state = mutableStateOf<PlatformCollectionScreenState>(
            PlatformCollectionScreenState.Results(emptyList())
        )
        setViewContent(state)
        assertTextIsDisplayed(R.string.platform_collection_empty)

        state.value = PlatformCollectionScreenState.Results(listOf(SWITCH))

        assertTextDoesNotExist(R.string.platform_collection_empty)
        composeTestRule.onNodeWithText(SWITCH.name.value).assertIsDisplayed()
    }
}
