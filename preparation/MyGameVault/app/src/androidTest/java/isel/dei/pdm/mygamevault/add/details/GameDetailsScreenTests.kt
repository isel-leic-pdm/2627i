package isel.dei.pdm.mygamevault.add.details

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.GameDetails
import isel.dei.pdm.mygamevault.setTestContent
import isel.dei.pdm.mygamevault.ui.common.FATAL_ERROR_VIEW_TAG
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GameDetailsScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGame = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), null as String?)
    private val testDetails = GameDetails(
        game = testGame,
        description = "Epic RPG",
        developers = listOf("FromSoftware"),
        publishers = listOf("Bandai Namco"),
        genres = listOf("RPG")
    )

    @Test
    fun loadedState_displaysAllInformation() {
        // Act
        composeTestRule.setTestContent {
            GameDetailsScreenView(
                state = GameDetailsScreenState.Loaded(testDetails),
                onRetryRequested = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Elden Ring").assertIsDisplayed()
        composeTestRule.onNodeWithText("Epic RPG").assertIsDisplayed()
        composeTestRule.onNodeWithText("FromSoftware").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bandai Namco").assertIsDisplayed()
        composeTestRule.onNodeWithText("RPG").assertIsDisplayed()
        composeTestRule.onNodeWithText("2022-02-25").assertIsDisplayed()
    }

    @Test
    fun errorState_displaysErrorView() {
        // Act
        composeTestRule.setTestContent {
            GameDetailsScreenView(
                state = GameDetailsScreenState.Error(Exception("Fail")),
                onRetryRequested = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(FATAL_ERROR_VIEW_TAG).assertIsDisplayed()
    }
}
