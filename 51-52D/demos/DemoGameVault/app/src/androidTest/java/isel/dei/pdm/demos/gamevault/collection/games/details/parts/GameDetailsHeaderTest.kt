package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Rule
import org.junit.Test

class GameDetailsHeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")
    private val pc = Platform(3L, "PC", "pc (Microsoft Windows)")

    private fun setHeaderContent(name: String = "Elden Ring", platforms: Collection<Platform>) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                GameDetailsHeader(name = NonBlankString(name), platforms = platforms)
            }
        }
    }

    private fun name() = composeTestRule.onNodeWithTag(GameDetailsHeaderTestTags.NAME)

    private fun platforms() = composeTestRule.onNodeWithTag(GameDetailsHeaderTestTags.PLATFORMS)

    @Test
    fun name_is_displayed() {
        setHeaderContent(name = "Elden Ring", platforms = setOf(ps5))

        name().assertIsDisplayed().assertTextEquals("Elden Ring")
    }

    @Test
    fun platform_is_displayed_by_its_full_name() {
        setHeaderContent(platforms = setOf(ps5))

        platforms().assertIsDisplayed().assertTextEquals("PlayStation 5")
    }

    @Test
    fun platforms_are_displayed_comma_separated_and_sorted_by_name_ignoring_case() {
        setHeaderContent(platforms = setOf(switch, ps5, pc))

        platforms().assertTextEquals("Nintendo Switch, pc (Microsoft Windows), PlayStation 5")
    }

    @Test
    fun platform_versions_are_not_displayed() {
        setHeaderContent(platforms = setOf(Platform(4L, "PS5", "PlayStation 5", version = "Pro")))

        platforms().assertTextEquals("PlayStation 5")
    }

    @Test
    fun platforms_are_not_displayed_when_there_are_none() {
        setHeaderContent(platforms = emptySet())

        name().assertIsDisplayed()
        platforms().assertDoesNotExist()
    }
}
