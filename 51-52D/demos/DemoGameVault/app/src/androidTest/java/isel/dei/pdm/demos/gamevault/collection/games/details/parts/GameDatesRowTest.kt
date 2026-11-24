package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test

class GameDatesRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val addedOn = LocalDate(2026, 11, 18)

    private fun setContent(releaseYear: Year?) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameDatesRow(releaseYear = releaseYear, addedOn = addedOn)
            }
        }
    }

    @Test
    fun displays_the_release_year() {
        setContent(releaseYear = Year(2022))

        composeTestRule.onNodeWithTag(GameDatesRowTestTags.RELEASED)
            .assertIsDisplayed()
            .assertTextEquals("Released: 2022")
    }

    @Test
    fun displays_the_date_the_game_was_added_on() {
        setContent(releaseYear = Year(2022))

        composeTestRule.onNodeWithTag(GameDatesRowTestTags.ADDED_ON)
            .assertIsDisplayed()
            .assertTextEquals("Added on: 2026-11-18")
    }

    @Test
    fun unknown_release_year_is_not_displayed() {
        setContent(releaseYear = null)

        composeTestRule.onNodeWithTag(GameDatesRowTestTags.RELEASED).assertDoesNotExist()
        composeTestRule.onNodeWithTag(GameDatesRowTestTags.ADDED_ON).assertIsDisplayed()
    }
}
