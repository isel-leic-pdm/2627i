package isel.dei.pdm.puzzle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import isel.dei.pdm.puzzle.domain.Board.Coordinate

class BoardTests {

    @Test
    fun `board isSolved returns true for solved board`() {
        val board = Board.SOLVED
        assertTrue(board.isSolved)
    }

    @Test
    fun `board isSolved returns false for unsolved board`() {
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        assertFalse(board.isSolved)
    }

    @Test
    fun `board move swaps with blank if adjacent`() {
        // 1 2 3
        // 4 5 6
        // 7 0 8
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        val movedBoard = board.move(8)
        
        // 1 2 3
        // 4 5 6
        // 7 8 0
        assertNull(movedBoard.getTileAt(Coordinate(2, 2)))
        assertEquals(8, movedBoard.getTileAt(Coordinate(2, 1)))
        assertTrue(movedBoard.isSolved)
    }

    @Test
    fun `board move does nothing if not adjacent`() {
        // 1 2 3
        // 4 5 6
        // 7 0 8
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        val movedBoard = board.move(1)
        
        assertEquals(board, movedBoard)
    }

    @Test
    fun `board move does nothing if moving blank`() {
        val board = Board.SOLVED
        // Moving "0" or any other non-1..8 value should do nothing
        val movedBoard = board.move(0)
        assertEquals(board, movedBoard)
    }

    @Test
    fun `board getCoordinateOf returns correct coordinate`() {
        val board = Board.SOLVED
        assertEquals(Coordinate(0, 0), board.getCoordinateOf(1))
    }

    @Test
    fun `board getTileAt returns correct tile`() {
        val board = Board.SOLVED
        assertEquals(1, board.getTileAt(Coordinate(0, 0)))
        assertNull(board.getTileAt(Coordinate(2, 2)))
    }

    @Test
    fun `board createRandom produces solvable board`() {
        repeat(10) {
            val board = Board.createRandom()
            // Can't check tiles size directly, but can check all coordinates
            var nonNullCount = 0
            for (r in 0 until Board.BOARD_SIDE) {
                for (c in 0 until Board.BOARD_SIDE) {
                    if (board.getTileAt(Coordinate(r, c)) != null) nonNullCount++
                }
            }
            assertEquals(Board.BOARD_SIDE * Board.BOARD_SIDE - 1, nonNullCount)
            assertNotEquals(Board.SOLVED, board)
        }
    }

    @Test
    fun `board computeManhattanDistance returns 0 for solved board`() {
        assertEquals(0, Board.SOLVED.computeManhattanDistance())
    }

    @Test
    fun `board computeManhattanDistance returns correct distance`() {
        // 1 2 3
        // 4 5 6
        // 7 0 8
        // 8 is at (2, 2), target is (2, 1). Distance = 1
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        assertEquals(1, board.computeManhattanDistance())

        // 0 1 2
        // 3 4 5
        // 6 7 8
        // 1: (0, 1) target (0, 0) dist 1
        // 2: (0, 2) target (0, 1) dist 1
        // 3: (1, 0) target (0, 2) dist 3
        // 4: (1, 1) target (1, 0) dist 1
        // 5: (1, 2) target (1, 1) dist 1
        // 6: (2, 0) target (1, 2) dist 3
        // 7: (2, 1) target (2, 0) dist 1
        // 8: (2, 2) target (2, 1) dist 1
        // total: 1+1+3+1+1+3+1+1 = 12
        val board2 = Board(listOf(0, 1, 2, 3, 4, 5, 6, 7, 8))
        assertEquals(12, board2.computeManhattanDistance())
    }

    @Test
    fun `board getAdjacentBoards returns all valid moves`() {
        // 1 2 3
        // 4 0 5
        // 6 7 8
        // Blank at (1, 1). Adjacents: (0, 1), (2, 1), (1, 0), (1, 2)
        // Tiles: 2, 7, 4, 5
        val board = Board(listOf(1, 2, 3, 4, 0, 5, 6, 7, 8))
        val adjacents = board.getAdjacentBoards()
        assertEquals(4, adjacents.size)
        assertTrue(adjacents.contains(board.move(2)))
        assertTrue(adjacents.contains(board.move(7)))
        assertTrue(adjacents.contains(board.move(4)))
        assertTrue(adjacents.contains(board.move(5)))
    }
}
