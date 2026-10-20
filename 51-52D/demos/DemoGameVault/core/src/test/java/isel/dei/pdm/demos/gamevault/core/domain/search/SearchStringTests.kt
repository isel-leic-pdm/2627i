package isel.dei.pdm.demos.gamevault.core.domain.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.test.assertFailsWith

class SearchStringTests {

    @Test
    fun `valid search string holds its value`() {
        // Arrange
        val expected = "test"
        val sut = SearchString(expected)

        // Act
        val actual = sut.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `string with two non-blank characters is allowed`() {
        // Arrange
        val expected = "PS"
        val sut = SearchString(expected)

        // Act
        val actual = sut.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `string with spaced non-blank characters is allowed`() {
        // Arrange
        val expected = " a b "
        val sut = SearchString(expected)

        // Act
        val actual = sut.value

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `empty string is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            SearchString("")
        }
    }

    @Test
    fun `blank string is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            SearchString("   ")
        }
    }

    @Test
    fun `single character string is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            SearchString("a")
        }
    }

    @Test
    fun `single non-blank character with surrounding spaces is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            SearchString("  a  ")
        }
    }

    @Test
    fun `isValid returns true for string with at least 2 non-blank characters`() {
        // Arrange & Act & Assert
        assertTrue(SearchString.isValid("PS"))
        assertTrue(SearchString.isValid(" a b "))
    }

    @Test
    fun `isValid returns false for string with less than 2 non-blank characters`() {
        // Arrange & Act & Assert
        assertFalse(SearchString.isValid(""))
        assertFalse(SearchString.isValid("   "))
        assertFalse(SearchString.isValid("a"))
        assertFalse(SearchString.isValid("  a  "))
    }

    @Test
    fun `invoke operator returns the value`() {
        // Arrange
        val expected = "expected"
        val sut = SearchString(expected)

        // Act
        val actual = sut()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `toString returns the underlying value`() {
        // Arrange
        val expected = "PlayStation"
        val sut = SearchString(expected)

        // Act
        val actual = sut.toString()

        // Assert
        assertEquals(expected, actual)
    }

    @Test
    fun `length property returns the correct length`() {
        // Arrange
        val expected = "test"
        val sut = SearchString(expected)

        // Act
        val actual = sut.length

        // Assert
        assertEquals(expected.length, actual)
    }

    @Test
    fun `get returns the correct character`() {
        // Arrange
        val expected = "test"
        val sut = SearchString(expected)

        // Act
        val actual = sut[1]

        // Assert
        assertEquals('e', actual)
    }

    @Test
    fun `subSequence returns the correct substring`() {
        // Arrange
        val original = SearchString("testing")

        // Act
        val subSequence = original.subSequence(0, 4)

        // Assert
        assertEquals("test", subSequence)
    }

    @Test
    fun `contains works directly on SearchString`() {
        // Arrange
        val sut = SearchString("Elden Ring")

        // Act & Assert
        assertTrue(sut.contains("Elden"))
    }

    @Test
    fun `two SearchString instances with same value are equal`() {
        // Arrange
        val first = SearchString("test")
        val second = SearchString("test")

        // Act & Assert
        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `toSearchStringOrNull returns SearchString for a valid string with at least 2 non-blank characters`() {
        // Arrange
        val input = "PlayStation"

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNotNull(actual)
        assertEquals(input, actual?.value)
    }

    @Test
    fun `toSearchStringOrNull returns null for an empty string`() {
        // Arrange
        val input = ""

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNull(actual)
    }

    @Test
    fun `toSearchStringOrNull returns null for a blank string`() {
        // Arrange
        val input = "   "

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNull(actual)
    }

    @Test
    fun `toSearchStringOrNull returns null for a single character string`() {
        // Arrange
        val input = "a"

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNull(actual)
    }

    @Test
    fun `toSearchStringOrNull returns null for a single non-blank character with surrounding spaces`() {
        // Arrange
        val input = "  a  "

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNull(actual)
    }
}
