package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import isel.dei.pdm.mygamevault.core.Game
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GameListFromSearchTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGames = listOf(
        Game(1, "Game 1", LocalDate.of(2024, 1, 1), coverUri = null as String?),
        Game(2, "Game 2", LocalDate.of(2023, 1, 1), coverUri = null as String?)
    )

    @Test
    fun gameList_displaysHeaderAndItems() {
        // Arrange
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = testGames,
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PLATFORM_SELECTOR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CATEGORY_SELECTOR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(GAME_LIST_HEADER_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG).assertCountEquals(2)
    }

    @Test
    fun onQueryChange_whenTextEntered_isInvoked() {
        // Arrange
        var query = ""
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = query,
                onQueryChange = { query = it },
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = emptyList(),
                onGameSelected = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).performTextInput("Elden")

        // Assert
        assertEquals("Elden", query)
    }

    @Test
    fun onPlatformClick_whenPlatformSelected_isInvoked() {
        // Arrange
        var platformClicked = false
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = { platformClicked = true },
                selectedCategory = null,
                onCategoryClick = {},
                results = emptyList(),
                onGameSelected = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(PLATFORM_SELECTOR_TAG).performClick()

        // Assert
        assertTrue(platformClicked)
    }

    @Test
    fun onCategoryClick_whenCategorySelected_isInvoked() {
        // Arrange
        var categoryClicked = false
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = { categoryClicked = true },
                results = emptyList(),
                onGameSelected = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(CATEGORY_SELECTOR_TAG).performClick()

        // Assert
        assertTrue(categoryClicked)
    }

    @Test
    fun onGameSelected_whenClickEnabled_isInvoked() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = testGames,
                onGameSelected = { selectedGame = it },
                isClickEnabled = true
            )
        }

        // Act
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG)[0].performClick()

        // Assert
        assertEquals(testGames[0], selectedGame)
    }

    @Test
    fun onGameSelected_whenClickDisabled_isNotInvoked() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = testGames,
                onGameSelected = { selectedGame = it },
                isClickEnabled = false
            )
        }

        // Act
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG)[0].performClick()

        // Assert
        assertNull(selectedGame)
    }

    @Test
    fun searchingOverlay_whenSearching_isDisplayed() {
        // Act
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "test",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = emptyList(),
                onGameSelected = {},
                isSearching = true
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertIsDisplayed()
    }

    @Test
    fun searchingOverlay_whenNotSearching_doesNotExist() {
        // Act
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "test",
                onQueryChange = {},
                selectedPlatform = Game.Platform.PS5,
                onPlatformClick = {},
                selectedCategory = null,
                onCategoryClick = {},
                results = emptyList(),
                onGameSelected = {},
                isSearching = false
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertDoesNotExist()
    }
}
