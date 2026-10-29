package isel.dei.pdm.demos.gamevault.ui.common

import android.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PlatformInfoListItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    private val samplePlatform = PlatformInfo(
        id = 1L,
        abbreviation = "PS5",
        name = "PlayStation 5",
        logoUri = "http://example.com/ps5.png",
    )

    @Test
    fun platform_list_item_displays_content() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE),
                    onSelected = {},
                )
            }
        }

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
    fun platform_list_item_displays_add_button_when_addable() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ADD_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_progress_indicator_when_adding() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDING),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ADDING_INDICATOR)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_check_icon_when_added() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDED),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ADDED_ICON)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_chevron_icon_when_navigable() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.NAVIGABLE),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.CHEVRON_ICON)
            .assertIsDisplayed()
    }

    @Test
    fun clicking_item_with_onSelected_triggers_callback_when_addable() {
        var selected = false

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE),
                    onSelected = { selected = true },
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .performClick()

        assertEquals(true, selected)
    }

    @Test
    fun item_has_no_click_action_when_adding() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDING),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .assertHasNoClickAction()
    }

    @Test
    fun item_has_no_click_action_when_added() {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItem(
                    item = samplePlatform.toListItemModel(PlatformListItemMode.ADDED),
                    onSelected = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(PlatformListItemTestTags.ITEM)
            .assertHasNoClickAction()
    }
}
