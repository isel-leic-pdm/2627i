package isel.dei.pdm.puzzle.play

import isel.dei.pdm.puzzle.domain.Board
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class PlayScreenViewModelTests {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `initial state is Idle`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        
        // Assert
        assertEquals(PlayScreenState.Idle, viewModel.state)
    }

    @Test
    fun `start changes state to Solving`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        
        // Act
        viewModel.start()
        
        // Assert
        assertTrue(viewModel.state is PlayScreenState.Solving)
    }

    @Test
    fun `reset changes state to Idle`() {
        // Arrange
        val viewModel = PlayScreenViewModel()
        viewModel.start()
        
        // Act
        viewModel.reset()
        
        // Assert
        assertEquals(PlayScreenState.Idle, viewModel.state)
    }

    @Test
    fun `move updates board in Solving state`() {
        // Arrange
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(Board.SOLVED))
        // Solved board is [1, 2, 3, 4, 5, 6, 7, 8, 0]. 8 is adjacent to blank.
        
        // Act
        viewModel.move(8)
        
        // Assert
        val nextState = viewModel.state as PlayScreenState.Solving
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
        
        // Assert
        assertEquals(PlayScreenState.Solved, viewModel.state)
        
        // Act: Advance time
        testDispatcher.scheduler.advanceTimeBy(SOLVED_TIMEOUT_MS.milliseconds)
        testDispatcher.scheduler.runCurrent()
        
        // Assert: We are back to Idle
        assertEquals(PlayScreenState.Idle, viewModel.state)
    }
}
