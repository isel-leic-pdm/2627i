package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformCriterionTests {

    // Platforms as provided by the search service
    private val gamePs5 = GamePlatform(167L, NonBlankString("PlayStation 5"), NonBlankString("PS5"))
    private val gamePs4 = GamePlatform(48L, NonBlankString("PlayStation 4"), NonBlankString("PS4"))
    private val gameSwitch = GamePlatform(
        id = 130L,
        name = NonBlankString("Nintendo Switch"),
        abbreviation = NonBlankString("Switch"),
        alternativeNames = listOf(NonBlankString("NSW"))
    )
    private val gameArcade = GamePlatform(52L, NonBlankString("Arcade"))

    private fun criterion(name: String, abbreviation: String) =
        PlatformCriterion(NonBlankString(name), NonBlankString(abbreviation))

    // Criterion of a platform

    @Test
    fun `platform is searched for by its name and abbreviation`() {
        // Arrange
        val platform = Platform(1L, "PS5", "PlayStation 5")

        // Act
        val criterion = platform.toSearchCriterion()

        // Assert
        assertEquals(criterion("PlayStation 5", "PS5"), criterion)
    }

    @Test
    fun `versions of a platform have equal criteria`() {
        // Arrange
        val standard = Platform(1L, "PS5", "PlayStation 5")
        val pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro")

        // Act & Assert
        assertEquals(
            standard.toSearchCriterion(),
            pro.toSearchCriterion()
        )
    }

    @Test
    fun `criterion does not depend on the platform's local id, logo or acquisition year`() {
        // Arrange
        val platform = Platform(1L, "PS5", "PlayStation 5")
        val other = Platform(
            id = 2L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            ownedSince = 2021
        )

        // Act & Assert
        assertEquals(
            platform.toSearchCriterion(),
            other.toSearchCriterion()
        )
    }

    // Matching

    @Test
    fun `criterion matches the platform with that name ignoring case`() {
        assertTrue(criterion("playstation 5", "Sony PS 5").matches(gamePs5))
    }

    @Test
    fun `criterion matches the platform with that abbreviation ignoring case`() {
        assertTrue(criterion("Sony PlayStation 5", "ps5").matches(gamePs5))
    }

    @Test
    fun `criterion matches the platform with one of its alternative names`() {
        assertTrue(criterion("My Switch", "NSW").matches(gameSwitch))
    }

    @Test
    fun `criterion does not match other platforms`() {
        assertFalse(criterion("PlayStation 5", "PS5").matches(gamePs4))
    }

    @Test
    fun `criterion requires names to be equal, not just similar`() {
        // "PS" is part of the PS5's abbreviation, but is not its abbreviation
        assertFalse(criterion("PlayStation", "PS").matches(gamePs5))
    }

    @Test
    fun `criterion matches platforms without abbreviation by their name`() {
        assertTrue(criterion("Arcade", "ARC").matches(gameArcade))
        assertFalse(criterion("Arcade machine", "ARC").matches(gameArcade))
    }

    // Platforms a game was released for

    @Test
    fun `game is released for the given platforms that match its own`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5")
        val switch = Platform(2L, "NSW", "Switch Console")
        val xbox = Platform(3L, "XONE", "Xbox One")
        val game = GameInfo(1L, "Hades", platforms = listOf(gamePs4, gamePs5, gameSwitch))

        // Act
        val platforms = game.releasedFor(listOf(ps5, switch, xbox))

        // Assert - By name for the PS5, and by alternative name for the Switch
        assertEquals(listOf(ps5, switch), platforms)
    }

    @Test
    fun `game is released for each platform once, through the first of its versions`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5")
        val ps5Pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro")
        val game = GameInfo(1L, "Demon's Souls", platforms = listOf(gamePs5))

        // Act
        val platforms = game.releasedFor(listOf(ps5Pro, ps5))

        // Assert - Games are oblivious of versions
        assertEquals(listOf(ps5Pro), platforms)
    }

    @Test
    fun `game without platforms is released for none of the given platforms`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5")
        val game = GameInfo(1L, "Pong")

        // Act & Assert
        assertEquals(emptyList(), game.releasedFor(listOf(ps5)))
    }
}
