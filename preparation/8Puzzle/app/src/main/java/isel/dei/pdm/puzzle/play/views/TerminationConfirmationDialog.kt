package isel.dei.pdm.puzzle.play.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val TerminationDialogTag = "TerminationDialog"
internal const val TerminationDialogConfirmTag = "TerminationDialogConfirm"
internal const val TerminationDialogDismissTag = "TerminationDialogDismiss"

@Composable
internal fun TerminationConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.play_screen_exit_dialog_title)) },
        text = { Text(stringResource(R.string.play_screen_exit_dialog_text)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(TerminationDialogConfirmTag)
            ) {
                Text(stringResource(R.string.play_screen_exit_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TerminationDialogDismissTag)
            ) {
                Text(stringResource(R.string.play_screen_exit_dialog_dismiss))
            }
        },
        modifier = Modifier.testTag(TerminationDialogTag)
    )
}

@Preview(showBackground = true)
@Composable
internal fun TerminationConfirmationDialogPreview() {
    Demo8PuzzleTheme {
        TerminationConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
