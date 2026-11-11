package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformCriterionTests {

    private val igdbSource = SourceReference(SourceReference.Provider.IGDB, NonBlankString("167"))

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

    private fun byId(id: String) = PlatformCriterion.ById(NonBlankString(id))

    private fun byName(name: String, abbreviation: String) =
        PlatformCriterion.ByName(NonBlankString(name), NonBlankString(abbreviation))

    // Criterion of a platform

    @Test
    fun `platform obtained from the provider is searched for by its external id`() {
        // Arrange
        val platform = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)

        // Act
        val criterion = platform.toSearchCriterion()

        // Assert
        assertEquals(PlatformCriterion.ById(NonBlankString("167")), criterion)
    }

    @Test
    fun `platform inserted manually is searched for by its name and abbreviation`() {
        // Arrange
        val platform = Platform(1L, "PS5", "PlayStation 5", source = null)

        // Act
        val criterion = platform.toSearchCriterion()

        // Assert
        assertEquals(
            PlatformCriterion.ByName(NonBlankString("PlayStation 5"), NonBlankString("PS5")),
            criterion
        )
    }

    @Test
    fun `versions of a platform obtained from the provider have equal criteria`() {
        // Arrange - The provider uses the same identifier for all versions of a platform
        val standard = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro", source = igdbSource)

        // Act & Assert
        assertEquals(
            standard.toSearchCriterion(),
            pro.toSearchCriterion()
        )
    }

    @Test
    fun `versions of a platform inserted manually have equal criteria`() {
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
        val platform = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val other = Platform(
            id = 2L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            ownedSince = 2021,
            source = igdbSource
        )

        // Act & Assert
        assertEquals(
            platform.toSearchCriterion(),
            other.toSearchCriterion()
        )
    }

    // Matching

    @Test
    fun `criterion by id matches the platform with that id only`() {
        assertTrue(byId("167").matches(gamePs5))
        assertFalse(byId("167").matches(gamePs4))
    }

    @Test
    fun `criterion by name matches the platform with that name ignoring case`() {
        assertTrue(byName("playstation 5", "Sony PS 5").matches(gamePs5))
    }

    @Test
    fun `criterion by name matches the platform with that abbreviation ignoring case`() {
        assertTrue(byName("Sony PlayStation 5", "ps5").matches(gamePs5))
    }

    @Test
    fun `criterion by name matches the platform with one of its alternative names`() {
        assertTrue(byName("My Switch", "NSW").matches(gameSwitch))
    }

    @Test
    fun `criterion by name requires names to be equal, not just similar`() {
        // "PS" is part of the PS5's abbreviation, but is not its abbreviation
        assertFalse(byName("PlayStation", "PS").matches(gamePs5))
    }

    @Test
    fun `criterion by name matches platforms without abbreviation by their name`() {
        assertTrue(byName("Arcade", "ARC").matches(gameArcade))
        assertFalse(byName("Arcade machine", "ARC").matches(gameArcade))
    }

    // Platforms a game was released for

    @Test
    fun `game is released for the given platforms that match its own`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val manualSwitch = Platform(2L, "NSW", "Switch Console")
        val xbox = Platform(3L, "XONE", "Xbox One")
        val game = GameInfo(1L, "Hades", platforms = listOf(gamePs4, gamePs5, gameSwitch))

        // Act
        val platforms = game.releasedFor(listOf(ps5, manualSwitch, xbox))

        // Assert - By id for the PS5, and by alternative name for the Switch inserted manually
        assertEquals(listOf(ps5, manualSwitch), platforms)
    }

    @Test
    fun `game is released for each platform once, through the first of its versions`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val ps5Pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro", source = igdbSource)
        val game = GameInfo(1L, "Demon's Souls", platforms = listOf(gamePs5))

        // Act
        val platforms = game.releasedFor(listOf(ps5Pro, ps5))

        // Assert - Games are oblivious of versions
        assertEquals(listOf(ps5Pro), platforms)
    }

    @Test
    fun `game without platforms is released for none of the given platforms`() {
        // Arrange
        val ps5 = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val game = GameInfo(1L, "Pong")

        // Act & Assert
        assertEquals(emptyList(), game.releasedFor(listOf(ps5)))
    }
}
