package isel.dei.pdm.mygamevault.add

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.ui.common.GAME_LIST_ITEM_TAG
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
}
