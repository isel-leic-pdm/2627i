package isel.dei.pdm.mygamevault.collection

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.setTestContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

class CollectionEntryItemTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGame = Game(
        id = 1,
        name = "Elden Ring",
        releaseDate = LocalDate.of(2022, 2, 25),
        coverUri = null as String?
    )

    private val testEntry = CollectionEntry(
        game = testGame,
        platform = Platforms.PS5,
        playStatus = PlayStatus(
            state = PlayStatus.State.PLAYING,
            timeSpent = 50.hours
        ),
        addedAt = LocalDate.now()
    )

    @Test
    fun collectionEntryItem_displaysInformation() {
        // Arrange
        composeTestRule.setTestContent {
            CollectionEntryItem(
                entry = testEntry,
                onClick = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText(testGame.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithText(testEntry.platform.name.value).assertIsDisplayed()
        composeTestRule.onNodeWithText("Playing", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("50 h", substring = true).assertIsDisplayed()
    }

    @Test
    fun collectionEntryItem_whenClicked_invokesOnClick() {
        // Arrange
        var clicked = false
        composeTestRule.setTestContent {
            CollectionEntryItem(
                entry = testEntry,
                onClick = { clicked = true }
            )
        }

        // Act
        composeTestRule.onNodeWithText(testGame.name.value).performClick()

        // Assert
        assertTrue(clicked)
    }
}
