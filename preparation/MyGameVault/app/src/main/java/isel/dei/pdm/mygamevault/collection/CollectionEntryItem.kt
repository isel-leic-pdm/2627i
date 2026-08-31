package isel.dei.pdm.mygamevault.collection

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ui.common.AsyncImage
import isel.dei.pdm.mygamevault.ui.common.PlayStatusTag
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

/**
 * A Composable that displays a single game entry in the user's collection.
 *
 * @param entry The collection entry to display.
 * @param onClick The callback to be invoked when the item is clicked.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun CollectionEntryItem(
    entry: CollectionEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            .semantics(mergeDescendants = true) {}
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.game.thumbnailUri ?: entry.game.coverUri,
                contentDescription = stringResource(R.string.game_cover_description, entry.game.name()),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = entry.game.name(),
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = 20.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.size(2.dp))

                Text(
                    text = entry.platform.name(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayStatusTag(state = entry.playStatus.state)
                    val hours = entry.playStatus.timeSpent.inWholeHours
                    if (hours > 0) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = stringResource(R.string.hours_spent_label, hours),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, name = "Playing State")
@Composable
fun CollectionEntryItemPlayingPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(
            state = PlayStatus.State.PLAYING,
            timeSpent = 45.hours
        ),
        addedAt = LocalDate.now()
    )
    MyGameVaultTheme {
        CollectionEntryItem(
            entry = sampleEntry,
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Finished State")
@Composable
fun CollectionEntryItemFinishedPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(2, "The Legend of Zelda: Tears of the Kingdom", LocalDate.of(2023, 5, 12), "cache://totk", null),
        platform = Platforms.SWITCH,
        playStatus = PlayStatus(
            state = PlayStatus.State.FINISHED,
            timeSpent = 120.hours
        ),
        addedAt = LocalDate.of(2023, 6, 1)
    )
    MyGameVaultTheme {
        CollectionEntryItem(
            entry = sampleEntry,
            onClick = {}
        )
    }
}
