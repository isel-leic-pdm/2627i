package isel.dei.pdm.demos.gamevault.domain.collection.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlatformTests {

    private val igdbSource = SourceReference(SourceReference.Provider.IGDB, NonBlankString("167"))

    // Platforms as far as games are concerned

    @Test
    fun `versions of a platform obtained from the provider are the same game platform`() {
        val ps5 = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)
        val slim = Platform(2L, "PS5", "PlayStation 5 Slim", version = "Slim", source = igdbSource)

        assertTrue(ps5.isSameGamePlatformAs(slim))
        assertTrue(slim.isSameGamePlatformAs(ps5))
    }

    @Test
    fun `platforms obtained from the provider are told apart by its identifiers, not by names`() {
        // Arrange - Even if they share the abbreviation
        val ps5 = Platform(1L, "PS", "PlayStation 5", source = igdbSource)
        val ps4 = Platform(
            2L, "PS", "PlayStation 4",
            source = SourceReference(SourceReference.Provider.IGDB, NonBlankString("48"))
        )

        // Act & Assert
        assertFalse(ps5.isSameGamePlatformAs(ps4))
    }

    @Test
    fun `platforms inserted manually are the same game platform if they share the name or abbreviation`() {
        val ps5 = Platform(1L, "PS5", "PlayStation 5")

        assertTrue(ps5.isSameGamePlatformAs(Platform(2L, "PS5", "Sony PS5", version = "Slim")))
        assertTrue(ps5.isSameGamePlatformAs(Platform(3L, "P5", "playstation 5")))
        assertFalse(ps5.isSameGamePlatformAs(Platform(4L, "PS4", "PlayStation 4")))
    }

    @Test
    fun `platforms from a provider and inserted manually are compared by name or abbreviation`() {
        val ps5 = Platform(1L, "PS5", "PlayStation 5", source = igdbSource)

        assertTrue(ps5.isSameGamePlatformAs(Platform(2L, "PS5", "My PS5")))
        assertFalse(ps5.isSameGamePlatformAs(Platform(3L, "NSW", "Nintendo Switch")))
    }

    @Test
    fun `distinctGamePlatforms keeps the first version of each platform, in order`() {
        // Arrange
        val pro = Platform(1L, "PS5", "PlayStation 5", version = "Pro", source = igdbSource)
        val switch = Platform(2L, "NSW", "Nintendo Switch")
        val ps5 = Platform(3L, "PS5", "PlayStation 5", source = igdbSource)
        val oled = Platform(4L, "NSW", "Nintendo Switch", version = "OLED")

        // Act & Assert
        assertEquals(listOf(pro, switch), listOf(pro, switch, ps5, oled).distinctGamePlatforms())
    }

    // Platform

    @Test
    fun `Platform primary constructor leaves optional properties absent by default`() {
        // Act
        val platform = Platform(1L, NonBlankString("PS5"), NonBlankString("PlayStation 5"))

        // Assert
        assertNull(platform.logoUri)
        assertNull(platform.ownedSince)
        assertNull(platform.version)
        assertNull(platform.source)
    }

    @Test
    fun `Platform secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val platform = Platform(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            ownedSince = 2020,
            version = "Digital Edition",
            source = igdbSource
        )

        // Assert
        assertEquals(igdbSource, platform.source)
        assertEquals(1L, platform.id)
        assertEquals(NonBlankString("PS5"), platform.abbreviation)
        assertEquals(NonBlankString("PlayStation 5"), platform.name)
        assertEquals(Uri("https://example.com/ps5.png"), platform.logoUri)
        assertEquals(Year(2020), platform.ownedSince)
        assertEquals(NonBlankString("Digital Edition"), platform.version)
    }

    @Test
    fun `Platform secondary constructor leaves optional properties absent by default`() {
        // Act
        val platform = Platform(id = 1L, abbreviation = "PS5", name = "PlayStation 5")

        // Assert
        assertNull(platform.logoUri)
        assertNull(platform.ownedSince)
        assertNull(platform.version)
        assertNull(platform.source)
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(id = 1L, abbreviation = "   ", name = "PlayStation 5")
        }
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(id = 1L, abbreviation = "PS5", name = "   ")
        }
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when version is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(id = 1L, abbreviation = "PS5", name = "PlayStation 5", version = "   ")
        }
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when logoUri is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(id = 1L, abbreviation = "PS5", name = "PlayStation 5", logoUri = " invalid_uri ")
        }
    }

    // NewPlatform

    @Test
    fun `NewPlatform primary constructor leaves optional properties absent by default`() {
        // Act
        val newPlatform = NewPlatform(NonBlankString("NSW"), NonBlankString("Nintendo Switch"))

        // Assert
        assertNull(newPlatform.logoUri)
        assertNull(newPlatform.ownedSince)
        assertNull(newPlatform.version)
        assertNull(newPlatform.source)
    }

    @Test
    fun `NewPlatform secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val newPlatform = NewPlatform(
            abbreviation = "NSW",
            name = "Nintendo Switch",
            logoUri = "https://example.com/switch.png",
            ownedSince = 2021,
            version = "OLED",
            source = igdbSource
        )

        // Assert
        assertEquals(igdbSource, newPlatform.source)
        assertEquals(NonBlankString("NSW"), newPlatform.abbreviation)
        assertEquals(NonBlankString("Nintendo Switch"), newPlatform.name)
        assertEquals(Uri("https://example.com/switch.png"), newPlatform.logoUri)
        assertEquals(Year(2021), newPlatform.ownedSince)
        assertEquals(NonBlankString("OLED"), newPlatform.version)
    }

    @Test
    fun `NewPlatform secondary constructor leaves optional properties absent by default`() {
        // Act
        val newPlatform = NewPlatform(abbreviation = "NSW", name = "Nintendo Switch")

        // Assert
        assertNull(newPlatform.logoUri)
        assertNull(newPlatform.ownedSince)
        assertNull(newPlatform.version)
        assertNull(newPlatform.source)
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation = "   ", name = "Nintendo Switch")
        }
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation = "NSW", name = "   ")
        }
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when version is blank`() {
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation = "NSW", name = "Nintendo Switch", version = " ")
        }
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when logoUri is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation = "NSW", name = "Nintendo Switch", logoUri = " invalid_uri ")
        }
    }
}
