package isel.dei.pdm.puzzle.play.views

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test

class PuzzleViewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testBoard = Board.SOLVED.move(8)

    @Test
    fun puzzleView_should_display_tiles_at_correct_positions() {
        // Arrange
        // Act
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleView(board = testBoard, onAnimationFinished = {}, onTileClick = {})
            }
        }

        // Assert
        // Tiles are now siblings of the cells (not nested inside them) so that they can slide
        // across cells when the board changes, so position is verified by comparing their
        // respective bounds.
        for (row in 0 until Board.BOARD_SIDE) {
            for (col in 0 until Board.BOARD_SIDE) {
                val boardTile = testBoard.getTileAt(Board.Coordinate(row, col))
                val cellPosition = composeTestRule
                    .onNodeWithTag(cellTag(row, col))
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .topLeft
                if (boardTile != null) {
                    val tileViewPosition = composeTestRule
                        .onNodeWithTag(tileTag(boardTile))
                        .fetchSemanticsNode()
                        .boundsInRoot
                        .topLeft
                    assertEquals(cellPosition, tileViewPosition)
                } else {
                    // Verify that we do not have a tile view at the puzzle's empty space position
                    Board.TILE_RANGE.forEach { t ->
                        val tileViewPosition = composeTestRule
                            .onNodeWithTag(tileTag(t))
                            .fetchSemanticsNode()
                            .boundsInRoot
                            .topLeft
                        assertNotEquals(cellPosition, tileViewPosition)
                    }
                }
            }
        }
    }

    @Test
    fun puzzleView_should_notify_on_tile_click() {
        // Arrange
        var clickedTile: Int? = null
        val expectedTile = 1

        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleView(
                    board = testBoard,
                    onAnimationFinished = {},
                    onTileClick = { clickedTile = it }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(tileTag(expectedTile)).performClick()

        // Assert
        assertEquals(expectedTile, clickedTile)
    }

    @Test
    fun puzzleView_shouldIgnoreClicks_duringAnimation() {
        // Arrange
        var clickCount = 0
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            var currentBoard by remember { mutableStateOf(testBoard) }
            Demo8PuzzleTheme {
                PuzzleView(
                    board = currentBoard,
                    onAnimationFinished = {},
                    onTileClick = { tile ->
                        clickCount++
                        currentBoard = currentBoard.move(tile)
                    }
                )
            }
        }

        // Act: start a move (tile 7 is adjacent to the blank space in testBoard), which begins
        // the slide animation, then try clicking another tile before it settles.
        composeTestRule.onNodeWithTag(tileTag(7)).performClick()
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert: the second click was ignored while the first move was still animating.
        assertEquals(1, clickCount)
    }

    @Test
    fun puzzleView_shouldAcceptClicks_afterAnimation() {
        // Arrange
        var clickCount = 0

        composeTestRule.setContent {
            var currentBoard by remember { mutableStateOf(testBoard) }
            Demo8PuzzleTheme {
                PuzzleView(
                    board = currentBoard,
                    onAnimationFinished = {},
                    onTileClick = { tile ->
                        clickCount++
                        currentBoard = currentBoard.move(tile)
                    }
                )
            }
        }

        // perform a move and let it settle to reach the idle state
        composeTestRule.onNodeWithTag(tileTag(7)).performClick()
        composeTestRule.waitForIdle()

        // Act: click again now that the UI is idle
        composeTestRule.onNodeWithTag(tileTag(1)).performClick()

        // Assert: the click was accepted
        assertEquals(2, clickCount)
    }

    @Test
    fun puzzleView_callsOnAnimationFinished_whenAnimationCompletes() {
        // Arrange
        var animationFinishedCalled = 0

        composeTestRule.setContent {
            var currentBoard by remember { mutableStateOf(testBoard) }
            Demo8PuzzleTheme {
                PuzzleView(
                    board = currentBoard,
                    onAnimationFinished = { animationFinishedCalled++ },
                    onTileClick = { tile ->
                        currentBoard = currentBoard.move(tile)
                    }
                )
            }
        }

        // perform a move and let the animation finish
        composeTestRule.onNodeWithTag(tileTag(7)).performClick()
        composeTestRule.waitForIdle()

        // Assert: called exactly once after the move
        assertEquals(1, animationFinishedCalled)
    }
}
