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
import isel.dei.pdm.mygamevault.core.NonBlankString
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
    fun onGameSelected_whenSearching_isNotInvoked() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(
                    results = listOf(testGame),
                    selectedPlatform = Game.Platform.PS5,
                    selectedCategory = null
                ),
                searchQuery = "test",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onGameSelected = { selectedGame = it }
            )
        }

        // Act
        composeTestRule.onNodeWithTag(GAME_LIST_ITEM_TAG).performClick()

        // Assert
        assertNull("Click should have been ignored while searching", selectedGame)
    }

    @Test
    fun searchingOverlay_whenSearching_isDisplayed() {
        // Act
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(
                    results = emptyList(),
                    selectedPlatform = Game.Platform.PS5,
                    selectedCategory = null
                ),
                searchQuery = "test",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertIsDisplayed()
    }

    @Test
    fun onQueryChange_whenTyping_isInvoked() {
        // Arrange
        var query = ""
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Idle(),
                searchQuery = query,
                onQueryChange = { query = it },
                onPlatformChange = {},
                onCategoryChange = {},
                onGameSelected = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).performTextInput("Elden")

        // Assert
        assertEquals("Elden", query)
    }

    @Test
    fun gameList_whenIdleWithResults_displaysAllGames() {
        // Arrange
        val games = listOf(
            testGame,
            testGame.copy(id = 456, name = NonBlankString("Another Game"))
        )

        // Act
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Idle(results = games),
                searchQuery = "",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG)[0]
            .assertTextContains("Test Game", substring = true)
    }
}
