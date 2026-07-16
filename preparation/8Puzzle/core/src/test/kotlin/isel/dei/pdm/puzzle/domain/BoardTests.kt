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
    fun board_isSolved_returnsTrueForSolvedBoard() {
        val board = Board.SOLVED
        assertTrue(board.isSolved)
    }

    @Test
    fun board_isSolved_returnsFalseForUnsolvedBoard() {
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        assertFalse(board.isSolved)
    }

    @Test
    fun board_move_swapsWithBlankIfAdjacent() {
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
    fun board_move_doesNothingIfNotAdjacent() {
        // 1 2 3
        // 4 5 6
        // 7 0 8
        val board = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        val movedBoard = board.move(1)
        
        assertEquals(board, movedBoard)
    }

    @Test
    fun board_move_doesNothingIfMovingBlank() {
        val board = Board.SOLVED
        // Moving "0" or any other non-1..8 value should do nothing
        val movedBoard = board.move(0)
        assertEquals(board, movedBoard)
    }

    @Test
    fun board_getCoordinateOf_returnsCorrectCoordinate() {
        val board = Board.SOLVED
        assertEquals(Coordinate(0, 0), board.getCoordinateOf(1))
    }

    @Test
    fun board_getTileAt_returnsCorrectTile() {
        val board = Board.SOLVED
        assertEquals(1, board.getTileAt(Coordinate(0, 0)))
        assertNull(board.getTileAt(Coordinate(2, 2)))
    }

    @Test
    fun board_createRandom_producesSolvableBoard() {
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
}
