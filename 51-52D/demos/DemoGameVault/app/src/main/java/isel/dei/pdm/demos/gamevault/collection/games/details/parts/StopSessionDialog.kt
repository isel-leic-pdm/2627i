package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.toPlayTime
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * The dialog through which the user confirms stopping the active play session, either because
 * they asked to stop it, or because they are leaving the screen, which stops it.
 *
 * What is asked depends on the session's length:
 * - If it is not short, the user confirms that the elapsed time will be added to the play time,
 * stopping the session, or keeps playing.
 * - If it is short (e.g. started by mistake), the user chooses whether to register it, i.e. to add
 * the elapsed time, or to discard it, or keeps playing.
 *
 * When leaving, the user is also told that leaving stops the session, and may stay instead.
 *
 * The dialog only displays the elapsed time it is given: keeping it up to date is up to the
 * caller. Likewise, it only emits the user's intents.
 *
 * @param elapsedTime The time elapsed since the active play session started.
 * @param isShort Whether the session is short.
 * @param isLeaving Whether the session is being stopped because the user is leaving the screen.
 * @param onStop Callback invoked when the user stops the session, with whether it is registered
 * (always, unless it is short and the user discards it).
 * @param onDismiss Callback invoked when the user keeps playing (or stays, when leaving), i.e.
 * the session is not stopped.
 * @param modifier Modifier to be applied to the dialog.
 */
@Composable
fun StopSessionDialog(
    elapsedTime: Duration,
    isShort: Boolean,
    isLeaving: Boolean,
    onStop: (register: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    when {
                        isLeaving -> R.string.game_details_stop_dialog_leave_title
                        isShort -> R.string.game_details_stop_dialog_short_title
                        else -> R.string.game_details_stop_dialog_title
                    }
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isLeaving) {
                    Text(text = stringResource(R.string.game_details_stop_dialog_leave_message))
                }
                Text(
                    text = elapsedTimeMessage(elapsedTime, isShort),
                    modifier = Modifier.testTag(StopSessionDialogTestTags.ELAPSED_TIME_MESSAGE)
                )
            }
        },
        dismissButton = {
            DialogButton(
                text = stringResource(
                    if (isLeaving) R.string.game_details_stop_dialog_stay
                    else R.string.game_details_stop_dialog_keep_playing
                ),
                onClick = onDismiss,
                tag = StopSessionDialogTestTags.DISMISS_BUTTON
            )
        },
        confirmButton = {
            if (isShort) {
                Row {
                    DialogButton(
                        text = stringResource(R.string.game_details_stop_dialog_discard),
                        onClick = { onStop(false) },
                        tag = StopSessionDialogTestTags.DISCARD_BUTTON
                    )
                    DialogButton(
                        text = stringResource(R.string.game_details_stop_dialog_register),
                        onClick = { onStop(true) },
                        tag = StopSessionDialogTestTags.REGISTER_BUTTON
                    )
                }
            } else {
                DialogButton(
                    text = stringResource(
                        if (isLeaving) R.string.game_details_stop_dialog_stop_and_leave
                        else R.string.game_details_stop_dialog_stop
                    ),
                    onClick = { onStop(true) },
                    tag = StopSessionDialogTestTags.STOP_BUTTON
                )
            }
        },
        modifier = modifier.testTag(StopSessionDialogTestTags.DIALOG)
    )
}

/**
 * The message about the session's elapsed time: the time to be added to the play time, or, if
 * the session is short, the question of whether to add it. During the first minute, the session
 * is said to have lasted less than a minute, instead of zero minutes.
 */
@Composable
private fun elapsedTimeMessage(elapsedTime: Duration, isShort: Boolean): String {
    val playTime = elapsedTime.toPlayTime().toDisplayText()
    return when {
        !isShort -> stringResource(R.string.game_details_stop_dialog_message, playTime)
        elapsedTime < 1.minutes ->
            stringResource(R.string.game_details_stop_dialog_just_started_message)
        else -> stringResource(R.string.game_details_stop_dialog_short_message, playTime)
    }
}

@Composable
private fun DialogButton(text: String, onClick: () -> Unit, tag: String) {
    TextButton(onClick = onClick, modifier = Modifier.testTag(tag)) {
        Text(text = text)
    }
}

// Testing & preview utilities

object StopSessionDialogTestTags {
    const val DIALOG = "StopSessionDialog"
    const val ELAPSED_TIME_MESSAGE = "StopSessionDialogElapsedTimeMessage"
    const val DISMISS_BUTTON = "StopSessionDialogDismissButton"
    const val STOP_BUTTON = "StopSessionDialogStopButton"
    const val DISCARD_BUTTON = "StopSessionDialogDiscardButton"
    const val REGISTER_BUTTON = "StopSessionDialogRegisterButton"
}

@Suppress("Unused")
class StopSessionDialogPreviews {

    @Preview(name = "Stopping")
    @Composable
    fun StoppingPreview() {
        DemoGameVaultTheme {
            StopSessionDialog(
                elapsedTime = 72.minutes,
                isShort = false,
                isLeaving = false,
                onStop = { },
                onDismiss = { }
            )
        }
    }

    @Preview(name = "Stopping a short session")
    @Composable
    fun StoppingShortPreview() {
        DemoGameVaultTheme {
            StopSessionDialog(
                elapsedTime = 3.minutes,
                isShort = true,
                isLeaving = false,
                onStop = { },
                onDismiss = { }
            )
        }
    }

    @Preview(name = "Leaving during a just started session")
    @Composable
    fun LeavingJustStartedPreview() {
        DemoGameVaultTheme {
            StopSessionDialog(
                elapsedTime = 20.seconds,
                isShort = true,
                isLeaving = true,
                onStop = { },
                onDismiss = { }
            )
        }
    }

    @Preview(name = "Leaving")
    @Composable
    fun LeavingPreview() {
        DemoGameVaultTheme {
            StopSessionDialog(
                elapsedTime = 72.minutes,
                isShort = false,
                isLeaving = true,
                onStop = { },
                onDismiss = { }
            )
        }
    }
}
