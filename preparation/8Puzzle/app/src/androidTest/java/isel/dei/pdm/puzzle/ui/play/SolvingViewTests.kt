package isel.dei.pdm.puzzle.ui.play

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme
import isel.dei.pdm.puzzle.ui.tileTag
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
            _8PuzzleTheme {
                SolvingView(board = board, onMoveRequested = {}, onResetRequested = {})
            }
        }

        // Assert
        Board.TILE_RANGE.forEach { tile ->
            composeTestRule.onNodeWithTag(tileTag(tile)).assertIsDisplayed()
        }
    }

    @Test
    fun playScreenSolvingView_shows_resetButtonEnabled() {
        // Arrange
        composeTestRule.setContent {
            _8PuzzleTheme {
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
            _8PuzzleTheme {
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
    fun playScreenSolvingView_pressingResetButton_callsOnResetRequested() {
        // Arrange
        var resetRequested = false
        composeTestRule.setContent {
            _8PuzzleTheme {
                SolvingView(
                    board = Board.SOLVED,
                    onMoveRequested = {},
                    onResetRequested = { resetRequested = true }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(ResetButtonTag).performClick()

        // Assert
        assert(resetRequested) { "onResetRequested should have been called when Reset button is clicked." }
    }
}
