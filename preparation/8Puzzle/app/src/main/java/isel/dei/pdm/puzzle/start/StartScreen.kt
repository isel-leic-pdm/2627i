package isel.dei.pdm.puzzle.start

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val StartScreenTag = "StartScreen"

/**
 * The start screen of the application.
 * @param onStartRequested callback to start the game.
 */
@Composable
internal fun StartScreen(onStartRequested: () -> Unit) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        StartScreenLandscape(onStartRequested)
    } else {
        StartScreenPortrait(onStartRequested)
    }
}

@Composable
private fun StartScreenPortrait(onStartRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .clickable { onStartRequested() }
            .padding(16.dp)
            .testTag(StartScreenTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_puzzle_start),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.6f).aspectRatio(1f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        PulsingText(text = stringResource(R.string.start_screen_touch_to_play))
    }
}

@Composable
private fun StartScreenLandscape(onStartRequested: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .clickable { onStartRequested() }
            .padding(16.dp)
            .testTag(StartScreenTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_puzzle_start),
            contentDescription = null,
            modifier = Modifier.fillMaxHeight(0.6f).aspectRatio(1f)
        )
        Spacer(modifier = Modifier.width(32.dp))
        PulsingText(text = stringResource(R.string.start_screen_touch_to_play))
    }
}

/**
 * A composable that displays a pulsing text.
 * @param text the text to display.
 */
@Composable
private fun PulsingText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "touchToPlayPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "touchToPlayAlpha"
    )
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.alpha(alpha)
    )
}

@Preview(showBackground = true)
@Composable
private fun StartScreenPreview() {
    Demo8PuzzleTheme {
        StartScreen(onStartRequested = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun StartScreenLandscapePreview() {
    Demo8PuzzleTheme {
        StartScreen(onStartRequested = {})
    }
}
