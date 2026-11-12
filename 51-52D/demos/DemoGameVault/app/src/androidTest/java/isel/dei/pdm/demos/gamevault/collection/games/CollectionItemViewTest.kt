package isel.dei.pdm.demos.gamevault.collection.games

import android.graphics.Color
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CollectionItemViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    private val sampleItem = CollectionItem(
        gameId = 1L,
        name = NonBlankString("Elden Ring"),
        releaseYear = Year(2022),
        coverUri = null,
        thumbnailUri = Uri("http://example.com/elden-ring.png"),
        platformAbbreviations = listOf(NonBlankString("PS5")),
        state = PlayStatus.State.PLAYING,
        timeSpent = PlayTime(45, 30),
        isFinished = false,
        hasPlatinum = false,
        isSessionActive = false
    )

    private val notCategorizedItem = sampleItem.copy(
        state = PlayStatus.State.NONE,
        timeSpent = PlayTime()
    )

    /**
     * Displays the view for the given item.
     */
    private fun setItemContent(item: CollectionItem, onSelected: () -> Unit = {}) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                CollectionItemView(item = item, onSelected = onSelected)
            }
        }
    }

    // Content

    @Test
    fun collection_item_displays_content() {
        setItemContent(sampleItem)

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.ITEM)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.NAME, useUnmergedTree = true)
            .assertTextEquals("Elden Ring")

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLATFORM, useUnmergedTree = true)
            .assertTextEquals("PS5")

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.THUMBNAIL, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun collection_item_displays_all_the_platforms_the_game_is_owned_on() {
        setItemContent(
            sampleItem.copy(platformAbbreviations = listOf(NonBlankString("PS5"), NonBlankString("PC")))
        )

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLATFORM, useUnmergedTree = true)
            .assertTextEquals("PS5, PC")
    }

    // Time spent

    @Test
    fun collection_item_displays_whole_hours_of_time_spent() {
        setItemContent(sampleItem.copy(timeSpent = PlayTime(120, 59)))

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLAY_TIME, useUnmergedTree = true)
            .assertTextEquals("120 h")
    }

    @Test
    fun collection_item_displays_minutes_of_time_spent_when_less_than_an_hour() {
        setItemContent(sampleItem.copy(timeSpent = PlayTime(minutes = 35)))

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLAY_TIME, useUnmergedTree = true)
            .assertTextEquals("35 min")
    }

    @Test
    fun collection_item_does_not_display_time_spent_when_never_played() {
        setItemContent(sampleItem.copy(state = PlayStatus.State.BACKLOG, timeSpent = PlayTime()))

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLAY_TIME, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    // Chips

    @Test
    fun collection_item_displays_play_state_chip_for_categorized_games() {
        val expectedLabels = mapOf(
            PlayStatus.State.BACKLOG to "Backlog",
            PlayStatus.State.PLAYING to "Playing",
            PlayStatus.State.PAUSED to "Paused",
            PlayStatus.State.FINISHED to "Finished",
            PlayStatus.State.DROPPED to "Dropped",
        )
        var state by mutableStateOf(PlayStatus.State.BACKLOG)
        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides successLoader) {
                CollectionItemView(item = sampleItem.copy(state = state), onSelected = {})
            }
        }

        expectedLabels.forEach { (expectedState, expectedLabel) ->
            state = expectedState
            composeTestRule
                .onNodeWithTag(PlayStateChipTestTags.CHIP, useUnmergedTree = true)
                .assertIsDisplayed()
            composeTestRule
                .onNodeWithText(expectedLabel, useUnmergedTree = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun collection_item_displays_only_platinum_chip_when_platinum_was_obtained() {
        setItemContent(sampleItem.copy(state = PlayStatus.State.FINISHED, isFinished = true, hasPlatinum = true))

        composeTestRule
            .onNodeWithTag(PlayStateChipTestTags.PLATINUM_CHIP, useUnmergedTree = true)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(PlayStateChipTestTags.CHIP, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun collection_item_does_not_display_platinum_chip_when_platinum_was_not_obtained() {
        setItemContent(sampleItem)

        composeTestRule
            .onNodeWithTag(PlayStateChipTestTags.PLATINUM_CHIP, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun collection_item_displays_neither_chips_nor_time_spent_for_not_categorized_games() {
        setItemContent(notCategorizedItem)

        composeTestRule
            .onNodeWithTag(PlayStateChipTestTags.CHIP, useUnmergedTree = true)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(PlayStateChipTestTags.PLATINUM_CHIP, useUnmergedTree = true)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.PLAY_TIME, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    // Interaction

    @Test
    fun clicking_item_triggers_onSelected() {
        var selected = false
        setItemContent(item = sampleItem, onSelected = { selected = true })

        composeTestRule
            .onNodeWithTag(CollectionItemViewTestTags.ITEM)
            .performClick()

        assertTrue(selected)
    }
}
