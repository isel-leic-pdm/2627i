package isel.dei.pdm.demos.gamevault.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NonBlankStringTests {

    @Test
    fun `non-blank string holds its value`() {
        // Arrange
        val expected = "expected"
        val sut = NonBlankString(expected)

        // Act
        val actual = sut.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `empty string is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            NonBlankString("")
        }
    }

    @Test
    fun `blank string is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            NonBlankString("   ")
        }
    }

    @Test
    fun `toString returns the underlying value`() {
        // Arrange
        val expected = "Elden Ring"
        val sut = NonBlankString(expected)

        // Act
        val actual = sut.toString()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `toNonBlankStringOrNull returns NonBlankString for a valid non-blank string`() {
        // Arrange
        val input = "PlayStation"

        // Act
        val actual = input.toNonBlankStringOrNull()

        // Assert
        assertNotNull(actual)
        assertEquals(input, actual.value)
    }

    @Test
    fun `toNonBlankStringOrNull returns null for a blank string`() {
        // Arrange
        val input = "   "

        // Act
        val actual = input.toNonBlankStringOrNull()

        // Assert
        assertNull(actual)
    }
}
