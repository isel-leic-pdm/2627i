package isel.dei.pdm.demos.gamevault.collection.games

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImage
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate

/**
 * Composable used to display a collection item inside a list of games (e.g. when browsing the
 * collection). Displays the game's thumbnail, name, platform, play state and time spent, and
 * navigates to the game's details when selected.
 *
 * Only meaningful information is displayed: the platinum, when obtained, prevails over the play
 * state, which is omitted for games that were not categorized yet ([PlayStatus.State.NONE]); the
 * time spent is omitted for games that were never played.
 *
 * @param item The collection item to be displayed.
 * @param onSelected Callback to be invoked when the item is selected.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun CollectionItemView(
    item: CollectionItem,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .testTag(CollectionItemViewTestTags.ITEM),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                uri = item.thumbnailUri,
                contentDescription = item.name.value,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(CollectionItemViewTestTags.THUMBNAIL),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name.value,
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = MaterialTheme.typography.titleSmall.lineHeight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(CollectionItemViewTestTags.NAME),
                )
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = item.platformAbbreviations.joinToString(", ") { it.value },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(CollectionItemViewTestTags.PLATFORM),
                )
                Spacer(modifier = Modifier.size(4.dp))
                val showState = item.state != PlayStatus.State.NONE
                val showTimeSpent = item.timeSpent != PlayTime()
                if (item.hasPlatinum || showState || showTimeSpent) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        // The platinum is the ultimate accomplishment, so it prevails over the state
                        when {
                            item.hasPlatinum -> PlatinumChip()
                            showState -> PlayStateChip(
                                state = item.state
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (showTimeSpent) {
                            Text(
                                text = item.timeSpent.toDisplayText(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .testTag(CollectionItemViewTestTags.PLAY_TIME),
                            )
                        }
                    }
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * The time spent as displayed in the item: whole hours, unless it is less than an hour, in which
 * case the minutes are displayed instead.
 */
@Composable
private fun PlayTime.toDisplayText(): String =
    if (hours > 0) stringResource(R.string.collection_item_play_time, hours)
    else stringResource(R.string.collection_item_play_time_minutes, minutes)

// Testing & preview utilities

object CollectionItemViewTestTags {
    const val ITEM = "CollectionItem"
    const val THUMBNAIL = "CollectionItemThumbnail"
    const val NAME = "CollectionItemName"
    const val PLATFORM = "CollectionItemPlatform"
    const val PLAY_TIME = "CollectionItemPlayTime"
}

@Suppress("Unused")
class CollectionItemViewPreviews {

    private val zelda = CollectionEntry(
        game = Game(1L, "The Legend of Zelda: Tears of the Kingdom", LocalDate(2023, 5, 12), coverUri = null),
        platforms = setOf(Platform(2L, "Switch", "Nintendo Switch")),
        playStatus = PlayStatus(
            timeSpent = PlayTime(120, 15),
            state = PlayStatus.State.FINISHED,
            completedRuns = listOf(Accomplishment(LocalDate(2023, 8, 20)))
        )
    ).toItem()

    private val eldenRing = CollectionEntry(
        game = Game(2L, "Elden Ring", LocalDate(2022, 2, 25), coverUri = null),
        platforms = setOf(Platform(1L, "PS5", "PlayStation 5")),
        playStatus = PlayStatus(PlayTime(45, 30), PlayStatus.State.PLAYING)
    ).toItem()

    private val hades = CollectionEntry(
        game = Game(3L, "Hades II", LocalDate(2025, 9, 25), coverUri = null),
        // Owned on several platforms
        platforms = setOf(Platform(2L, "Switch", "Nintendo Switch"), Platform(3L, "PC", "PC")),
        playStatus = PlayStatus(state = PlayStatus.State.BACKLOG)
    ).toItem()

    private val celeste = CollectionEntry(
        game = Game(4L, "Celeste", LocalDate(2018, 1, 25), coverUri = null),
        platforms = setOf(Platform(2L, "Switch", "Nintendo Switch")),
        playStatus = PlayStatus()
    ).toItem()

    @Preview(showBackground = true, name = "Finished")
    @Composable
    fun FinishedPreview() {
        DemoGameVaultTheme {
            CollectionItemView(item = zelda, onSelected = {}, modifier = Modifier.padding(16.dp))
        }
    }

    @Preview(showBackground = true, name = "Finished with platinum")
    @Composable
    fun FinishedWithPlatinumPreview() {
        DemoGameVaultTheme {
            CollectionItemView(
                item = zelda.copy(hasPlatinum = true),
                onSelected = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Playing")
    @Composable
    fun PlayingPreview() {
        DemoGameVaultTheme {
            CollectionItemView(item = eldenRing, onSelected = {}, modifier = Modifier.padding(16.dp))
        }
    }

    @Preview(showBackground = true, name = "Playing, less than an hour")
    @Composable
    fun PlayingLessThanAnHourPreview() {
        DemoGameVaultTheme {
            CollectionItemView(
                item = eldenRing.copy(timeSpent = PlayTime(minutes = 35)),
                onSelected = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Backlog, never played")
    @Composable
    fun BacklogPreview() {
        DemoGameVaultTheme {
            CollectionItemView(item = hades, onSelected = {}, modifier = Modifier.padding(16.dp))
        }
    }

    @Preview(showBackground = true, name = "Not categorized")
    @Composable
    fun NotCategorizedPreview() {
        DemoGameVaultTheme {
            CollectionItemView(item = celeste, onSelected = {}, modifier = Modifier.padding(16.dp))
        }
    }
}
