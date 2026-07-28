package isel.dei.pdm.puzzle.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The function type that can solve an 8-puzzle.
 */
typealias Solver = (Board) -> Flow<Board>

/**
 * An implementation of [Solver] that uses the Learning Real-Time A* (LRTA*) algorithm.
 * This algorithm implements an algorithm that searches for the solution of the 8-puzzle by
 * emitting each state through a [Flow] as it moves.
 *
 * This illustrates a reactive architecture where the solver incrementally produces the steps
 * that it takes to solve the puzzle.
 *
 * The original paper that introduces the algorithm is available here:
 * https://dl.acm.org/doi/10.5555/2887965.2887990
 * or in the book
 * "Artificial Intelligence: A Modern Approach" by Stuart Russell and Peter Norvig, section 4.5.3
 */
val learningRealTimeAStar: Solver = { initial ->
    flow {
        val memory = mutableMapOf<Board, Int>()
        fun getH(board: Board): Int = memory[board] ?: board.computeManhattanDistance()

        var current = initial
        emit(current)

        while (!current.isSolved) {
            val neighbors = current.getAdjacentBoards()
            if (neighbors.isEmpty()) break

            // Move to the neighbor with the minimum f-value.
            current = neighbors.minBy { 1 + getH(it) }
            emit(current)

            // Update the heuristic value for the current state (stored in memory)

            // First, we calculate f(s') = cost(s, s') + H(s') for all neighbors s'
            // In the 8-puzzle, the cost of any move is 1, hence, f(s') = 1 + H(s')
            val fValues = neighbors.map { 1 + getH(it) }.sorted()

            // Second, we update the heuristic for the current state to the second-best f-value.
            // This ensures the agent eventually explores all paths and doesn't get stuck in the
            // same state forever. (the one we are currently in)
            memory[current] = if (fValues.size > 1) fValues[1] else fValues[0]

        }
    }
}
