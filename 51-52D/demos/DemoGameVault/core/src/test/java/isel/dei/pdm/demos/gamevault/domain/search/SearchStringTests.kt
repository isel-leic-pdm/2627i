package isel.dei.pdm.demos.gamevault.domain.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The validity rule is tested through [SearchString.isValid]. The constructor and
 * [toSearchStringOrNull] are only checked to apply it.
 */
class SearchStringTests {

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
    fun `single non-blank character with surrounding spaces is not allowed`() {
        assertFailsWith<IllegalArgumentException> {
            SearchString("  a  ")
        }
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
    fun `toSearchStringOrNull returns SearchString for a valid string with at least 2 non-blank characters`() {
        // Arrange
        val input = "PlayStation"

        // Act
        val actual = input.toSearchStringOrNull()

        // Assert
        assertNotNull(actual)
        assertEquals(input, actual.value)
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
