package isel.dei.pdm.puzzle.play.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class SolvingViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playScreenSolvingView_shows_providedBoard() {
        // Arrange
        val board = Board.SOLVED // Solved board for simplicity
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(board = board, onMoveRequested = {}, onResetRequested = {})
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(PuzzleViewTag).assertIsDisplayed()
    }

    @Test
    fun playScreenSolvingView_shows_resetButtonEnabled() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(board = Board.SOLVED, onMoveRequested = {}, onResetRequested = {})
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(ResetButtonTag).assertIsDisplayed().assertIsEnabled()
    }

    @Test
    fun playScreenSolvingView_pressingTile_callsOnMoveRequested() {
        // Arrange
        var moveRequestedTile: Int? = null
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = { moveRequestedTile = it },
                    onResetRequested = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert
        assert(moveRequestedTile == 1) { "onMoveRequested should have been called with tile 1." }
    }

    @Test
    fun playScreenSolvingView_pressingResetButton_shows_confirmationDialog() {
        // Arrange
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = {},
                    onResetRequested = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ResetButtonTag).performClick()

        // Assert
        composeTestRule.onNodeWithTag(ResetDialogTag).assertIsDisplayed()
    }

    @Test
    fun playScreenSolvingView_confirmingReset_callsOnResetRequested() {
        // Arrange
        var resetRequested = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = {},
                    onResetRequested = { resetRequested = true },
                    initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ResetDialogConfirmTag).performClick()

        // Assert
        assert(resetRequested) { "onResetRequested should have been called when Reset is confirmed." }
    }

    @Test
    fun playScreenSolvingView_dismissingReset_doesNotCallOnResetRequested() {
        // Arrange
        var resetRequested = false
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = {},
                    onResetRequested = { resetRequested = true },
                    initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ResetDialogDismissTag).performClick()

        // Assert
        assert(!resetRequested) { "onResetRequested should not have been called when Reset is dismissed." }
        composeTestRule.onNodeWithTag(ResetButtonTag).assertIsDisplayed()
    }

    @Test
    fun playScreenSolvingView_preservesPresentationState_afterStateRestoration() {
        // Arrange
        val stateRestorationTester = StateRestorationTester(composeTestRule)
        stateRestorationTester.setContent {
            Demo8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = {},
                    onResetRequested = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ResetButtonTag).performClick()
        stateRestorationTester.emulateSavedInstanceStateRestore()

        // Assert
        composeTestRule.onNodeWithTag(ResetDialogTag).assertIsDisplayed()
    }
}
