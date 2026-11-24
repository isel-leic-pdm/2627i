package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.toPlayTime
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * The play session controls, at the bottom of the game details screen: a button that starts a
 * play session or, while one is active, a button that stops it, in the error color, below the
 * time elapsed since the session started.
 *
 * The bar only displays the elapsed time it is given: keeping it up to date (e.g. every minute)
 * is up to the caller. Likewise, it only emits the user's intents: what happens when a session is
 * stopped (e.g. whether the user is asked to confirm the elapsed time) is also up to the caller.
 *
 * @param sessionElapsedTime The time elapsed since the active play session started, or null if
 * no session is active.
 * @param onStart Callback invoked when the user asks to start a play session.
 * @param onStop Callback invoked when the user asks to stop the active play session.
 * @param modifier Modifier to be applied to the bar's layout.
 */
@Composable
fun PlaySessionBar(
    sessionElapsedTime: Duration?,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        if (sessionElapsedTime != null) {
            SessionElapsedTime(sessionElapsedTime)
            SessionButton(
                icon = Icons.Filled.Stop,
                text = stringResource(R.string.game_details_stop_playing),
                onClick = onStop,
                tag = PlaySessionBarTestTags.STOP_BUTTON,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            )
        } else {
            SessionButton(
                icon = Icons.Filled.PlayArrow,
                text = stringResource(R.string.game_details_start_playing),
                onClick = onStart,
                tag = PlaySessionBarTestTags.START_BUTTON
            )
        }
    }
}

/**
 * The bar while the game is being read: a disabled button without label, as whether a session is
 * active, i.e. whether it would start or stop one, is not known yet. The button has the height of
 * the labelled ones, so that the layout does not change once the game is displayed.
 *
 * @param modifier Modifier to be applied to the bar's layout.
 */
@Composable
fun PlaySessionBarPlaceholder(modifier: Modifier = Modifier) {
    Button(
        onClick = { },
        enabled = false,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = modifier
            .fillMaxWidth()
            .testTag(PlaySessionBarTestTags.PLACEHOLDER_BUTTON)
    ) {
        // An empty line, so that the button is as tall as the labelled ones
        Text(text = "")
    }
}

/**
 * The time elapsed since the active play session started, in whole minutes, as the play time.
 * During the first minute, the session is said to have just started, instead of displaying zero
 * minutes.
 */
@Composable
private fun SessionElapsedTime(elapsedTime: Duration) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = if (elapsedTime < 1.minutes) {
                stringResource(R.string.game_details_session_just_started)
            } else {
                stringResource(
                    R.string.game_details_playing_for,
                    elapsedTime.toPlayTime().toDisplayText()
                )
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag(PlaySessionBarTestTags.ELAPSED_TIME)
        )
    }
}

/**
 * A full width button of the bar, with a decorative [icon] before its [text].
 */
@Composable
private fun SessionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tag: String,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        colors = colors,
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(text = text)
    }
}

// Testing & preview utilities

object PlaySessionBarTestTags {
    const val START_BUTTON = "PlaySessionBarStartButton"
    const val STOP_BUTTON = "PlaySessionBarStopButton"
    const val ELAPSED_TIME = "PlaySessionBarElapsedTime"
    const val PLACEHOLDER_BUTTON = "PlaySessionBarPlaceholderButton"
}

@Suppress("Unused")
class PlaySessionBarPreviews {

    @Preview(showBackground = true, name = "No active session")
    @Composable
    fun NoSessionPreview() {
        DemoGameVaultTheme {
            PlaySessionBar(
                sessionElapsedTime = null,
                onStart = { },
                onStop = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Active session")
    @Composable
    fun ActiveSessionPreview() {
        DemoGameVaultTheme {
            PlaySessionBar(
                sessionElapsedTime = 72.minutes,
                onStart = { },
                onStop = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
