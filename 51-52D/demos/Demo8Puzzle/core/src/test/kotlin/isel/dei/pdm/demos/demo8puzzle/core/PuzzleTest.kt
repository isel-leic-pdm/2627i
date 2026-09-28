package isel.dei.pdm.demos.demo8puzzle.core

import org.junit.Assert.*
import org.junit.Test

class PuzzleTest {
    @Test
    fun `Puzzle initialization and access`() {
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        assertEquals(Tile(1), puzzle[0])
        assertEquals(Tile(1), puzzle[0, 0])
        assertEquals(Tile(3), puzzle[0, 2])
        assertEquals(Tile(4), puzzle[1, 0])
        assertEquals(null, puzzle[8])
        assertEquals(null, puzzle[2, 2])
    }

    @Test
    fun `Puzzle move works for adjacent tile`() {
        // 1 2 3
        // 4 5 6
        // 7 8 _
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        
        // Move 8 to the right
        val nextPuzzle = puzzle.move(Tile(8))
        
        // 1 2 3
        // 4 5 6
        // 7 _ 8
        assertEquals(null, nextPuzzle[2, 1])
        assertEquals(Tile(8), nextPuzzle[2, 2])
    }

    @Test
    fun `Puzzle move returns same instance for non-adjacent tile`() {
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val nextPuzzle = puzzle.move(Tile(1))
        assertSame(puzzle, nextPuzzle)
    }

    @Test
    fun `Puzzle is iterable`() {
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val list = puzzle.toList()
        assertEquals(9, list.size)
        assertEquals(Tile(1), list[0])
        assertEquals(null, list[8])
    }

    @Test
    fun `solvedPuzzle is solved`() {
        assertTrue(solvedPuzzle.isSolved())
    }

    @Test
    fun `unsolved puzzle isNotSolved`() {
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 0, 8)
        assertFalse(puzzle.isSolved())
    }

    @Test
    fun `shuffle produces non-solved solvable puzzle`() {
        val shuffled = solvedPuzzle.shuffle(50)
        assertFalse(shuffled.isSolved())
        // Verify tile count
        assertEquals(9, shuffled.count())
        assertEquals(1, shuffled.count { it == null })
    }
}
