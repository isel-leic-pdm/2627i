package isel.dei.pdm.demos.gamevault.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.collection.games.color
import isel.dei.pdm.demos.gamevault.collection.games.icon
import isel.dei.pdm.demos.gamevault.collection.games.PlatinumIcon
import isel.dei.pdm.demos.gamevault.collection.games.labelResId
import isel.dei.pdm.demos.gamevault.collection.games.platinumColor
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.time.LocalDate
import java.time.Year

/**
 * The view used to display the home screen contents: the games being played, the collection
 * shortcuts, and the summary of the user's accomplishments. The games being played and the summary
 * are displayed according to their own states, so that each one can be loading or have failed
 * independently of the other.
 *
 * @param nowPlaying The state of the games being played.
 * @param summary The state of the summary of the user's accomplishments.
 * @param onRetryNowPlaying Callback invoked when the user retries loading the games being played.
 * @param onRetrySummary Callback invoked when the user retries computing the summary.
 * @param onGameSelected Callback invoked when the user selects a game being played.
 * @param onFilterSelected Callback invoked when the user selects a filter, to browse the
 * collection filtered by it.
 * @param onBrowseAll Callback invoked when the user wants to browse the whole collection.
 * @param onAddGame Callback invoked when the user wants to add a game to the collection.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun HomeScreenView(
    nowPlaying: NowPlayingState,
    summary: SummaryState,
    onRetryNowPlaying: () -> Unit,
    onRetrySummary: () -> Unit,
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
        Section(
            title = stringResource(R.string.home_continue_playing_title),
            modifier = Modifier.testTag(HomeScreenViewTestTags.NOW_PLAYING_SECTION)
        ) {
            // The region takes the carousel's height in all states, so that the sections below
            // it do not shift when its state changes (e.g. when loading completes)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DEFAULT_CAROUSEL_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                when (nowPlaying) {
                    NowPlayingState.Loading -> RegionLoading()
                    is NowPlayingState.Loaded -> ContinuePlaying(
                        playing = nowPlaying.items,
                        onGameSelected = onGameSelected,
                        onPickFromBacklog = {
                            onFilterSelected(CollectionFilter.InState(PlayStatus.State.BACKLOG))
                        }
                    )
                    is NowPlayingState.Error -> RegionErrorView(onRetry = onRetryNowPlaying)
                }
            }
        }
        Section(
            title = stringResource(R.string.home_collection_title),
            modifier = Modifier.testTag(HomeScreenViewTestTags.COLLECTION_SECTION)
        ) {
            CollectionShortcuts(
                onFilterSelected = onFilterSelected,
                onBrowseAll = onBrowseAll,
                onAddGame = onAddGame
            )
        }
        Section(
            title = stringResource(R.string.home_summary_title),
            modifier = Modifier.testTag(HomeScreenViewTestTags.SUMMARY_SECTION)
        ) {
            when (summary) {
                SummaryState.Loading -> RegionLoading()
                is SummaryState.Loaded -> StatsSummary(summary = summary.summary)
                is SummaryState.Error -> RegionErrorView(onRetry = onRetrySummary)
            }
        }
    }
}

/**
 * A titled section of the home screen.
 */
@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        content()
    }
}

/**
 * The contents of a region of the screen while it is loading.
 */
@Composable
private fun RegionLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * The carousel of games being played or, if there are none, a prompt to pick one from the backlog.
 */
@Composable
private fun ContinuePlaying(
    playing: List<CollectionItem>,
    onGameSelected: (CollectionItem) -> Unit,
    onPickFromBacklog: () -> Unit
) {
    if (playing.isEmpty()) {
        PickFromBacklogPrompt(onPickFromBacklog = onPickFromBacklog)
    } else {
        GameCarousel(items = playing, onItemSelected = onGameSelected)
    }
}

/**
 * The prompt displayed when no game is being played, inviting the user to pick one from the
 * backlog, with a shortcut to browse it.
 */
