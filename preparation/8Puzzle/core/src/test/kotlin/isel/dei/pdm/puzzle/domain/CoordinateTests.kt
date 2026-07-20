package isel.dei.pdm.puzzle.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import isel.dei.pdm.puzzle.domain.Board.Coordinate

class CoordinateTests {

    @Test
    fun `coordinate isAdjacentTo returns true for adjacent cells`() {
        val center = Coordinate(1, 1)
        assertTrue(center.isAdjacentTo(Coordinate(0, 1))) // Top
        assertTrue(center.isAdjacentTo(Coordinate(2, 1))) // Bottom
        assertTrue(center.isAdjacentTo(Coordinate(1, 0))) // Left
        assertTrue(center.isAdjacentTo(Coordinate(1, 2))) // Right
    }

    @Test
    fun `coordinate isAdjacentTo returns false for non-adjacent cells`() {
        val center = Coordinate(1, 1)
        assertFalse(center.isAdjacentTo(Coordinate(1, 1))) // Same
        assertFalse(center.isAdjacentTo(Coordinate(0, 0))) // Diagonal
        assertFalse(center.isAdjacentTo(Coordinate(0, 2))) // Diagonal
        assertFalse(center.isAdjacentTo(Coordinate(2, 0))) // Diagonal
        assertFalse(center.isAdjacentTo(Coordinate(2, 2))) // Diagonal
    }

    @Test(expected = IllegalArgumentException::class)
    fun `coordinate init throws for invalid row`() {
        Coordinate(Board.BOARD_SIDE, 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `coordinate init throws for invalid col`() {
        Coordinate(1, -1)
    }
}
