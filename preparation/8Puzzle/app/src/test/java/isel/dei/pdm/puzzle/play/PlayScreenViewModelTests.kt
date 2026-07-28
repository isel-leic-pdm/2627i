package isel.dei.pdm.puzzle.play

import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class PlayScreenViewModelTests {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(testDispatcher)

    @Test
    fun `initial state is Idle`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        
        // Assert
        assertEquals(PlayScreenState.Idle, viewModel.state.value)
    }

    @Test
    fun `start changes state to Solving`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        
        // Act
        viewModel.start()
        
        // Assert
        assertTrue(viewModel.state.value is PlayScreenState.Solving)
    }

    @Test
    fun `reset changes state to Idle`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        viewModel.start()
        
        // Act
        viewModel.reset()
        
        // Assert
        assertEquals(PlayScreenState.Idle, viewModel.state.value)
    }

    @Test
    fun `move updates board in Solving state`() {
        // Arrange
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(Board.SOLVED))
        // Solved board is [1, 2, 3, 4, 5, 6, 7, 8, 0]. 8 is adjacent to blank.
        
        // Act
        viewModel.move(8)
        
        // Assert
        val nextState = viewModel.state.value as PlayScreenState.Solving
        assertEquals(Board.SOLVED.move(8), nextState.board)
    }

    @Test
    fun `move to solved board changes state to Solved and then back to Idle`() = runTest {
        // Arrange
        // [1, 2, 3, 4, 5, 6, 7, 0, 8] is almost solved.
        val almostSolvedBoard = Board.SOLVED.move(8)
        val viewModel =
            PlayScreenViewModel(initialState = PlayScreenState.Solving(almostSolvedBoard))
        
        // Act
        viewModel.move(8)

        // Assert: the board is updated immediately, but the screen stays in Solving so the
        // last tile's slide animation can play out.
        assertTrue(viewModel.state.value is PlayScreenState.Solving)
        assertTrue((viewModel.state.value as PlayScreenState.Solving).board.isSolved)

        // Act: simulate animation finishing
        viewModel.onAnimationFinished()
        testDispatcher.scheduler.runCurrent()

        // Assert: only now do we reach Solved
        assertEquals(PlayScreenState.Solved, viewModel.state.value)

        // Act: Advance time
        testDispatcher.scheduler.advanceTimeBy(SOLVED_TIMEOUT_MS.milliseconds)
        testDispatcher.scheduler.runCurrent()

        // Assert: We are back to Idle
        assertEquals(PlayScreenState.Idle, viewModel.state.value)
    }

    @Test
    fun `autoSolve transitions state to AutoSolving and updates board over time`() = runTest {
        // Arrange
        // Board one move away from solved
        val board = Board.SOLVED.move(8)
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(board))
        val states = mutableListOf<PlayScreenState>()
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.toList(states)
        }

        // Act
        viewModel.autoSolve()
        
        // At T=500, first emission (initial board) is collected
        testScheduler.advanceTimeBy(AUTO_SOLVE_DELAY_MS.milliseconds)
        testScheduler.runCurrent()
        assertTrue(states.any { it is PlayScreenState.AutoSolving })
        
        // At T=1000, second emission (solved board) is collected and triggers transition to Solved
        testScheduler.advanceTimeBy(AUTO_SOLVE_DELAY_MS.milliseconds)
        testScheduler.runCurrent()
        
        // The solver reaches the goal and triggers transition to Solved
        assertTrue(states.any { it is PlayScreenState.Solved })

        collectJob.cancel()
    }

    @Test
    fun `stopAutoSolve returns to Solving state at current board position`() = runTest {
        // Arrange
        // Start from a board 2 moves away from solved, to avoid immediate completion
        val initial = Board.SOLVED.move(8).move(7) 
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(initial))
        
        // Act: Start autoSolve
        viewModel.autoSolve()
        
        // Advance to first emission (initial board)
        testScheduler.advanceTimeBy(AUTO_SOLVE_DELAY_MS.milliseconds)
        testScheduler.runCurrent()
        
        // Verify we are in AutoSolving with the initial board
        assertTrue(viewModel.state.value is PlayScreenState.AutoSolving)
        assertEquals(initial, (viewModel.state.value as PlayScreenState.AutoSolving).board)
        
        // Act: Stop
        viewModel.stopAutoSolve()
        testScheduler.runCurrent()
        
        // Assert
        assertTrue(viewModel.state.value is PlayScreenState.Solving)
        assertEquals(initial, (viewModel.state.value as PlayScreenState.Solving).board)
    }

    @Test
    fun `reset during autoSolve cancels job and returns to Idle`() = runTest {
        // Arrange
        val board = Board.SOLVED.move(8)
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(board))
        
        // Act: Start autoSolve
        viewModel.autoSolve()
        
        // Act: Reset immediately
        viewModel.reset()
        testScheduler.runCurrent()
        
        // Assert
        assertEquals(PlayScreenState.Idle, viewModel.state.value)
        
        // Advance time to verify no more updates happen
        testScheduler.advanceTimeBy(AUTO_SOLVE_DELAY_MS.milliseconds * 2)
        testScheduler.runCurrent()
        assertEquals(PlayScreenState.Idle, viewModel.state.value)
    }
}
