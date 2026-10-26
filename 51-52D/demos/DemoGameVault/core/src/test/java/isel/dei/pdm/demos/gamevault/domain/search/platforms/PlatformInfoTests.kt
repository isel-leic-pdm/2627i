package isel.dei.pdm.demos.gamevault.domain.search.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class PlatformInfoTests {

    @Test
    fun `primary constructor creates instance with valid properties`() {
        // Arrange
        val expectedId = 1L
        val expectedAbbreviation = NonBlankString("PS5")
        val expectedName = NonBlankString("PlayStation 5")
        val expectedLogoUri = URI.create("https://example.com/ps5.png")

        // Act
        val sut = PlatformInfo(
            id = expectedId,
            abbreviation = expectedAbbreviation,
            name = expectedName,
            logoUri = expectedLogoUri
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedAbbreviation, sut.abbreviation)
        assertEquals(expectedName, sut.name)
        assertEquals(expectedLogoUri, sut.logoUri)
    }

    @Test
    fun `primary constructor creates instance without logoUri`() {
        // Arrange
        val expectedId = 1L
        val expectedAbbreviation = NonBlankString("PS5")
        val expectedName = NonBlankString("PlayStation 5")

        // Act
        val sut = PlatformInfo(
            id = expectedId,
            abbreviation = expectedAbbreviation,
            name = expectedName
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedAbbreviation, sut.abbreviation)
        assertEquals(expectedName, sut.name)
        assertNull(sut.logoUri)
    }

    @Test
    fun `secondary constructor creates instance with valid string inputs`() {
        // Arrange
        val expectedId = 1L
        val expectedAbbreviation = "PS5"
        val expectedName = "PlayStation 5"
        val expectedLogoUriString = "https://example.com/ps5.png"

        // Act
        val sut = PlatformInfo(
            id = expectedId,
            abbreviation = expectedAbbreviation,
            name = expectedName,
            logoUri = expectedLogoUriString
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedAbbreviation, sut.abbreviation.value)
        assertEquals(expectedName, sut.name.value)
        assertEquals(URI.create(expectedLogoUriString), sut.logoUri)
    }

    @Test
    fun `secondary constructor creates instance without logoUri`() {
        // Arrange
        val expectedId = 2L
        val expectedAbbreviation = "XBOX"
        val expectedName = "Xbox Series X"

        // Act
        val sut = PlatformInfo(
            id = expectedId,
            abbreviation = expectedAbbreviation,
            name = expectedName
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedAbbreviation, sut.abbreviation.value)
        assertEquals(expectedName, sut.name.value)
        assertNull(sut.logoUri)
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(
                id = 1L,
                abbreviation = "   \t ",
                name = "PlayStation 5"
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(
                id = 1L,
                abbreviation = "PS5",
                name = "  "
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when logoUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(
                id = 1L,
                abbreviation = "PS5",
                name = "PlayStation 5",
                logoUri = "http://example.com/logo with space.png"
            )
        }
    }
}
