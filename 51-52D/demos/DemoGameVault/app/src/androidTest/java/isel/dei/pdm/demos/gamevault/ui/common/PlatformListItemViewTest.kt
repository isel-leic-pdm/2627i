package isel.dei.pdm.demos.gamevault.ui.common

import android.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PlatformListItemViewTest {

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

    /**
     * Displays a list item for the given model.
     */
    private fun setItemContent(item: PlatformListItemModel, onSelected: () -> Unit = {}) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                PlatformListItemView(item = item, onSelected = onSelected)
            }
        }
    }

    // Content

    @Test
    fun platform_list_item_displays_content() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ITEM)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.NAME, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ABBREVIATION, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.LOGO, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_version_when_present() {
        val versionedPlatform = samplePlatform.copy(version = NonBlankString("Pro"))
        setItemContent(versionedPlatform.toListItemModel(PlatformListItemMode.ADDABLE))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.VERSION, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Pro", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_does_not_display_version_when_absent() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.VERSION, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    // Trailing indicators

    @Test
    fun platform_list_item_displays_add_button_when_addable() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ADD_BUTTON)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_progress_indicator_when_adding() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDING))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ADDING_INDICATOR)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_check_icon_when_added() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDED))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ADDED_ICON, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun platform_list_item_displays_chevron_icon_when_navigable() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.NAVIGABLE))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.CHEVRON_ICON, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    // Interaction

    @Test
    fun clicking_item_triggers_onSelected_when_addable() {
        var selected = false
        setItemContent(
            item = samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE),
            onSelected = { selected = true }
        )

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ITEM)
            .performClick()

        assertTrue(selected)
    }

    @Test
    fun clicking_add_button_triggers_onSelected() {
        var selected = false
        setItemContent(
            item = samplePlatform.toListItemModel(PlatformListItemMode.ADDABLE),
            onSelected = { selected = true }
        )

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ADD_BUTTON)
            .performClick()

        assertTrue(selected)
    }

    @Test
    fun clicking_item_triggers_onSelected_when_navigable() {
        var selected = false
        setItemContent(
            item = samplePlatform.toListItemModel(PlatformListItemMode.NAVIGABLE),
            onSelected = { selected = true }
        )

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ITEM)
            .performClick()

        assertTrue(selected)
    }

    @Test
    fun item_has_no_click_action_when_adding() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDING))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ITEM)
            .assertHasNoClickAction()
    }

    @Test
    fun item_has_no_click_action_when_added() {
        setItemContent(samplePlatform.toListItemModel(PlatformListItemMode.ADDED))

        composeTestRule
            .onNodeWithTag(PlatformListItemViewTestTags.ITEM)
            .assertHasNoClickAction()
    }
}
