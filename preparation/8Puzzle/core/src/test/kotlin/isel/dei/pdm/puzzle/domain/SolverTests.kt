package isel.dei.pdm.puzzle.domain

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolverTests {

    @Test
    fun `learningRealTimeAStar solves an already solved board`() = runBlocking {
        val path = learningRealTimeAStar(Board.SOLVED).toList()
        assertEquals(1, path.size)
        assertEquals(Board.SOLVED, path[0])
    }

    @Test
    fun `learningRealTimeAStar solves a simple board`() = runBlocking {
        // 1 2 3
        // 4 5 6
        // 7 0 8
        val initial = Board(listOf(1, 2, 3, 4, 5, 6, 7, 0, 8))
        val path = learningRealTimeAStar(initial).toList()
        
        // LRTA* should take exactly 1 move for this one
        assertEquals(2, path.size)
        assertEquals(initial, path[0])
        assertEquals(Board.SOLVED, path[1])
    }

    @Test
    fun `learningRealTimeAStar reaches the goal for boards requiring multiple moves`() = runBlocking {
        // 1 2 0
        // 4 5 3
        // 7 8 6
        // 2 moves away
        val initial = Board(listOf(1, 2, 0, 4, 5, 3, 7, 8, 6))
        val path = learningRealTimeAStar(initial).toList()
        
        assertTrue(path.size >= 3)
        assertTrue(path.last().isSolved)
        
        // Verify each step is a single move from the previous one
        path.zipWithNext().forEach { (prev, next) ->
            assertTrue(prev.getAdjacentBoards().contains(next))
        }
    }
}
