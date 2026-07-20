package isel.dei.pdm.puzzle.ui.play

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.domain.Board
import isel.dei.pdm.puzzle.ui.AdaptivePlayLayout
import isel.dei.pdm.puzzle.ui.PuzzleView
import isel.dei.pdm.puzzle.ui.theme._8PuzzleTheme

const val ResetButtonTag = "ResetButton"
const val ResetDialogTag = "ResetDialog"
const val ResetDialogConfirmTag = "ResetDialogConfirm"
const val ResetDialogDismissTag = "ResetDialogDismiss"

/**
 * The possible presentation states of the [SolvingView].
 */
enum class SolvingPresentationState {
    IDLE,
    CONFIRMING_RESET
}

/**
 * View displayed while the user is solving the puzzle.
 * @param board current board state.
 * @param onMoveRequested callback when a tile is clicked.
 * @param onResetRequested callback to reset the game.
 * @param initialPresentationState the initial presentation state of the view (mostly for testing).
 */
@Composable
fun SolvingView(
    board: Board,
    onMoveRequested: (Int) -> Unit,
    onResetRequested: () -> Unit,
    initialPresentationState: SolvingPresentationState = SolvingPresentationState.IDLE
) {
    var presentationState by rememberSaveable { mutableStateOf(initialPresentationState) }

    AdaptivePlayLayout(
        puzzle = {
            PuzzleView(board = board, onTileClick = onMoveRequested)
        },
        controls = {
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
fun ResetConfirmationDialog(
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
fun SolvingViewPreview() {
    _8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onResetRequested = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SolvingViewLandscapePreview() {
    _8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onResetRequested = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SolvingViewConfirmingResetPreview() {
    _8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onResetRequested = {},
            initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
        )
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun SolvingViewConfirmingResetLandscapePreview() {
    _8PuzzleTheme {
        SolvingView(
            board = Board.createRandom(),
            onMoveRequested = {},
            onResetRequested = {},
            initialPresentationState = SolvingPresentationState.CONFIRMING_RESET
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResetConfirmationDialogPreview() {
    _8PuzzleTheme {
        ResetConfirmationDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
