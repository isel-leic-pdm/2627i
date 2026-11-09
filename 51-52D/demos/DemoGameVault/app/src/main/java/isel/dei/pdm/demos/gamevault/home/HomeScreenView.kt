package isel.dei.pdm.demos.gamevault.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.GameCarousel
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.time.LocalDate

/**
 * The view used to display the home screen contents: the games being played, the collection
 * shortcuts, and the summary of the user's accomplishments.
 *
 * DRAFT: Meant to visualize the screen's layout. Its inputs will be replaced by the screen's state.
 *
 * @param playing The items of the collection entries currently being played.
 * @param stats The summary of the user's accomplishments.
 * @param onGameSelected Callback invoked when the user selects a game being played.
 * @param onFilterSelected Callback invoked when the user selects a filter, to browse the
 * collection filtered by it.
 * @param onBrowseAll Callback invoked when the user wants to browse the whole collection.
 * @param onAddGame Callback invoked when the user wants to add a game to the collection.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun HomeScreenView(
    playing: List<CollectionItem>,
    stats: HomeStats,
    onGameSelected: (CollectionItem) -> Unit,
    onFilterSelected: (CollectionFilter) -> Unit,
    onBrowseAll: () -> Unit,
    onAddGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Section(title = stringResource(R.string.home_continue_playing_title)) {
            ContinuePlaying(
                playing = playing,
                onGameSelected = onGameSelected
            )
        }
        Section(title = stringResource(R.string.home_collection_title)) {
            CollectionShortcuts(
                onFilterSelected = onFilterSelected,
                onBrowseAll = onBrowseAll,
                onAddGame = onAddGame
            )
        }
        Section(title = stringResource(R.string.home_summary_title)) {
            StatsSummary(stats = stats)
        }
    }
}

/**
 * A titled section of the home screen.
 */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        content()
    }
}

/**
 * The carousel of games being played, or a message if there are none.
 */
