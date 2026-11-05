package isel.dei.pdm.demos.gamevault.domain.search.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class PlatformInfoTests {

    @Test
    fun `primary constructor leaves optional properties absent by default`() {
        // Act
        val sut = PlatformInfo(
            id = 1L,
            abbreviation = NonBlankString("PS5"),
            name = NonBlankString("PlayStation 5")
        )

        // Assert
        assertNull(sut.logoUri)
        assertNull(sut.version)
    }

    @Test
    fun `secondary constructor converts strings into the corresponding types`() {
        // Act
        val sut = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "https://example.com/ps5.png",
            version = "Digital Edition"
        )

        // Assert
        assertEquals(1L, sut.id)
        assertEquals(NonBlankString("PS5"), sut.abbreviation)
        assertEquals(NonBlankString("PlayStation 5"), sut.name)
        assertEquals(URI.create("https://example.com/ps5.png"), sut.logoUri)
        assertEquals(NonBlankString("Digital Edition"), sut.version)
    }

    @Test
    fun `secondary constructor leaves optional properties absent by default`() {
        // Act
        val sut = PlatformInfo(id = 2L, abbreviation = "XBOX", name = "Xbox Series X")

        // Assert
        assertNull(sut.logoUri)
        assertNull(sut.version)
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when abbreviation is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(id = 1L, abbreviation = "   \t ", name = "PlayStation 5")
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(id = 1L, abbreviation = "PS5", name = "  ")
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when version is blank`() {
        assertFailsWith<IllegalArgumentException> {
            PlatformInfo(id = 1L, abbreviation = "PS5", name = "PlayStation 5", version = "  ")
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
