package isel.dei.pdm.demos.gamevault.search.games

import android.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags
import isel.dei.pdm.demos.gamevault.ui.common.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GameSearchItemViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val ps5Pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro")

    private val sampleGame = GameInfo(
        id = 1L,
        name = "Elden Ring",
        releaseDate = LocalDate.of(2022, 2, 25),
        thumbnailUri = "http://example.com/elden-ring.png"
    )

    /**
     * Displays a list item for the given item state.
     */
    private fun setItemContent(item: GameSearchItemUiState, onSelected: () -> Unit = {}) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                GameSearchItemView(item = item, onSelected = onSelected)
            }
        }
    }

    private fun assertDoesNotExist(tag: String) {
        composeTestRule.onNodeWithTag(tag, useUnmergedTree = true).assertDoesNotExist()
    }

    // Content

    @Test
    fun game_search_item_displays_content() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5))

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.ITEM)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.NAME, useUnmergedTree = true)
            .assertTextEquals(sampleGame.name.value)

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.THUMBNAIL, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun game_search_item_displays_the_platform_abbreviation() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5))

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.PLATFORM, useUnmergedTree = true)
            .assertTextEquals("PS5")
    }

    @Test
    fun game_search_item_does_not_display_the_platform_version() {
        // Games are oblivious of the platforms' versions
        setItemContent(GameSearchItemUiState(sampleGame, ps5Pro))

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.PLATFORM, useUnmergedTree = true)
            .assertTextEquals("PS5")
    }

    @Test
    fun game_search_item_displays_add_icon_when_not_added() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5, AddStatus.NOT_ADDED))

        composeTestRule
            .onNodeWithTag(AddStatusIndicatorTestTags.ADD_ICON, useUnmergedTree = true)
            .assertIsDisplayed()
        assertDoesNotExist(AddStatusIndicatorTestTags.ADDING_INDICATOR)
        assertDoesNotExist(AddStatusIndicatorTestTags.ADDED_ICON)
    }

    @Test
    fun game_search_item_displays_progress_indicator_when_adding() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5, AddStatus.ADDING))

        composeTestRule
            .onNodeWithTag(AddStatusIndicatorTestTags.ADDING_INDICATOR)
            .assertIsDisplayed()
        assertDoesNotExist(AddStatusIndicatorTestTags.ADD_ICON)
    }

    @Test
    fun game_search_item_displays_check_icon_when_added() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5, AddStatus.ADDED))

        composeTestRule
            .onNodeWithTag(AddStatusIndicatorTestTags.ADDED_ICON, useUnmergedTree = true)
            .assertIsDisplayed()
        assertDoesNotExist(AddStatusIndicatorTestTags.ADD_ICON)
    }

    // Interaction

    @Test
    fun clicking_item_triggers_onSelected_when_not_added() {
        var selected = false
        setItemContent(
            item = GameSearchItemUiState(sampleGame, ps5, AddStatus.NOT_ADDED),
            onSelected = { selected = true }
        )

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.ITEM)
            .performClick()

        assertTrue(selected)
    }

    @Test
    fun clicking_add_icon_triggers_onSelected() {
        var selected = false
        setItemContent(
            item = GameSearchItemUiState(sampleGame, ps5, AddStatus.NOT_ADDED),
            onSelected = { selected = true }
        )

        composeTestRule
            .onNodeWithTag(AddStatusIndicatorTestTags.ADD_ICON, useUnmergedTree = true)
            .performClick()

        assertTrue(selected)
    }

    @Test
    fun item_has_no_click_action_when_adding() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5, AddStatus.ADDING))

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.ITEM)
            .assertHasNoClickAction()
    }

    @Test
    fun item_has_no_click_action_when_added() {
        setItemContent(GameSearchItemUiState(sampleGame, ps5, AddStatus.ADDED))

        composeTestRule
            .onNodeWithTag(GameSearchItemViewTestTags.ITEM)
            .assertHasNoClickAction()
    }
}
