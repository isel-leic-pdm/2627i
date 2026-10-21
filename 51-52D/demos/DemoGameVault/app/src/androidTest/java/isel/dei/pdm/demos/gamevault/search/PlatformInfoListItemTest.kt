package isel.dei.pdm.demos.gamevault.search

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.search.PlatformInfo
import isel.dei.pdm.demos.gamevault.search.platforms.PlatformListItem
import isel.dei.pdm.demos.gamevault.search.platforms.PlatformListItemTestTags
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlatformInfoListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    @Test
    fun platform_list_item_displays_content() {
        // Arrange & Act
        val platform = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "http://example.com/ps5.png",
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    platform = platform,
                    onSelected = {},
                )
            }
        }

        // Assert
        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.NAME, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ABBREVIATION, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.LOGO, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_item_with_onSelected_triggers_callback() {
        // Arrange
        var selectedPlatform: PlatformInfo? = null
        val platform = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "http://example.com/ps5.png",
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    platform = platform,
                    onSelected = { selectedPlatform = it },
                )
            }
        }

        // Act
        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .performClick()

        // Assert
        assertEquals(platform, selectedPlatform)
    }

    @Test
    fun platform_list_item_with_null_trailing_content_renders_correctly() {
        // Arrange & Act
        val platform = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "http://example.com/ps5.png",
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    platform = platform,
                    onSelected = {},
                    trailingContent = null,
                )
            }
        }

        composeTestRule.waitForIdle()

        // Assert
        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .assertIsDisplayed()
    }
}