@Composable
private fun ContinuePlaying(
    playing: List<CollectionItem>,
    onGameSelected: (CollectionItem) -> Unit
) {
    if (playing.isEmpty()) {
        Text(
            text = stringResource(R.string.home_continue_playing_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    } else {
        GameCarousel(items = playing, onItemSelected = onGameSelected)
    }
}

/**
 * The number of filter chips in each row of the collection shortcuts grid.
 */
private const val SHORTCUTS_PER_ROW = 2

/**
 * The collection shortcuts: a grid of equally sized chips, one per filter, and shortcuts to add
 * a game and to browse the whole collection. The number of entries matching each filter is
 * deliberately not displayed: the summary already provides the relevant figures, and some of them
 * would differ from those of the filters (e.g. games in the FINISHED state versus games finished
 * at least once).
 */
@Composable
private fun CollectionShortcuts(
    onFilterSelected: (CollectionFilter) -> Unit,
    onBrowseAll: () -> Unit,
    onAddGame: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        CollectionFilter.shortcuts.chunked(SHORTCUTS_PER_ROW).forEach { rowFilters ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowFilters.forEach { filter ->
                    CollectionFilterChip(
                        filter = filter,
                        onSelected = { onFilterSelected(filter) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // A leading icon signals an action, and a trailing arrow signals navigation
            TextButton(
                onClick = onAddGame,
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Text(
                    text = stringResource(R.string.home_add_game_label),
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                )
            }
            TextButton(onClick = onBrowseAll) {
                Text(
                    text = stringResource(R.string.home_browse_all_label),
                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}

/**
 * A chip that selects the given collection [filter].
 */
@Composable
private fun CollectionFilterChip(
    filter: CollectionFilter,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onSelected,
        label = { Text(stringResource(filter.labelResId())) },
        icon = {
            // The label describes the filter, so the icon is decorative
            Icon(
                imageVector = filter.icon(),
                contentDescription = null,
                tint = filter.iconColor(),
                modifier = Modifier.size(AssistChipDefaults.IconSize)
            )
        },
        modifier = modifier
    )
}

/**
 * The summary of the user's accomplishments: those of the current year, followed by the all-time
 * figures.
 */
@Composable
private fun StatsSummary(stats: HomeStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatsGroup(
                title = stringResource(R.string.home_summary_this_year),
                stats = listOf(
                    stats.finishedThisYear to stringResource(R.string.home_stat_finished),
                    stats.replayedThisYear to stringResource(R.string.home_stat_replayed),
                    stats.platinumThisYear to stringResource(R.string.home_stat_platinum)
                )
            )
            HorizontalDivider()
            StatsGroup(
                title = stringResource(R.string.home_summary_all_time),
                stats = listOf(
                    stats.owned to stringResource(R.string.home_stat_owned),
                    stats.played to stringResource(R.string.home_stat_played),
                    stats.finished to stringResource(R.string.home_stat_finished),
                    stats.platinum to stringResource(R.string.home_stat_platinum)
                )
            )
        }
    }
}

/**
 * A titled group of figures, evenly spread in a row.
 */
@Composable
private fun StatsGroup(title: String, stats: List<Pair<Int, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stats.forEach { (value, label) -> Stat(value = "$value", label = label) }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * The icon displayed for each filter.
 */
private fun CollectionFilter.icon(): ImageVector =
    when (this) {
        is CollectionFilter.InState -> when (state) {
            PlayStatus.State.PLAYING -> Icons.Filled.PlayArrow
            PlayStatus.State.BACKLOG -> Icons.Filled.Bookmark
            PlayStatus.State.PAUSED -> Icons.Filled.Pause
            PlayStatus.State.FINISHED -> Icons.Filled.Flag
            PlayStatus.State.DROPPED -> Icons.Filled.Block
            // Not offered as a shortcut
            PlayStatus.State.NONE -> Icons.Filled.Bookmark
        }
        CollectionFilter.WithPlatinum -> Icons.Filled.EmojiEvents
    }

/**
 * The color of the icon displayed for each filter. Colors are taken from the theme's color roles,
 * so that they adapt to the dynamic color scheme and to the dark theme: only the filters that are
 * meant to stand out are highlighted.
 */
@Composable
private fun CollectionFilter.iconColor(): Color =
    when (this) {
        CollectionFilter.InState(PlayStatus.State.PLAYING) -> MaterialTheme.colorScheme.primary
        CollectionFilter.WithPlatinum -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

/**
 * The label displayed to the user for each filter.
 */
@StringRes
private fun CollectionFilter.labelResId(): Int =
    when (this) {
        is CollectionFilter.InState -> state.labelResId()
        // TODO: The term used for platinum (e.g. Platinum, All achievements) is to become a user
        //  preference, as it is platform specific
        CollectionFilter.WithPlatinum -> R.string.accomplishment_platinum
    }

/**
 * The label displayed to the user for each play status.
 */
@StringRes
private fun PlayStatus.State.labelResId(): Int =
    when (this) {
        PlayStatus.State.NONE -> R.string.play_status_none
        PlayStatus.State.BACKLOG -> R.string.play_status_backlog
        PlayStatus.State.PLAYING -> R.string.play_status_playing
        PlayStatus.State.PAUSED -> R.string.play_status_paused
        PlayStatus.State.FINISHED -> R.string.play_status_finished
        PlayStatus.State.DROPPED -> R.string.play_status_dropped
    }

// Testing & preview utilities

/**
 * PLACEHOLDER: Sample data used to visualize the home screen, while its state is not defined.
 */
object HomeScreenSampleData {
    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")

    private fun playingEntry(id: Long, name: String, platform: Platform, hours: Int, minutes: Int) =
        CollectionEntry(
            game = Game(id, name, LocalDate.of(2023, 1, 1), coverUri = null),
            platform = platform,
            playStatus = PlayStatus(
                timeSpent = PlayTime(hours, minutes),
                state = PlayStatus.State.PLAYING
            )
        ).toItem()

    val playing = listOf(
        playingEntry(1L, "Elden Ring", ps5, 42, 10),
        playingEntry(2L, "The Legend of Zelda: Tears of the Kingdom", switch, 87, 45),
        playingEntry(3L, "Hades II", ps5, 12, 5)
    )

    val stats = HomeStats(
        finishedThisYear = 6,
        replayedThisYear = 1,
        platinumThisYear = 2,
        owned = 73,
        played = 32,
        finished = 23,
        platinum = 5
    )
}

@Suppress("Unused")
class HomeScreenViewPreviews {

    @Composable
    private fun PreviewOf(playing: List<CollectionItem>) {
        DemoGameVaultTheme {
            HomeScreenView(
                playing = playing,
                stats = HomeScreenSampleData.stats,
                onGameSelected = {},
                onFilterSelected = {},
                onBrowseAll = {},
                onAddGame = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Playing games")
    @Composable
    fun PlayingPreview() = PreviewOf(HomeScreenSampleData.playing)

    @Preview(showBackground = true, name = "Nothing being played")
    @Composable
    fun NothingPlayingPreview() = PreviewOf(emptyList())
}
