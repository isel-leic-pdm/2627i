package isel.dei.pdm.demos.gamevault.domain.collection

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import java.time.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PlatformTests {

    // Platform

    @Test
    fun `Platform primary constructor leaves optional properties absent by default`() {
        // Act
        val platform = Platform(1L, NonBlankString("PS5"), NonBlankString("PlayStation 5"))

        // Assert
        assertNull(platform.logoUri)
        assertNull(platform.ownedSince)
        assertNull(platform.version)
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
            version = "Digital Edition"
        )

        // Assert
        assertEquals(1L, platform.id)
        assertEquals(NonBlankString("PS5"), platform.abbreviation)
        assertEquals(NonBlankString("PlayStation 5"), platform.name)
        assertEquals(URI.create("https://example.com/ps5.png"), platform.logoUri)
        assertEquals(Year.of(2020), platform.ownedSince)
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
    }

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
        assertEquals(URI.create("https://example.com/switch.png"), newPlatform.logoUri)
        assertEquals(Year.of(2021), newPlatform.ownedSince)
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
