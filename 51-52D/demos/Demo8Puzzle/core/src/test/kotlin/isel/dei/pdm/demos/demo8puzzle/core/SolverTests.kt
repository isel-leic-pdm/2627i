package isel.dei.pdm.demos.demo8puzzle.core

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SolverTests {

    @Test
    fun `rebuildSolutionPath produces the correct solution path`() {
        // Arrange
        val initialPuzzle = Puzzle(1, 2, 3, 4, 6, 8, 7, 5, 0)
        val moves = listOf(Tile(8), Tile(6), Tile(5), Tile(8))
        var currentNode = GraphNode(initialPuzzle)

        moves.forEach {
            currentNode = GraphNode(
                puzzle = currentNode.puzzle.move(it),
                move = it,
                parent = currentNode
            )
        }

        // Act
        val solutionPath = currentNode.rebuildSolutionPath()

        // Assert
        assertEquals(moves, solutionPath)
    }

    @Test
    fun `rebuildSolution for solved puzzle returns empty list`() {
        // Arrange
        val solvedPuzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

        // Act
        val solutionPath = GraphNode(solvedPuzzle).rebuildSolutionPath()

        // Assert
        assertEquals(emptyList<Tile>(), solutionPath)
    }

    @Test
    fun `findNeighbors produces the correct neighbors`() {
        // Arrange
        val puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)
        val graphNode = GraphNode(puzzle)
        val expectedNeighbors = listOf(
            GraphNode(
                puzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 0, 8),
                move = Tile(8),
                parent = graphNode
            ),
            GraphNode(
                puzzle = Puzzle(1, 2, 3, 4, 5, 0, 7, 8, 6),
                move = Tile(6),
                parent = graphNode
            )
        )

        // Act
        val actualNeighbors = graphNode.findNeighbors()

        // Assert (the order of the neighbors is not relevant)
        assertTrue(expectedNeighbors.size == actualNeighbors.size)
        assertTrue(actualNeighbors.containsAll(expectedNeighbors))
    }

    @Test
    fun `findSolutionOrNull returns null for unsolvable puzzle`() = runTest {
        // Arrange
        val unsolvablePuzzle = Puzzle(1, 2, 3, 4, 5, 6, 8, 7, 0)

        // Act
        val solutionPath = findSolutionOrNull(unsolvablePuzzle, onStep = {})

        // Assert
        assertNull(solutionPath)
    }


    @Test
    fun `findSolutionOrNull returns the correct solution path for solvable puzzle`() = runTest {
        // Arrange
        val solvablePuzzle = Puzzle(1, 2, 3, 4, 6, 8, 7, 5, 0)
        val expectedSolution = listOf(Tile(8), Tile(6), Tile(5), Tile(8))

        // Act
        val actualSolution = findSolutionOrNull(solvablePuzzle, onStep = {})

        // Assert
        assertEquals(expectedSolution, actualSolution)
    }

    @Test
    fun `findSolutionOrNull returns empty list for solved puzzle`() = runTest {
        // Arrange
        val solvedPuzzle = Puzzle(1, 2, 3, 4, 5, 6, 7, 8, 0)

        // Act
        val solutionPath = findSolutionOrNull(solvedPuzzle, onStep = {})

        // Assert
        assertEquals(emptyList<Tile>(), solutionPath)
    }
}