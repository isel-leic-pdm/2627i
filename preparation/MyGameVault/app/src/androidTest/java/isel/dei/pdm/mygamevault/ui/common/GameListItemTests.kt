package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.mygamevault.add.ADD_GAME_BUTTON_TAG
import isel.dei.pdm.mygamevault.add.GAME_NAME_TAG
import isel.dei.pdm.mygamevault.add.GAME_RELEASE_DATE_TAG
import isel.dei.pdm.mygamevault.add.GameListItem
import isel.dei.pdm.mygamevault.add.VIEW_DETAILS_BUTTON_TAG
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GameListItemTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGame = Game(
        id = 123,
        name = "Test Game",
        releaseDate = LocalDate.of(2024, 1, 1),
        coverUri = null as String?
    )

    @Test
    fun gameListItem_displaysGameInfo() {
        // Arrange
        composeTestRule.setContent {
            GameListItem(
                game = testGame,
                platform = Platforms.PS5,
                onAddRequested = {},
                onDetailsRequested = {}
            )
        }

        // Assert
        composeTestRule
            .onNodeWithTag(GAME_NAME_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains(testGame.name.value)

        composeTestRule
            .onNodeWithTag(GAME_RELEASE_DATE_TAG, useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains(testGame.releaseDate?.year.toString(), substring = true)
    }

    @Test
    fun gameListItem_whenAddClicked_invokesOnAddRequested() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListItem(
                game = testGame,
                platform = Platforms.PS5,
                onAddRequested = { selectedGame = it },
                onDetailsRequested = {}
            )
        }

        // Act
        composeTestRule.onNodeWithTag(ADD_GAME_BUTTON_TAG).performClick()

        // Assert
        assertEquals(testGame, selectedGame)
    }

    @Test
    fun gameListItem_whenDetailsClicked_invokesOnDetailsRequested() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListItem(
                game = testGame,
                platform = Platforms.PS5,
                onAddRequested = {},
                onDetailsRequested = { selectedGame = it }
            )
        }

        // Act
        composeTestRule.onNodeWithTag(VIEW_DETAILS_BUTTON_TAG).performClick()

        // Assert
        assertEquals(testGame, selectedGame)
    }
}
