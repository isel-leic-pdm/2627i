package isel.dei.pdm.mygamevault.add

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.ui.common.GAME_LIST_ITEM_TAG
import isel.dei.pdm.mygamevault.ui.common.SEARCHING_OVERLAY_TAG
import isel.dei.pdm.mygamevault.ui.common.SEARCH_BAR_TAG
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AddGameScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGame = Game(
        id = 123,
        name = "Test Game",
        releaseDate = LocalDate.of(2024, 1, 1),
        coverUri = null as String?
    )

    @Test
    fun addGameScreen_whenSearching_ignoresClicksOnListItems() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(listOf(testGame)),
                searchQuery = "test",
                onQueryChange = {},
                onGameSelected = { selectedGame = it }
            )
        }

        // Act
        composeTestRule.onNodeWithTag(GAME_LIST_ITEM_TAG).performClick()

        // Assert
        assertNull("Click should have been ignored while searching", selectedGame)
    }

    @Test
    fun addGameScreen_whenSearching_showsSearchingOverlay() {
        // Act
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(emptyList()),
                searchQuery = "test",
                onQueryChange = {},
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertIsDisplayed()
    }

    @Test
    fun addGameScreen_whenTyping_updatesQuery() {
        // Arrange
        var query = ""
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Idle(emptyList()),
                searchQuery = query,
                onQueryChange = { query = it },
                onGameSelected = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).performTextInput("Elden")

        // Assert
        assertEquals("Elden", query)
    }

    @Test
    fun addGameScreen_withResults_displaysCorrectNumber() {
        // Arrange
        val games = listOf(
            testGame,
            testGame.copy(id = 456, name = "Another Game")
        )

        // Act
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Idle(games),
                searchQuery = "",
                onQueryChange = {},
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG)[0]
            .assertTextContains("Test Game", substring = true)
    }
}
