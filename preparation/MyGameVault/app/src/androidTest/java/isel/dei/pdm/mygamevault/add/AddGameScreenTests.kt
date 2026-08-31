package isel.dei.pdm.mygamevault.add

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platforms
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
    fun onAddRequested_whenSearching_isNotInvoked() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(
                    results = listOf(testGame),
                    selectedPlatform = Platforms.PS5,
                    selectedCategory = null
                ),
                searchQuery = "test",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onAddRequested = { selectedGame = it },
                onDetailsRequested = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(ADD_GAME_BUTTON_TAG).performClick()

        // Assert
        assertNull("Add click should have been ignored while searching", selectedGame)
    }

    @Test
    fun onDetailsRequested_whenSearching_isNotInvoked() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(
                    results = listOf(testGame),
                    selectedPlatform = Platforms.PS5,
                    selectedCategory = null
                ),
                searchQuery = "test",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onAddRequested = {},
                onDetailsRequested = { selectedGame = it }
            )
        }

        // Act
        composeTestRule.onNodeWithTag(VIEW_DETAILS_BUTTON_TAG).performClick()

        // Assert
        assertNull("Details click should have been ignored while searching", selectedGame)
    }

    @Test
    fun searchingOverlay_whenSearching_isDisplayed() {
        // Act
        composeTestRule.setContent {
            AddGameScreen(
                state = AddGameScreenState.Searching(
                    results = emptyList(),
                    selectedPlatform = Platforms.PS5,
                    selectedCategory = null
                ),
                searchQuery = "test",
                onQueryChange = {},
                onPlatformChange = {},
                onCategoryChange = {},
                onAddRequested = {},
                onDetailsRequested = {}
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
                onAddRequested = {},
                onDetailsRequested = {}
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
                onAddRequested = {},
                onDetailsRequested = {}
            )
        }

        // Assert
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG).assertCountEquals(2)
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG)[0]
            .assertTextContains("Test Game", substring = true)
    }
}
