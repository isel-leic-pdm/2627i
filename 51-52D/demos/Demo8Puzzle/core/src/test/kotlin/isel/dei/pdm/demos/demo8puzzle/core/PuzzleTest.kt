package isel.dei.pdm.demos.demo8puzzle.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleTest {
    @Test
    fun `initialisation works correctly`() {
        // Arrange and Act
        val pieces = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val puzzle = Puzzle(*pieces)

        // Assert
        assertNull(puzzle[pieces.size - 1])
        puzzle.filterNotNull().forEachIndexed { index, tile ->
            assertEquals(pieces[index], tile.value)
        }
    }

    @Test
    fun `move tile works for tile adjacent to the space`() {
        // Arrange
        // 1 2 3
        // 4 5 6
        // 7 8 _
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val valueToMove = 8

        // Act
        val actualPuzzle = puzzle.move(tile = Tile(valueToMove))

        // Assert
        // 1 2 3
        // 4 5 6
        // 7 _ 8
        assertNull(actualPuzzle[2, 1])
        assertEquals(valueToMove, actualPuzzle[2, 2]?.value)
    }

    @Test
    fun `move tile returns same instance for non-adjacent tile`() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val tileToMove = Tile(1)

        // Act
        val nextPuzzle = puzzle.move(tileToMove)

        // Assert
        assertSame(puzzle, nextPuzzle)
    }

    @Test
    fun `instance is iterable`() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

        // Act & Assert
        puzzle.filterNotNull().forEachIndexed { index, tile ->
            assertEquals(index + 1, tile.value)
        }
        assertNull(puzzle.last())
    }

    @Test
    fun `solvedPuzzle is solved`() {
        // Arrange
        val sut = solvedPuzzle

        // Act & Assert
        assertTrue(sut.isSolved())
    }

    @Test
    fun `isSolved for unsolved puzzle returns false`() {
        // Arrange
        val sut = Puzzle(1, 2, 3, 4, 5, 6, 7, 0, 8)

        // Act & Assert
        assertFalse(sut.isSolved())
    }

    @Test
    fun `isSolvable for solvable puzzle returns true`() {
        // Arrange
        val sut = Puzzle(1, 2, 3, 4, 5, 7, 8, 6, 0)

        // Act & Assert
        assertTrue(sut.isSolvable())
    }

    @Test
    fun `shuffle produces non-solved solvable puzzles`() {
        // Arrange
        val solved = solvedPuzzle

        // We use randomization in our algorithm, so lets repeat the test a few times
        repeat(times = 100) {
            // Act
            val shuffled = solved.shuffle()

            // Assert
            assertFalse(shuffled.isSolved())
            assertTrue(shuffled.isSolvable())
        }
    }

    @Test
    fun `inversions returns the correct number of inversions`() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 8, 6, 7, 0)
        val expectedInversions = 2

        // Act
        val actual = puzzle.inversions()

        // Assert
        assertEquals(expectedInversions, actual)
    }

    @Test
    fun `equals between two equivalent puzzles returns true`() {
        // Arrange
        val puzzle1 = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val puzzle2 = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

        // Act & Assert
        assertTrue(puzzle1 == puzzle2)
    }

    @Test
    fun `equals between two different puzzles returns false`() {
        // Arrange
        val puzzle1 = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val puzzle2 = Puzzle(1, 2, 3, 4, 5, 6, 7, 0, 8)

        // Act & Assert
        assertFalse(puzzle1 == puzzle2)
    }

    @Test
    fun `equals between puzzle and non-puzzle returns false`() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val nonPuzzle = "Not a puzzle"

        // Act & Assert
        assertFalse(puzzle.equals(nonPuzzle))
    }
}
