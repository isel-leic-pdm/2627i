package isel.dei.pdm.demos.demo8puzzle.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.dei.pdm.demos.demo8puzzle.R
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Tags used to identify elements in MainScreen (for testing purposes).
 */
const val MAIN_IMAGE_TAG = "MainImage"
const val TOUCH_TO_PLAY_BUTTON_TAG = "TouchToPlayButton"

/**
 * The main screen root composable.
 * @param onNavigateToPlay the callback to be invoked when the user intends to start playing.
 */
@Composable
fun MainScreen(onNavigateToPlay: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.main_image_description),
            modifier = Modifier
                .size(200.dp)
                .testTag(MAIN_IMAGE_TAG)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToPlay,
            modifier = Modifier.testTag(TOUCH_TO_PLAY_BUTTON_TAG)
        ) {
            Text(
                text = stringResource(id = R.string.touch_to_play),
                fontSize = 24.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    Demo8PuzzleTheme {
        MainScreen(onNavigateToPlay = { })
    }
}
