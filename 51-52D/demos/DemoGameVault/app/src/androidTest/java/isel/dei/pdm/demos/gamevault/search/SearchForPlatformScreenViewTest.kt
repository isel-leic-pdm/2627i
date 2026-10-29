package isel.dei.pdm.demos.gamevault.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.search.platforms.PlatformAddStatus
import isel.dei.pdm.demos.gamevault.search.platforms.PlatformSearchItemUiState
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenView
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.SearchBoxTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchForPlatformScreenViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    private val samplePlatforms = listOf(
        PlatformSearchItemUiState(
            PlatformInfo(1L, "PS5", "PlayStation 5", "https://fake.url/ps5.png"),
            addStatus = PlatformAddStatus.NOT_ADDED
        ),
        PlatformSearchItemUiState(
            PlatformInfo(2L, "XSX", "Xbox Series X/S", "https://fake.url/xbox.png"),
            addStatus = PlatformAddStatus.ADDED
        )
    )

    @Test
    fun screen_displays_top_app_bar_title_and_back_button() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = samplePlatforms,
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = {},
                        onBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(SearchForPlatformScreenViewTestTags.SCREEN_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.search_platform_title))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SearchForPlatformScreenViewTestTags.BACK_BUTTON_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_back_button_triggers_onBack_callback() {
        var backClicked = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = samplePlatforms,
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = {},
                        onBack = { backClicked = true }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(SearchForPlatformScreenViewTestTags.BACK_BUTTON_TAG)
            .performClick()

        assertTrue(backClicked)
    }

    @Test
    fun search_box_and_results_header_are_displayed() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = samplePlatforms,
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = {},
                        onBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(SearchBoxTestTags.INPUT)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.search_results_label))
            .assertIsDisplayed()
    }

    @Test
    fun displays_list_of_platforms_when_not_empty() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = samplePlatforms,
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = {},
                        onBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(SearchForPlatformScreenViewTestTags.PLATFORM_LIST_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("PlayStation 5")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Xbox Series X/S")
            .assertIsDisplayed()
    }

    @Test
    fun selecting_platform_item_triggers_onPlatformSelected_callback() {
        var selectedPlatform: PlatformInfo? = null

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = samplePlatforms,
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = { selectedPlatform = it },
                        onBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText("PlayStation 5")
            .performClick()

        assertEquals(samplePlatforms[0].platform, selectedPlatform)
    }

    @Test
    fun displays_empty_state_message_when_platforms_list_is_empty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreenView(
                        platforms = emptyList(),
                        onSearchQueryChange = {},
                        onSearchQueryCleared = {},
                        onPlatformSelected = {},
                        onBack = {}
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.search_no_platforms_found))
            .assertIsDisplayed()
    }
}
