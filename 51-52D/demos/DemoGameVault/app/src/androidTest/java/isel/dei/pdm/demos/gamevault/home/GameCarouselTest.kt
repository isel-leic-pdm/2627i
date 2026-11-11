package isel.dei.pdm.demos.gamevault.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameCarouselTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")

    private fun entry(id: Long, name: String, platform: Platform, hours: Int = 0, minutes: Int = 0) =
        CollectionEntry(
            game = Game(id, name, releaseDate = null, coverUri = null),
            platforms = setOf(platform),
            playStatus = PlayStatus(PlayTime(hours, minutes), PlayStatus.State.PLAYING)
        ).toItem()

    private val eldenRing = entry(1L, "Elden Ring", ps5, hours = 42, minutes = 10)
    private val zelda = entry(2L, "Tears of the Kingdom", switch, hours = 87, minutes = 45)

    /**
     * The font scale used to display the carousel, simulating the user's font size setting.
     */
    private val fontScale = mutableFloatStateOf(1f)

    /**
     * Displays a carousel with the given items.
     */
    private fun setCarouselContent(
        items: List<CollectionItem>,
        onItemSelected: (CollectionItem) -> Unit = {}
    ) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale.floatValue)
            ) {
                GameCarousel(items = items, onItemSelected = onItemSelected)
            }
        }
    }

    /**
     * Gets the text layout of the given text node, to inspect its number of lines.
     */
    private fun SemanticsNodeInteraction.textLayout(): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.first()
    }

    private fun titleNode() =
        composeTestRule.onNodeWithTag(GameCarouselTestTags.TITLE, useUnmergedTree = true)

    private fun detailsNode() =
        composeTestRule.onNodeWithTag(GameCarouselTestTags.DETAILS, useUnmergedTree = true)

    // Content

    @Test
    fun carousel_displays_one_item_per_entry() {
        setCarouselContent(listOf(eldenRing, zelda))

        composeTestRule
            .onNodeWithTag(GameCarouselTestTags.CAROUSEL)
            .assertIsDisplayed()

        composeTestRule
            .onAllNodesWithTag(GameCarouselTestTags.ITEM)
            .assertCountEquals(2)
    }

    @Test
    fun carousel_displays_no_items_when_there_are_none() {
        setCarouselContent(emptyList())

        composeTestRule
            .onAllNodesWithTag(GameCarouselTestTags.ITEM)
            .assertCountEquals(0)
    }

    @Test
    fun item_displays_game_name_and_platform_and_play_time() {
        setCarouselContent(listOf(eldenRing))

        val expectedDetails = InstrumentationRegistry.getInstrumentation().targetContext
            .getString(R.string.game_card_platform_and_play_time, "PS5", 42, 10)

        composeTestRule
            .onNodeWithText("Elden Ring")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(expectedDetails)
            .assertIsDisplayed()
    }

    // Text size bounds

    @Test
    fun title_never_exceeds_two_lines() {
        fontScale.floatValue = 2f
        val longName = "A Very Long Game Name That Goes On And On: The Definitive Remastered Edition"
        setCarouselContent(listOf(entry(1L, longName, ps5)))

        val layout = titleNode().textLayout()
        assertEquals(2, layout.lineCount)
        assertTrue(layout.hasVisualOverflow)
    }

    @Test
    fun details_never_exceed_one_line() {
        fontScale.floatValue = 2f
        setCarouselContent(listOf(entry(1L, "Elden Ring", ps5, hours = 12345, minutes = 59)))

        assertEquals(1, detailsNode().textLayout().lineCount)
    }

    @Test
    fun text_follows_the_font_scale_up_to_the_maximum() {
        setCarouselContent(listOf(eldenRing))

        fontScale.floatValue = 1f
        val heightAtDefaultScale = titleNode().fetchSemanticsNode().size.height

        fontScale.floatValue = MAX_OVERLAY_FONT_SCALE
        val heightAtMaxScale = titleNode().fetchSemanticsNode().size.height

        fontScale.floatValue = 2f
        val heightAboveMaxScale = titleNode().fetchSemanticsNode().size.height

        assertTrue(heightAtMaxScale > heightAtDefaultScale)
        assertEquals(heightAtMaxScale, heightAboveMaxScale)
    }

    // Interaction

    @Test
    fun clicking_item_triggers_onItemSelected_with_it() {
        var selected: CollectionItem? = null
        setCarouselContent(listOf(eldenRing, zelda), onItemSelected = { selected = it })

        composeTestRule
            .onAllNodesWithTag(GameCarouselTestTags.ITEM)[1]
            .performClick()

        assertEquals(zelda, selected)
    }

    @Test
    fun swiping_reveals_items_beyond_the_visible_ones() {
        val items = (1L..8L).map { entry(it, "Game $it", ps5) }
        setCarouselContent(items)

        // Initially, only the first items are composed
        composeTestRule
            .onNodeWithText("Game 8")
            .assertDoesNotExist()

        repeat(items.size) {
            composeTestRule
                .onNodeWithTag(GameCarouselTestTags.CAROUSEL)
                .performTouchInput { swipeLeft() }
        }

        composeTestRule
            .onNodeWithText("Game 8")
            .assertExists()
    }
}
