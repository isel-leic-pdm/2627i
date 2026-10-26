package isel.dei.pdm.demos.gamevault.domain.collection

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import java.time.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull


class PlatformTests {

    @Test
    fun `Platform primary constructor creates instance with valid properties`() {
        // Arrange
        val id = 1L
        val abbreviation = NonBlankString("PS5")
        val name = NonBlankString("PlayStation 5")
        val logoUri = URI.create("https://example.com/ps5.png")
        val ownedSince = Year.of(2020)


        // Act
        val platform = Platform(id, abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(id, platform.id)
        assertEquals(abbreviation, platform.abbreviation)
        assertEquals(name, platform.name)
        assertEquals(logoUri, platform.logoUri)
        assertEquals(ownedSince, platform.ownedSince)
    }

    @Test
    fun `Platform primary constructor creates instance without optional args`() {
        // Arrange
        val id = 1L
        val abbreviation = NonBlankString("PS5")
        val name = NonBlankString("PlayStation 5")
        val logoUri: URI? = null
        val ownedSince: Year? = null

        // Act
        val platform = Platform(id, abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(id, platform.id)
        assertEquals(abbreviation, platform.abbreviation)
        assertEquals(name, platform.name)
        assertNull(platform.logoUri)
        assertNull(platform.ownedSince)
    }

    @Test
    fun `Platform secondary constructor creates instance with valid inputs`() {
        // Arrange
        val id = 1L
        val abbreviation = "PS5"
        val name = "PlayStation 5"
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act
        val platform = Platform(id, abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(id, platform.id)
        assertEquals(abbreviation, platform.abbreviation.value)
        assertEquals(name, platform.name.value)
        assertEquals(URI.create(logoUri), platform.logoUri)
    }

    @Test
    fun `Platform secondary constructor creates instance with valid string inputs and no optional args`() {
        // Arrange
        val id = 1L
        val abbreviation = "PS5"
        val name = "PlayStation 5"
        val logoUri: String? = null
        val ownedSince: Int? = null

        // Act
        val platform = Platform(id, abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(id, platform.id)
        assertEquals(abbreviation, platform.abbreviation.value)
        assertEquals(name, platform.name.value)
        assertNull(platform.logoUri)
        assertNull(platform.ownedSince)
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        // Arrange
        val id = 1L
        val abbreviation = "   "
        val name = "PlayStation 5"
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            Platform(id, abbreviation, name, logoUri, ownedSince)
        }
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when name is blank`() {
        // Arrange
        val id = 1L
        val abbreviation = "PS5"
        val name = "   "
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            Platform(id, abbreviation, name, logoUri, ownedSince)
        }
    }

    @Test
    fun `Platform secondary constructor throws IllegalArgumentException when logoUri is invalid`() {
        // Arrange
        val id = 1L
        val abbreviation = "PS5"
        val name = "PlayStation 5"
        val logoUri = " invalid_uri "
        val ownedSince = 2020

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            Platform(id, abbreviation, name, logoUri, ownedSince)
        }
    }

    @Test
    fun `NewPlatform primary constructor creates instance with valid properties`() {
        // Arrange
        val abbreviation = NonBlankString("PS5")
        val name = NonBlankString("PlayStation 5")
        val logoUri = URI.create("https://example.com/ps5.png")

        // Act
        val newPlatform = NewPlatform(abbreviation, name, logoUri)

        // Assert
        assertEquals(abbreviation, newPlatform.abbreviation)
        assertEquals(name, newPlatform.name)
        assertEquals(logoUri, newPlatform.logoUri)
    }

    @Test
    fun `NewPlatform primary constructor creates instance with no optional args`() {
        // Arrange
        val abbreviation = NonBlankString("PS5")
        val name = NonBlankString("PlayStation 5")
        val logoUri: URI? = null
        val ownedSince: Year? = null

        // Act
        val newPlatform = NewPlatform(abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(abbreviation, newPlatform.abbreviation)
        assertEquals(name, newPlatform.name)
        assertNull(newPlatform.logoUri)
        assertNull(newPlatform.ownedSince)
    }

    @Test
    fun `NewPlatform secondary constructor creates instance with valid inputs`() {
        // Arrange
        val abbreviation = "PS5"
        val name = "PlayStation 5"
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act
        val newPlatform = NewPlatform(abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(abbreviation, newPlatform.abbreviation.value)
        assertEquals(name, newPlatform.name.value)
        assertEquals(URI.create(logoUri), newPlatform.logoUri)
        assertEquals(Year.of(ownedSince), newPlatform.ownedSince)
    }

    @Test
    fun `NewPlatform secondary constructor creates instance with valid string inputs and no optional args`() {
        // Arrange
        val abbreviation = "PS5"
        val name = "PlayStation 5"
        val logoUri: String? = null
        val ownedSince: Int? = null

        // Act
        val newPlatform = NewPlatform(abbreviation, name, logoUri, ownedSince)

        // Assert
        assertEquals(abbreviation, newPlatform.abbreviation.value)
        assertEquals(name, newPlatform.name.value)
        assertNull(newPlatform.logoUri)
        assertNull(newPlatform.ownedSince)
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        // Arrange
        val abbreviation = "   "
        val name = "PlayStation 5"
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation, name, logoUri, ownedSince)
        }
    }

    @Test
    fun `NewPlatform secondary constructor throws IllegalArgumentException when name is blank`() {
        // Arrange
        val abbreviation = "PS5"
        val name = "   "
        val logoUri = "https://example.com/ps5.png"
        val ownedSince = 2020

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            NewPlatform(abbreviation, name, logoUri, ownedSince)
        }
    }
}