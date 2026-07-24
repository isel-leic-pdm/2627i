package isel.dei.pdm.puzzle.play.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.puzzle.R
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

internal const val InfoButtonTag = "InfoButton"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayScreenTopBar(onInfoClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        actions = {
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.testTag(InfoButtonTag)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.play_screen_top_bar_info)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PlayScreenTopBarPreview() {
    Demo8PuzzleTheme {
        PlayScreenTopBar(onInfoClick = {})
    }
}
