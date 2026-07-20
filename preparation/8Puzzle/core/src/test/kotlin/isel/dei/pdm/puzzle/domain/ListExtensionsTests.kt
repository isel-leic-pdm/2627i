package isel.dei.pdm.puzzle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class ListExtensionsTests {

    @Test
    fun `countInversions returns zero for sorted list`() {
        assertEquals(0, listOf(1, 2, 3, 4, 5, 6, 7, 8).countInversions())
    }

    @Test
    fun `countInversions returns correct count for mixed list`() {
        // (2, 1) is 1 inversion
        assertEquals(1, listOf(2, 1, 3).countInversions())
        // (3, 1), (3, 2), (2, 1) are 3 inversions
        assertEquals(3, listOf(3, 2, 1).countInversions())
        // Example from 8-puzzle: 1 2 3, 4 5 6, 8 7 -> (8, 7) is 1 inversion
        assertEquals(1, listOf(1, 2, 3, 4, 5, 6, 8, 7).countInversions())
    }

    @Test
    fun `swap returns new list with swapped elements`() {
        val original = listOf(1, 2, 3)
        val swapped = original.swap(0, 2)
        assertEquals(listOf(3, 2, 1), swapped)
        assertNotSame(original, swapped)
    }

    @Test
    fun `swap returns same instance if indices are equal`() {
        val original = listOf(1, 2, 3)
        val swapped = original.swap(1, 1)
        assertEquals(original, swapped)
    }
}
