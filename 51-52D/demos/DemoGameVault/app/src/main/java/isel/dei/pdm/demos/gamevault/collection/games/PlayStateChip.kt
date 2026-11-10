package isel.dei.pdm.demos.gamevault.collection.games

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * A small pill displaying a game's play state, with an icon and a label, both tinted with a color
 * that identifies the state.
 *
 * @param state The play state to be displayed.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlayStateChip(state: PlayStatus.State, modifier: Modifier = Modifier) {
    StatusChip(
        icon = state.icon(),
        label = stringResource(state.labelResId()),
        color = state.color(),
        modifier = modifier.testTag(PlayStateChipTestTags.CHIP)
    )
}

/**
 * A small pill signaling that the game's platinum was obtained. It is styled like [PlayStateChip],
 * so that both can be displayed side by side.
 *
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlatinumChip(modifier: Modifier = Modifier) {
    StatusChip(
        icon = PlatinumIcon,
        label = stringResource(R.string.accomplishment_platinum),
        color = platinumColor(),
        modifier = modifier.testTag(PlayStateChipTestTags.PLATINUM_CHIP)
    )
}

/**
 * The pill shared by the status chips: an icon and a label, both tinted with the given [color],
 * on top of a translucent version of it.
 */
@Composable
private fun StatusChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    iconSize: Dp = 16.dp,
    labelStyle: TextStyle = MaterialTheme.typography.labelSmall,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = color.copy(alpha = 0.12f),
        contentColor = color,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = label,
                style = labelStyle,
                maxLines = 1,
            )
        }
    }
}

/**
 * The icon used to signal the platinum.
 */
val PlatinumIcon: ImageVector
    get() = Icons.Filled.EmojiEvents

/**
 * The color used to signal the platinum. Taken from the theme's color roles, like the play
 * states' colors (see [color]).
 */
@Composable
fun platinumColor(): Color = MaterialTheme.colorScheme.tertiary

/**
 * The label displayed to the user for each play state.
 */
@StringRes
fun PlayStatus.State.labelResId(): Int =
    when (this) {
        PlayStatus.State.NONE -> R.string.play_status_none
        PlayStatus.State.BACKLOG -> R.string.play_status_backlog
        PlayStatus.State.PLAYING -> R.string.play_status_playing
        PlayStatus.State.PAUSED -> R.string.play_status_paused
        PlayStatus.State.FINISHED -> R.string.play_status_finished
        PlayStatus.State.DROPPED -> R.string.play_status_dropped
    }

/**
 * The icon displayed for each play state.
 */
fun PlayStatus.State.icon(): ImageVector =
    when (this) {
        PlayStatus.State.NONE -> Icons.Filled.Inventory2
        PlayStatus.State.BACKLOG -> Icons.Filled.Bookmark
        PlayStatus.State.PLAYING -> Icons.Filled.PlayArrow
        PlayStatus.State.PAUSED -> Icons.Filled.Pause
        PlayStatus.State.FINISHED -> Icons.Filled.Flag
        PlayStatus.State.DROPPED -> Icons.Filled.Block
    }

/**
 * The color that identifies each play state. Colors are taken from the theme's color roles, so
 * that they adapt to the dynamic color scheme and to the dark theme: only the states that are
 * meant to stand out are highlighted.
 */
@Composable
fun PlayStatus.State.color(): Color =
    when (this) {
        PlayStatus.State.PLAYING -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

// Testing & preview utilities

object PlayStateChipTestTags {
    const val CHIP = "PlayStateChip"
    const val PLATINUM_CHIP = "PlatinumChip"
}

@Suppress("Unused")
class PlayStateChipPreviews {

    @Preview(showBackground = true, name = "All states and platinum")
    @Composable
    fun AllStatesPreview() {
        DemoGameVaultTheme {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                PlayStatus.State.entries.forEach { PlayStateChip(state = it) }
                PlatinumChip()
            }
        }
    }
}
