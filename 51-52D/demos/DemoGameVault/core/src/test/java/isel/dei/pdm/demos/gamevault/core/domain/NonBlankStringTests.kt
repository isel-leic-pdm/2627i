package isel.dei.pdm.demos.gamevault.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

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
    fun `invoke operator returns the value`() {
        // Arrange
        val expected = "expected"
        val sut = NonBlankString(expected)

        // Act
        val actual = sut()

        // Assert
        assertEquals(expected, actual)
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
    fun `length property returns the correct length`() {
        // Arrange
        val expected = "test"
        val sut = NonBlankString(expected)

        // Act
        val actual = sut.length

        // Assert
        assertEquals(expected.length, actual)
    }

    @Test
    fun `get returns the correct character`() {
        // Arrange
        val expected = "test"
        val sut = NonBlankString(expected)

        // Act
        val actual = sut[1]

        // Assert
        assertEquals('e', actual)
    }

    @Test
    fun `subSequence returns the correct substring`() {
        // Arrange
        val original = NonBlankString("testing")

        // Act
        val subSequence = original.subSequence(0, 4)

        // Assert
        assertEquals("test", subSequence)
    }

    @Test
    fun `contains works directly on NonBlankString`() {
        // Arrange
        val sut = NonBlankString("Elden Ring")

        // Act & Assert
        assertTrue(sut.contains("Elden"))
    }

    @Test
    fun `two NonBlankString instances with same value are equal`() {
        // Arrange
        val first = NonBlankString("test")
        val second = NonBlankString("test")

        // Act & Assert
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `toNonBlankStringOrNull returns NonBlankString for a valid non-blank string`() {
        // Arrange
        val input = "PlayStation"

        // Act
        val actual = input.toNonBlankStringOrNull()

        // Assert
        assertNotNull(actual)
        assertEquals(input, actual?.value)
    }

    @Test
    fun `toNonBlankStringOrNull returns null for an empty string`() {
        // Arrange
        val input = ""

        // Act
        val actual = input.toNonBlankStringOrNull()

        // Assert
        assertNull(actual)
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
