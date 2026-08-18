package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertDoesNotExist
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
    fun gameListFromSearch_displaysHeaderAndItems() {
        // Arrange
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = {},
                results = testGames,
                onGameSelected = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(PLATFORM_SELECTOR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(GAME_LIST_HEADER_TAG).assertIsDisplayed()
        composeTestRule.onAllNodesWithTag(GAME_LIST_ITEM_TAG).assertCountEquals(2)
    }

    @Test
    fun gameListFromSearch_whenTextEntered_invokesOnQueryChange() {
        // Arrange
        var query = ""
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = query,
                onQueryChange = { query = it },
                selectedPlatform = "PS5",
                onPlatformClick = {},
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
    fun gameListFromSearch_whenPlatformClicked_invokesOnPlatformClick() {
        // Arrange
        var platformClicked = false
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = { platformClicked = true },
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
    fun gameListFromSearch_whenClickEnabled_invokesOnGameSelected() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = {},
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
    fun gameListFromSearch_whenClickDisabled_ignoresClicks() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = {},
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
    fun gameListFromSearch_whenSearching_displaysOverlay() {
        // Act
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "test",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = {},
                results = emptyList(),
                onGameSelected = {},
                isSearching = true
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertIsDisplayed()
    }

    @Test
    fun gameListFromSearch_whenNotSearching_hidesOverlay() {
        // Act
        composeTestRule.setContent {
            GameListFromSearch(
                searchQuery = "test",
                onQueryChange = {},
                selectedPlatform = "PS5",
                onPlatformClick = {},
                results = emptyList(),
                onGameSelected = {},
                isSearching = false
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(SEARCHING_OVERLAY_TAG).assertDoesNotExist()
    }
}
