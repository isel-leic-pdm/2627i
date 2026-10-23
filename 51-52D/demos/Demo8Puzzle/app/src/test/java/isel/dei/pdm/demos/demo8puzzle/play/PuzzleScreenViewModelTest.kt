package isel.dei.pdm.demos.demo8puzzle.play

import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PuzzleScreenViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is Idle`() {
        // Arrange & Act
        val sut = PuzzleScreenViewModel()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `start from Idle transitions to Solving`() {
        // Arrange
        val sut = PuzzleScreenViewModel()

        // Act
        sut.start()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.Solving)
    }

    @Test
    fun `moveTile in Solving state updates puzzle state`() {
        // Arrange
        val sut = PuzzleScreenViewModel(initialState = PuzzleScreenState.Solving(solvedPuzzle))
        val initialState = sut.state as PuzzleScreenState.Solving

        // Act
        val tileToMove = initialState.puzzle.getMoveableTiles().first()
        sut.moveTile(tileToMove)

        // Assert
        val newState = sut.state
        assertEquals(initialState.puzzle.move(tileToMove), newState.puzzle)
    }

    @Test
    fun `reset from Solving state transitions to Idle`() {
        // Arrange
        val sut = PuzzleScreenViewModel(initialState = PuzzleScreenState.Solving(solvedPuzzle))

        // Act
        sut.reset()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `solve from Solving transitions to AutoSolving`() {
        // Arrange
        val nearSolvedPuzzle = solvedPuzzle.move(solvedPuzzle.getMoveableTiles().first())
        val sut = PuzzleScreenViewModel(
            initialState = PuzzleScreenState.Solving(nearSolvedPuzzle),
            solveDelayMs = 1000L
        )

        // Act
        sut.solve()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.AutoSolving)
    }

    @Test
    fun `stop from AutoSolving transitions to Solving`() {
        // Arrange
        val nearSolvedPuzzle = solvedPuzzle.move(solvedPuzzle.getMoveableTiles().first())
        val sut = PuzzleScreenViewModel(
            initialState = PuzzleScreenState.AutoSolving(nearSolvedPuzzle)
        )

        // Act
        sut.stop()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.Solving)
        assertEquals(nearSolvedPuzzle, state.puzzle)
    }

    @Test
    fun `state auto resets from Solved to Idle after timeout`() = runTest {
        // Arrange
        val nearSolvedPuzzle = solvedPuzzle.move(solvedPuzzle.getMoveableTiles().first())
        val viewModel = PuzzleScreenViewModel(
            initialState = PuzzleScreenState.Solving(nearSolvedPuzzle),
            solvedTimeoutMs = 100L,
            solveDelayMs = 0L,
            computationDispatcher = mainDispatcherRule.testDispatcher
        )

        // Act
        viewModel.solve()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state
        assertTrue("Expected Idle, but got $state", state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `viewModel can be initialized with custom initial state`() {
        // Arrange
        val customPuzzle = solvedPuzzle.move(solvedPuzzle.getMoveableTiles().first())
        val customState = PuzzleScreenState.Solving(customPuzzle)

        // Act
        val sut = PuzzleScreenViewModel(initialState = customState)

        // Assert
        assertEquals(customState, sut.state)
        assertEquals(customPuzzle, sut.state.puzzle)
    }
}
