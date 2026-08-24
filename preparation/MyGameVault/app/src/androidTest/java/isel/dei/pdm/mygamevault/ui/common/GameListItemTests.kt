package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.mygamevault.core.Game
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
                onGameSelected = {}
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
    fun gameListItem_whenClicked_invokesOnGameSelected() {
        // Arrange
        var selectedGame: Game? = null
        composeTestRule.setContent {
            GameListItem(
                game = testGame,
                onGameSelected = { selectedGame = it }
            )
        }

        // Act
        composeTestRule.onNodeWithTag(GAME_LIST_ITEM_TAG).performClick()

        // Assert
        assertEquals(testGame, selectedGame)
    }
}