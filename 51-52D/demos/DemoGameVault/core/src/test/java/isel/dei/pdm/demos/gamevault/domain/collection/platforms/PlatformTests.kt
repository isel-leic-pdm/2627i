package isel.dei.pdm.demos.gamevault.domain.collection.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformTests {

    // Platforms as far as games are concerned

    @Test
    fun `versions of a platform are the same game platform`() {
        val ps5 = Platform(1L, "PS5", "PlayStation 5")
        val slim = Platform(2L, "PS5", "PlayStation 5", version = "Slim")

        assertTrue(ps5.isSameGamePlatformAs(slim))
        assertTrue(slim.isSameGamePlatformAs(ps5))
    }

    @Test
    fun `platforms are the same game platform if they share the name or abbreviation, ignoring case`() {
        val ps5 = Platform(1L, "PS5", "PlayStation 5")

        assertTrue(ps5.isSameGamePlatformAs(Platform(2L, "PS5", "Sony PS5", version = "Slim")))
        assertTrue(ps5.isSameGamePlatformAs(Platform(3L, "P5", "playstation 5")))
        assertFalse(ps5.isSameGamePlatformAs(Platform(4L, "PS4", "PlayStation 4")))
    }

    @Test
    fun `distinctGamePlatforms keeps the first version of each platform, in order`() {
        // Arrange
        val pro = Platform(1L, "PS5", "PlayStation 5", version = "Pro")
        val switch = Platform(2L, "NSW", "Nintendo Switch")
        val ps5 = Platform(3L, "PS5", "PlayStation 5")
        val oled = Platform(4L, "NSW", "Nintendo Switch", version = "OLED")

        // Act & Assert
        assertEquals(listOf(pro, switch), listOf(pro, switch, ps5, oled).distinctGamePlatforms())
    }

    // Platform

    @Test
    fun `Platform secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val platform = Platform(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            ownedSince = 2020,
            version = "Digital Edition"
        )

        // Assert
        assertEquals(1L, platform.id)
        assertEquals(NonBlankString("PS5"), platform.abbreviation)
        assertEquals(NonBlankString("PlayStation 5"), platform.name)
        assertEquals(Uri("https://example.com/ps5.png"), platform.logoUri)
        assertEquals(Year(2020), platform.ownedSince)
        assertEquals(NonBlankString("Digital Edition"), platform.version)
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
    fun `NewPlatform secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val newPlatform = NewPlatform(
            abbreviation = "NSW",
            name = "Nintendo Switch",
            logoUri = "https://example.com/switch.png",
            ownedSince = 2021,
            version = "OLED"
        )

        // Assert
        assertEquals(NonBlankString("NSW"), newPlatform.abbreviation)
        assertEquals(NonBlankString("Nintendo Switch"), newPlatform.name)
        assertEquals(Uri("https://example.com/switch.png"), newPlatform.logoUri)
        assertEquals(Year(2021), newPlatform.ownedSince)
        assertEquals(NonBlankString("OLED"), newPlatform.version)
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
