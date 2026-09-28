package isel.dei.pdm.demos.demo8puzzle.play

import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleScreenViewModelTest {

    @Test
    fun `initial state is Idle`() {
        val viewModel = PuzzleScreenViewModel()
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `start from Idle transitions to Solving`() {
        val viewModel = PuzzleScreenViewModel()
        viewModel.start()
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Solving)
    }

    @Test
    fun `moveTile in Solving state updates puzzle state`() {
        val viewModel = PuzzleScreenViewModel()
        viewModel.start()
        val initialState = viewModel.state as PuzzleScreenState.Solving

        val nullIdx = initialState.puzzle.indexOf(null)
        val row = nullIdx / 3
        val col = nullIdx % 3
        val tileToMove: Tile = when {
            row > 0 -> initialState.puzzle[row - 1, col]
            row < 2 -> initialState.puzzle[row + 1, col]
            col > 0 -> initialState.puzzle[row, col - 1]
            else -> initialState.puzzle[row, col + 1]
        }!!

        viewModel.moveTile(tileToMove)

        val newState = viewModel.state
        assertEquals(initialState.puzzle.move(tileToMove), newState.puzzle)
    }

    @Test
    fun `reset from Solving state transitions to Idle`() {
        val viewModel = PuzzleScreenViewModel()
        viewModel.start()
        assertTrue(viewModel.state is PuzzleScreenState.Solving)

        viewModel.reset()
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `reset from Solved state transitions to Idle`() {
        val viewModel = PuzzleScreenViewModel()
        viewModel.start()
        viewModel.solve()
        assertTrue(viewModel.state is PuzzleScreenState.Solved)

        viewModel.reset()
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `solve from Solving state transitions to Solved`() {
        val viewModel = PuzzleScreenViewModel()
        viewModel.start()
        assertTrue(viewModel.state is PuzzleScreenState.Solving)

        viewModel.solve()
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Solved)
        assertEquals(solvedPuzzle, state.puzzle)
    }
}
