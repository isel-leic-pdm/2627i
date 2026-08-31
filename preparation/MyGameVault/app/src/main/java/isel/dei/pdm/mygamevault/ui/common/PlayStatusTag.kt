package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import isel.dei.pdm.mygamevault.ui.theme.StatusBacklog
import isel.dei.pdm.mygamevault.ui.theme.StatusDropped
import isel.dei.pdm.mygamevault.ui.theme.StatusFinished
import isel.dei.pdm.mygamevault.ui.theme.StatusNone
import isel.dei.pdm.mygamevault.ui.theme.StatusPaused
import isel.dei.pdm.mygamevault.ui.theme.StatusPlatinum
import isel.dei.pdm.mygamevault.ui.theme.StatusPlaying

/**
 * A Composable that displays the playing status as a tag with an icon and text.
 *
 * @param state The play status state to display.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayStatusTag(
    state: PlayStatus.State,
    modifier: Modifier = Modifier
) {
    val (icon, color, labelRes) = when (state) {
        PlayStatus.State.BACKLOG -> Triple(Icons.Default.Inventory2, StatusBacklog, R.string.status_backlog)
        PlayStatus.State.PLAYING -> Triple(Icons.Default.VideogameAsset, StatusPlaying, R.string.status_playing)
        PlayStatus.State.FINISHED -> Triple(Icons.Default.CheckCircle, StatusFinished, R.string.status_finished)
        PlayStatus.State.PLATINUM -> Triple(Icons.Default.EmojiEvents, StatusPlatinum, R.string.status_platinum)
        PlayStatus.State.PAUSED -> Triple(Icons.Default.PauseCircle, StatusPaused, R.string.status_paused)
        PlayStatus.State.DROPPED -> Triple(Icons.Default.Cancel, StatusDropped, R.string.status_dropped)
        PlayStatus.State.NONE -> Triple(Icons.AutoMirrored.Filled.HelpOutline, StatusNone, R.string.status_none)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlayStatusTagPreview() {
    MyGameVaultTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            PlayStatusTag(PlayStatus.State.PLAYING)
            Spacer(modifier = Modifier.width(8.dp))
            PlayStatusTag(PlayStatus.State.FINISHED)
            Spacer(modifier = Modifier.width(8.dp))
            PlayStatusTag(PlayStatus.State.PLATINUM)
        }
    }
}
