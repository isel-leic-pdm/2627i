package isel.dei.pdm.demos.demo8puzzle.about

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.dei.pdm.demos.demo8puzzle.R
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Tags used to identify elements in AboutScreen (for testing purposes).
 */
const val ABOUT_LOGO_TAG = "AboutLogo"
const val ABOUT_DESCRIPTION_TAG = "AboutDescription"
const val ABOUT_GITHUB_BUTTON_TAG = "AboutGitHubButton"

/**
 * The About screen root composable.
 * @param onNavigateToGitHub the callback to be invoked when the user clicks on the GitHub link
 */
@Composable
fun AboutScreen(onNavigateToGitHub: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = stringResource(id = R.string.about_logo_description),
            modifier = Modifier
                .size(160.dp)
                .testTag(ABOUT_LOGO_TAG)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.about_description),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag(ABOUT_DESCRIPTION_TAG)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onNavigateToGitHub,
            modifier = Modifier.testTag(ABOUT_GITHUB_BUTTON_TAG)
        ) {
            Text(
                text = stringResource(id = R.string.about_github_button),
                fontSize = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AboutScreenPreview() {
    Demo8PuzzleTheme {
        AboutScreen(onNavigateToGitHub = { })
    }
}
