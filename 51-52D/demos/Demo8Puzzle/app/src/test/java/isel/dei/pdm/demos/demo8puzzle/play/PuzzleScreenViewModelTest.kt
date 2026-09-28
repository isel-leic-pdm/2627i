package isel.dei.pdm.demos.demo8puzzle.play

import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

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
        val sut = PuzzleScreenViewModel()
        sut.start()
        val initialState = sut.state as PuzzleScreenState.Solving

        // Act
        val tileToMove = initialState.puzzle.getMoveableTiles().shuffled().first()
        sut.moveTile(tileToMove)

        // Assert
        val newState = sut.state
        assertEquals(initialState.puzzle.move(tileToMove), newState.puzzle)
    }

    @Test
    fun `reset from Solving state transitions to Idle`() {
        // Arrange
        val sut = PuzzleScreenViewModel()
        sut.start()

        // Act
        sut.reset()

        // Assert
        val state = sut.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }

    @Test
    fun `state auto resets from Solved to Idle after timeout`() = runTest {
        // Arrange
        val viewModel = PuzzleScreenViewModel(solvedTimeoutMs = 100L)
        viewModel.start()

        // Act
        viewModel.solve()
        advanceTimeBy(150L.milliseconds)

        // Assert
        val state = viewModel.state
        assertTrue(state is PuzzleScreenState.Idle)
        assertEquals(solvedPuzzle, state.puzzle)
    }
}
