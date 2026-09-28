package isel.dei.pdm.demos.demo8puzzle.play

import android.content.res.Configuration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import isel.dei.pdm.demos.demo8puzzle.core.MAX_TILE_VALUE
import isel.dei.pdm.demos.demo8puzzle.core.MIN_TILE_VALUE
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme
import org.junit.Rule
import org.junit.Test

class PuzzleScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screen_starts_in_idle_state() {
        // Arrange & Act
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }

        // Assert
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertDoesNotExist()
    }

    @Test
    fun clicking_start_transitions_to_solving_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }

        // Act
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()

        // Assert
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertIsDisplayed()
    }

    @Test
    fun clicking_reset_transitions_back_to_idle_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()

        // Act
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).performClick()

        // Assert
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertDoesNotExist()
    }

    @Test
    fun clicking_solve_transitions_to_solved_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()

        // Act
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).performClick()

        // Assert
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertDoesNotExist()
    }

    @Test
    fun clicking_reset_in_solved_state_transitions_to_idle_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).performClick()

        // Act
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).performClick()

        // Assert
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertDoesNotExist()
    }

    @Test
    fun board_is_disabled_in_idle_state() {
        // Arrange & Act
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }

        // Assert
        for (value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            composeTestRule.onNodeWithTag(puzzleTileTag(value)).assertIsNotEnabled()
        }
    }

    @Test
    fun board_is_enabled_in_solving_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }

        // Act
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()

        // Assert
        for (value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            composeTestRule.onNodeWithTag(puzzleTileTag(value)).assertIsEnabled()
        }
    }

    @Test
    fun board_is_disabled_in_solved_state() {
        // Arrange
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
            }
        }
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()

        // Act
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).performClick()

        // Assert
        for (value in MIN_TILE_VALUE..MAX_TILE_VALUE) {
            composeTestRule.onNodeWithTag(puzzleTileTag(value)).assertIsNotEnabled()
        }
    }

    @Test
    fun landscape_screen_displays_board_and_controls() {
        val viewModel = PuzzleScreenViewModel()
        composeTestRule.setContent {
            val landscapeConfig = Configuration(LocalConfiguration.current).apply {
                orientation = Configuration.ORIENTATION_LANDSCAPE
            }
            CompositionLocalProvider(LocalConfiguration provides landscapeConfig) {
                Demo8PuzzleTheme {
                    PuzzleScreen(viewModel = viewModel)
                }
            }
        }

        composeTestRule.onNodeWithTag(PUZZLE_BOARD_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(START_BUTTON_TAG).assertIsDisplayed()

        composeTestRule.onNodeWithTag(START_BUTTON_TAG).performClick()
        composeTestRule.onNodeWithTag(RESET_BUTTON_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SOLVE_BUTTON_TAG).assertIsDisplayed()
    }
}
