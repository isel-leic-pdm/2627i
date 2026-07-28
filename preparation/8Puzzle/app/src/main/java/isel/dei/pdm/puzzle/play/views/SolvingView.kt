package isel.dei.pdm.puzzle.play.views

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val ResetButtonTag = "ResetButton"
internal const val SolveButtonTag = "SolveButton"
internal const val ResetDialogTag = "ResetDialog"
internal const val ResetDialogConfirmTag = "ResetDialogConfirm"
internal const val ResetDialogDismissTag = "ResetDialogDismiss"

/**
 * The possible presentation states of the [SolvingView].
 */
internal enum class SolvingPresentationState {
    IDLE,
    CONFIRMING_RESET
}

/**
 * View displayed while the user is solving the puzzle.
 * @param board current board state.
 * @param onMoveRequested callback when a tile is clicked.
 * @param onAnimationFinished callback when the tile animation is finished.
 * @param onAutoSolveRequested callback when the auto-solve button is clicked.
 * @param onResetRequested callback to reset the game.
 * @param modifier the modifier to be applied to the layout.
 * @param initialPresentationState the initial presentation state of the view (mostly for testing).
 */
@Composable
internal fun SolvingView(
    board: Board,
    onMoveRequested: (Int) -> Unit,
    onAnimationFinished: () -> Unit,
    onAutoSolveRequested: () -> Unit,
    onResetRequested: () -> Unit,
    modifier: Modifier = Modifier,
    initialPresentationState: SolvingPresentationState = SolvingPresentationState.IDLE
) {
    var presentationState by rememberSaveable { mutableStateOf(initialPresentationState) }

    AdaptivePlayLayout(
        modifier = modifier,
        puzzle = {
            PuzzleView(
                board = board,
                onTileClick = onMoveRequested,
                onAnimationFinished = onAnimationFinished
            )
        },
        controls = {
            val orientation = LocalConfiguration.current.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoSolveRequested,
                        modifier = Modifier.testTag(SolveButtonTag)
                    ) {
                        Text(
                            text = stringResource(R.string.play_screen_solve_button),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Button(
                        onClick = { presentationState = SolvingPresentationState.CONFIRMING_RESET },
                        modifier = Modifier.testTag(ResetButtonTag)
                    ) {
                        Text(
                            text = stringResource(R.string.play_screen_reset_button),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoSolveRequested,
                        modifier = Modifier.testTag(SolveButtonTag)
                    ) {
                        Text(
                            text = stringResource(R.string.play_screen_solve_button),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Button(
                        onClick = { presentationState = SolvingPresentationState.CONFIRMING_RESET },
                        modifier = Modifier.testTag(ResetButtonTag)
                    ) {
                        Text(
                            text = stringResource(R.string.play_screen_reset_button),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    )

    if (presentationState == SolvingPresentationState.CONFIRMING_RESET) {
        ResetConfirmationDialog(
            onConfirm = {
                presentationState = SolvingPresentationState.IDLE
                onResetRequested()
            },
            onDismiss = { presentationState = SolvingPresentationState.IDLE }
        )
    }
}

/**
 * Dialog to confirm game reset.
 */
@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.play_screen_reset_dialog_title)) },
        text = { Text(stringResource(R.string.play_screen_reset_dialog_text)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(ResetDialogConfirmTag)
            ) {
                Text(stringResource(R.string.play_screen_reset_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(ResetDialogDismissTag)
            ) {
                Text(stringResource(R.string.play_screen_reset_dialog_dismiss))
            }
        },
        modifier = Modifier.testTag(ResetDialogTag)
    )
}

@Preview(showBackground = true)
@Composable
private fun SolvingViewPreview() {
    Demo8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onAnimationFinished = {},
            onAutoSolveRequested = {},
            onResetRequested = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun SolvingViewLandscapePreview() {
    Demo8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onAnimationFinished = {},
            onAutoSolveRequested = {},
            onResetRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SolvingViewConfirmingResetPreview() {
    Demo8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onAnimationFinished = {},
            onAutoSolveRequested = {},
            onResetRequested = {},
            initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun SolvingViewConfirmingResetLandscapePreview() {
    Demo8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onAnimationFinished = {},
            onAutoSolveRequested = {},
            onResetRequested = {},
            initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetConfirmationDialogPreview() {
    Demo8PuzzleTheme {
        ResetConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
