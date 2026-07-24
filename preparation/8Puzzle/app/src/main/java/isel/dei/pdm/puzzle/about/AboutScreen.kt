package isel.dei.pdm.puzzle.about

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val GitHubSectionTag = "GitHubSection"

@Composable
internal fun AboutScreen(onOpenGitHubRequested: () -> Unit) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        AboutScreenLandscape(onOpenGitHubRequested)
    } else {
        AboutScreenPortrait(onOpenGitHubRequested)
    }
}

@Composable
private fun AboutScreenPortrait(onOpenGitHubRequested: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pdm_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(0.5f).aspectRatio(1f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.about_screen_content),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        GitHubSection(onOpenGitHubRequested)
    }
}

@Composable
private fun AboutScreenLandscape(onOpenGitHubRequested: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.pdm_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxHeight(0.5f).aspectRatio(1f)
        )
        Spacer(modifier = Modifier.width(32.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.about_screen_content),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            GitHubSection(onOpenGitHubRequested)
        }
    }
}

@Composable
private fun GitHubSection(onOpenGitHubRequested: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onOpenGitHubRequested() }
            .testTag(GitHubSectionTag)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_github),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.about_screen_on_github),
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutScreenPreview() {
    Demo8PuzzleTheme {
        AboutScreen(onOpenGitHubRequested = {})
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun AboutScreenLandscapePreview() {
    Demo8PuzzleTheme {
        AboutScreen(onOpenGitHubRequested = {})
    }
}
