package isel.dei.pdm.demos.demo8puzzle.play

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.dei.pdm.demos.demo8puzzle.R
import isel.dei.pdm.demos.demo8puzzle.core.Tile
import isel.dei.pdm.demos.demo8puzzle.core.solvedPuzzle
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Tags used to identify the relevant buttons in the UI (for testing purposes)
 */
const val START_BUTTON_TAG = "StartButton"
const val RESET_BUTTON_TAG = "ResetButton"
const val SOLVE_BUTTON_TAG = "SolveButton"

/**
 * Root composable for the play screen.
 * @param viewModel The view model for this screen.
 */
@Composable
fun PuzzleScreen(viewModel: PuzzleScreenViewModel) {
    PuzzleScreenContent(
        state = viewModel.state,
        onTileClicked = { viewModel.moveTile(it) },
        onStart = { viewModel.start() },
        onReset = { viewModel.reset() },
        onSolve = { viewModel.solve() }
    )
}

/**
 * Stateless composable for displaying the play screen actual contents.
 * Elects the correct layout variant based on orientation.
 * @param state The current state of the screen.
 * @param onTileClicked Callback for when a tile is clicked.
 * @param onStart Callback for when the start button is clicked.
 * @param onReset Callback for when the reset button is clicked.
 * @param onSolve Callback for when the solve button is clicked.
 */
@Composable
private fun PuzzleScreenContent(
    state: PuzzleScreenState,
    onTileClicked: (Tile) -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onSolve: () -> Unit
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        PuzzleScreenLandscapeContent(
            state = state,
            onTileClicked = onTileClicked,
            onStart = onStart,
            onReset = onReset,
            onSolve = onSolve
        )
    } else {
        PuzzleScreenPortraitContent(
            state = state,
            onTileClicked = onTileClicked,
            onStart = onStart,
            onReset = onReset,
            onSolve = onSolve
        )
    }
}

@Composable
private fun PuzzleScreenPortraitContent(
    state: PuzzleScreenState,
    onTileClicked: (Tile) -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onSolve: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        PuzzleBoard(
            puzzle = state.puzzle,
            onTileClicked = onTileClicked,
            enabled = state is PuzzleScreenState.Solving
        )
        Spacer(modifier = Modifier.height(32.dp))
        PuzzleControls(
            state = state,
            onStart = onStart,
            onReset = onReset,
            onSolve = onSolve
        )
    }
}

@Composable
private fun PuzzleScreenLandscapeContent(
    state: PuzzleScreenState,
    onTileClicked: (Tile) -> Unit,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onSolve: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PuzzleBoard(
            puzzle = state.puzzle,
            onTileClicked = onTileClicked,
            enabled = state is PuzzleScreenState.Solving
        )
        Spacer(modifier = Modifier.width(32.dp))
        PuzzleControls(
            state = state,
            onStart = onStart,
            onReset = onReset,
            onSolve = onSolve
        )
    }
}

@Composable
private fun PuzzleControls(
    state: PuzzleScreenState,
    onStart: () -> Unit,
    onReset: () -> Unit,
    onSolve: () -> Unit
) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    when (state) {
        is PuzzleScreenState.Idle -> {
            Button(
                onClick = onStart,
                modifier = Modifier.testTag(START_BUTTON_TAG)
            ) {
                Text(text = stringResource(id = R.string.start_button), fontSize = 24.sp)
            }
        }
        is PuzzleScreenState.Solving -> {
            if (isLandscape) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = onReset,
                        modifier = Modifier.testTag(RESET_BUTTON_TAG)
                    ) {
                        Text(text = stringResource(id = R.string.reset_button), fontSize = 24.sp)
                    }
                    Button(
                        onClick = onSolve,
                        modifier = Modifier.testTag(SOLVE_BUTTON_TAG)
                    ) {
                        Text(text = stringResource(id = R.string.solve_button), fontSize = 24.sp)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onReset,
                        modifier = Modifier.testTag(RESET_BUTTON_TAG)
                    ) {
                        Text(text = stringResource(id = R.string.reset_button), fontSize = 24.sp)
                    }
                    Button(
                        onClick = onSolve,
                        modifier = Modifier.testTag(SOLVE_BUTTON_TAG)
                    ) {
                        Text(text = stringResource(id = R.string.solve_button), fontSize = 24.sp)
                    }
                }
            }
        }
        is PuzzleScreenState.Solved -> {
            Button(
                onClick = onReset,
                modifier = Modifier.testTag(RESET_BUTTON_TAG)
            ) {
                Text(text = stringResource(id = R.string.reset_button), fontSize = 24.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreviewIdle() {
    Demo8PuzzleTheme {
        PuzzleScreenContent(
            state = PuzzleScreenState.Idle(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreviewSolving() {
    Demo8PuzzleTheme {
        PuzzleScreenContent(
            state = PuzzleScreenState.Solving(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PuzzleScreenPreviewSolved() {
    Demo8PuzzleTheme {
        PuzzleScreenContent(
            state = PuzzleScreenState.Solved(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun PuzzleScreenLandscapePreviewIdle() {
    Demo8PuzzleTheme {
        PuzzleScreenLandscapeContent(
            state = PuzzleScreenState.Idle(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun PuzzleScreenLandscapePreviewSolving() {
    Demo8PuzzleTheme {
        PuzzleScreenLandscapeContent(
            state = PuzzleScreenState.Solving(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
fun PuzzleScreenLandscapePreviewSolved() {
    Demo8PuzzleTheme {
        PuzzleScreenLandscapeContent(
            state = PuzzleScreenState.Solved(solvedPuzzle),
            onTileClicked = { },
            onStart = { },
            onReset = { },
            onSolve = { }
        )
    }
}
