package isel.dei.pdm.puzzle.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.play.views.ResetButtonTag
import isel.dei.pdm.puzzle.play.views.ResetDialogConfirmTag
import isel.dei.pdm.puzzle.play.views.SolveButtonTag
import isel.dei.pdm.puzzle.play.views.StartButtonTag
import isel.dei.pdm.puzzle.play.views.StopButtonTag
import isel.dei.pdm.puzzle.play.views.SuccessMessageTag
import isel.dei.pdm.puzzle.play.views.tileTag
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class PlayScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreen_starts_in_Idle_and_can_transition_to_Solving() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {})
            }
        }

        // Assert: We are in Idle
        composeTestRule.onNodeWithTag(StartButtonTag).assertIsDisplayed()

        // Act: Start the game
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Assert: We are in Solving
        composeTestRule.onNodeWithTag(ResetButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_can_transition_from_Solving_back_to_Idle_via_Reset() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {})
            }
        }
        // Transition to Solving
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Act: Reset the game
        composeTestRule.onNodeWithTag(ResetButtonTag).performClick()
        // Confirm reset in the dialog
        composeTestRule.onNodeWithTag(ResetDialogConfirmTag).performClick()

        // Assert: We are back in Idle
        composeTestRule.onNodeWithTag(StartButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_transitions_to_Solved_after_winning_move_and_animation() {
        // Arrange
        // almost solved board: [1, 2, 3, 4, 5, 6, 7, 0, 8]
        val almostSolvedBoard = Board.SOLVED.move(8)
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(almostSolvedBoard))
        
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {}, viewModel = viewModel)
            }
        }

        // Act: perform winning move (clicking 8)
        composeTestRule.onNodeWithTag(tileTag(8)).performClick()
        
        // Assert: SolvedView is now displayed (eventually)
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag(SuccessMessageTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(SuccessMessageTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_transitions_back_to_Idle_after_solved_timeout() {
        // Arrange
        // To trigger the timer, we must go from Solving(SolvedBoard) -> Solved
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(Board.SOLVED))
        
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {}, viewModel = viewModel)
            }
        }

        // Act: trigger transition to Solved (simulating animation finish)
        viewModel.onAnimationFinished()
        
        // Wait for Solved state to appear
        composeTestRule.onNodeWithTag(SuccessMessageTag).assertIsDisplayed()
        
        // Act: wait for SOLVED_TIMEOUT_MS and transition back to Idle
        composeTestRule.waitUntil(timeoutMillis = SOLVED_TIMEOUT_MS + 2000) {
            composeTestRule.onAllNodesWithTag(StartButtonTag).fetchSemanticsNodes().isNotEmpty()
        }
        
        // Assert: we are back in Idle
        composeTestRule.onNodeWithTag(StartButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_can_transition_from_Solving_to_AutoSolving() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {})
            }
        }
        // Enter Solving
        composeTestRule.onNodeWithTag(StartButtonTag).performClick()

        // Act: Click Solve
        composeTestRule.onNodeWithTag(SolveButtonTag).performClick()

        // Assert: We are in AutoSolving (Stop button is visible)
        composeTestRule.waitUntil(5000) {
            composeTestRule.onAllNodesWithTag(StopButtonTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(StopButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_can_transition_from_AutoSolving_back_to_Solving_via_Stop() {
        // Arrange
        val viewModel = PlayScreenViewModel(
            initialState = PlayScreenState.AutoSolving(Board.SOLVED.move(8))
        )
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {}, viewModel = viewModel)
            }
        }

        // Act: Click Stop
        composeTestRule.onNodeWithTag(StopButtonTag).performClick()

        // Assert: We are back in Solving (Reset button is visible)
        composeTestRule.onNodeWithTag(ResetButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreen_transitions_to_Solved_after_autoSolve_reaches_goal() {
        // Arrange
        // 1 move away board: [1, 2, 3, 4, 5, 6, 7, 0, 8]
        val almostSolvedBoard = Board.SOLVED.move(8)
        val viewModel = PlayScreenViewModel(initialState = PlayScreenState.Solving(almostSolvedBoard))

        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PlayScreen(onInfoRequested = {}, viewModel = viewModel)
            }
        }

        // Act: Start auto-solve
        composeTestRule.onNodeWithTag(SolveButtonTag).performClick()

        // Assert: Transitions to Solved
        composeTestRule.waitUntil(10000) {
            composeTestRule.onAllNodesWithTag(SuccessMessageTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(SuccessMessageTag).assertIsDisplayed()
    }
}
