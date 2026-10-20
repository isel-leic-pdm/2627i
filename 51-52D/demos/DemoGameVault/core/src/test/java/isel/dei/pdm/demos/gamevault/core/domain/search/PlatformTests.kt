package isel.dei.pdm.demos.gamevault.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.net.URI
import kotlin.test.assertFailsWith

class PlatformTests {

    @Test
    fun `primary constructor creates Platform instance with valid properties`() {
        // Arrange
        val expectedId = 1L
        val expectedAbbreviation = SearchString("PS5")
        val expectedName = SearchString("PlayStation 5")
        val expectedLogoUri = URI.create("https://example.com/ps5.png")

        // Act
        val sut = Platform(
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
    fun `secondary constructor creates Platform instance with valid string inputs`() {
        // Arrange
        val expectedId = 1L
        val expectedAbbreviation = "PS5"
        val expectedName = "PlayStation 5"
        val expectedLogoUriString = "https://example.com/ps5.png"

        // Act
        val sut = Platform(
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
    fun `secondary constructor creates Platform instance without logoUri`() {
        // Arrange
        val expectedId = 2L
        val expectedAbbreviation = "XBOX"
        val expectedName = "Xbox Series X"

        // Act
        val sut = Platform(
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
    fun `secondary constructor throws IllegalArgumentException when abbreviation has less than 2 non-blank characters`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(
                id = 1L,
                abbreviation = "P",
                name = "PlayStation 5"
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when name has less than 2 non-blank characters`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(
                id = 1L,
                abbreviation = "PS5",
                name = "P"
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when logoUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            Platform(
                id = 1L,
                abbreviation = "PS5",
                name = "PlayStation 5",
                logoUri = "http://example.com/logo with space.png"
            )
        }
    }

    @Test
    fun `two Platform instances with same properties are equal`() {
        // Arrange
        val platform1 = Platform(id = 1L, abbreviation = "PS5", name = "PlayStation 5")
        val platform2 = Platform(id = 1L, abbreviation = "PS5", name = "PlayStation 5")

        // Act & Assert
        assertEquals(platform1, platform2)
        assertEquals(platform1.hashCode(), platform2.hashCode())
    }
}