@Composable
private fun PickFromBacklogPrompt(onPickFromBacklog: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.SportsEsports,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = stringResource(R.string.home_continue_playing_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
            onClick = onPickFromBacklog,
            contentPadding = ButtonDefaults.ButtonWithIconContentPadding
        ) {
            Icon(
                imageVector = PlayStatus.State.BACKLOG.icon(),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Text(
                text = stringResource(R.string.home_pick_from_backlog_label),
                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
            )
        }
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
private fun StatsSummary(summary: CollectionSummary) {
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
                    summary.finishedInYear to stringResource(R.string.home_stat_finished),
                    summary.replayedInYear to stringResource(R.string.home_stat_replayed),
                    summary.platinumInYear to stringResource(R.string.home_stat_platinum)
                )
            )
            HorizontalDivider()
            StatsGroup(
                title = stringResource(R.string.home_summary_all_time),
                stats = listOf(
                    summary.owned to stringResource(R.string.home_stat_owned),
                    summary.played to stringResource(R.string.home_stat_played),
                    summary.finished to stringResource(R.string.home_stat_finished),
                    summary.platinum to stringResource(R.string.home_stat_platinum)
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
        is CollectionFilter.InState -> state.icon()
        CollectionFilter.WithPlatinum -> PlatinumIcon
    }

/**
 * The color of the icon displayed for each filter. Colors are taken from the theme's color roles,
 * so that they adapt to the dynamic color scheme and to the dark theme: only the filters that are
 * meant to stand out are highlighted.
 */
@Composable
private fun CollectionFilter.iconColor(): Color =
    when (this) {
        is CollectionFilter.InState -> state.color()
        CollectionFilter.WithPlatinum -> platinumColor()
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

// Testing & preview utilities

object HomeScreenViewTestTags {
    const val NOW_PLAYING_SECTION = "HomeNowPlayingSection"
    const val COLLECTION_SECTION = "HomeCollectionSection"
    const val SUMMARY_SECTION = "HomeSummarySection"
}

/**
 * Sample data used to visualize the home screen.
 */
object HomeScreenSampleData {
    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")

    private fun playingEntry(id: Long, name: String, platform: Platform, hours: Int, minutes: Int) =
        CollectionEntry(
            game = Game(id, name, LocalDate.of(2023, 1, 1), coverUri = null),
            platforms = setOf(platform),
            playStatus = PlayStatus(
                timeSpent = PlayTime(hours, minutes),
                state = PlayStatus.State.PLAYING
            )
        )

    val entries = listOf(
        playingEntry(1L, "Elden Ring", ps5, 42, 10),
        playingEntry(2L, "The Legend of Zelda: Tears of the Kingdom", switch, 87, 45),
        playingEntry(3L, "Hades II", ps5, 12, 5)
    )

    val playing = entries.map { it.toItem() }

    val summary = CollectionSummary(
        year = Year.of(2026),
        finishedInYear = 6,
        replayedInYear = 1,
        platinumInYear = 2,
        owned = 73,
        played = 32,
        finished = 23,
        platinum = 5
    )
}

@Suppress("Unused")
class HomeScreenViewPreviews {

    @Composable
    private fun PreviewOf(
        nowPlaying: NowPlayingState,
        summary: SummaryState = SummaryState.Loaded(HomeScreenSampleData.summary)
    ) {
        DemoGameVaultTheme {
            HomeScreenView(
                nowPlaying = nowPlaying,
                summary = summary,
                onRetryNowPlaying = {},
                onRetrySummary = {},
                onGameSelected = {},
                onFilterSelected = {},
                onBrowseAll = {},
                onAddGame = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Playing games")
    @Composable
    fun PlayingPreview() =
        PreviewOf(NowPlayingState.Loaded(HomeScreenSampleData.playing, hasMore = false))

    @Preview(showBackground = true, name = "Nothing being played")
    @Composable
    fun NothingPlayingPreview() = PreviewOf(NowPlayingState.Loaded(emptyList(), hasMore = false))

    @Preview(showBackground = true, name = "Loading")
    @Composable
    fun LoadingPreview() = PreviewOf(NowPlayingState.Loading, SummaryState.Loading)

    @Preview(showBackground = true, name = "Summary error")
    @Composable
    fun SummaryErrorPreview() = PreviewOf(
        nowPlaying = NowPlayingState.Loaded(HomeScreenSampleData.playing, hasMore = false),
        summary = SummaryState.Error(RecoverablePersistenceException())
    )

    @Preview(showBackground = true, name = "Now playing error")
    @Composable
    fun NowPlayingErrorPreview() = PreviewOf(
        nowPlaying = NowPlayingState.Error(RecoverablePersistenceException())
    )
}
