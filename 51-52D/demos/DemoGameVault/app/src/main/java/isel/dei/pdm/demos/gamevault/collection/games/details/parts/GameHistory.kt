package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import isel.dei.pdm.demos.gamevault.collection.games.PlatinumIcon
import isel.dei.pdm.demos.gamevault.collection.games.color
import isel.dei.pdm.demos.gamevault.collection.games.icon
import isel.dei.pdm.demos.gamevault.collection.games.platinumColor
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.ui.common.TextPlaceholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate

/**
 * The history of a game, as displayed on its details screen, below a divider: its completed runs
 * and its platinum, if obtained, with their dates, in chronological order.
 *
 * The entries are ordered as in [Accomplishment.chronologically], the undated ones first, and the
 * runs are numbered in that order: "Run 1" is the first time the game was finished. The platinum
 * follows the runs completed on the same date, as obtaining it implies a completed run.
 *
 * Undated entries are displayed as such, instead of being omitted, as they still count. Without
 * completed runs, a placeholder is displayed instead.
 *
 * @param completedRuns The game's completed runs, in any order.
 * @param platinum The game's platinum, or null if not obtained.
 * @param modifier Modifier to be applied to the section's layout.
 */
@Composable
fun GameHistory(
    completedRuns: List<Accomplishment>,
    platinum: Accomplishment?,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        HistoryTitle()
        val entries = historyEntries(completedRuns, platinum)
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.game_details_history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(GameHistoryTestTags.EMPTY)
            )
        } else {
            entries.forEach { HistoryEntryRow(it) }
        }
    }
}

/**
 * The history while the game is being read: the divider and the title, above a placeholder
 * instead of the entries, as their number is not known yet.
 *
 * @param modifier Modifier to be applied to the section's layout.
 */
@Composable
fun GameHistoryPlaceholder(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        HistoryTitle()
        TextPlaceholder(style = MaterialTheme.typography.bodyLarge, width = 200.dp)
    }
}

@Composable
private fun HistoryTitle() {
    Text(
        text = stringResource(R.string.game_details_history_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * An entry of the history: a completed run, identified by its [number], or the platinum.
 */
private sealed interface HistoryEntry {
    val accomplishment: Accomplishment

    data class Run(val number: Int, override val accomplishment: Accomplishment) : HistoryEntry
    data class Platinum(override val accomplishment: Accomplishment) : HistoryEntry
}

/**
 * The entries of the history, in chronological order (see [GameHistory]). As the sort is stable,
 * appending the platinum after the runs places it after the runs completed on the same date.
 */
private fun historyEntries(
    completedRuns: List<Accomplishment>,
    platinum: Accomplishment?
): List<HistoryEntry> {
    val runs = completedRuns
        .sortedWith(Accomplishment.chronologically)
        .mapIndexed { index, run -> HistoryEntry.Run(number = index + 1, accomplishment = run) }
    val all = if (platinum != null) runs + HistoryEntry.Platinum(platinum) else runs
    return all.sortedWith(compareBy(Accomplishment.chronologically) { it.accomplishment })
}

/**
 * A row of the history: a decorative icon and the entry's name, followed by its date, at the end.
 */
@Composable
private fun HistoryEntryRow(entry: HistoryEntry) {
    val (icon, tint, name) = when (entry) {
        is HistoryEntry.Run -> Triple(
            PlayStatus.State.FINISHED.icon(),
            PlayStatus.State.FINISHED.color(),
            stringResource(R.string.game_details_history_run, entry.number)
        )
        is HistoryEntry.Platinum -> Triple(
            PlatinumIcon,
            platinumColor(),
            stringResource(R.string.accomplishment_platinum)
        )
    }
    val date = entry.accomplishment.achievedOn
    HistoryRow(
        icon = icon,
        tint = tint,
        name = name,
        date = date?.toString() ?: stringResource(R.string.game_details_history_date_unknown),
        isDateKnown = date != null
    )
}

@Composable
private fun HistoryRow(
    icon: ImageVector,
    tint: Color,
    name: String,
    date: String,
    isDateKnown: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(GameHistoryTestTags.ENTRY)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .testTag(GameHistoryTestTags.ENTRY_NAME)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDateKnown) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(GameHistoryTestTags.ENTRY_DATE)
        )
    }
}

// Testing & preview utilities

object GameHistoryTestTags {
    const val EMPTY = "GameHistoryEmpty"
    const val ENTRY = "GameHistoryEntry"
    const val ENTRY_NAME = "GameHistoryEntryName"
    const val ENTRY_DATE = "GameHistoryEntryDate"
}

@Suppress("Unused")
class GameHistoryPreviews {

    @Preview(showBackground = true, name = "Replayed, with the platinum")
    @Composable
    fun ReplayedWithPlatinumPreview() {
        DemoGameVaultTheme {
            GameHistory(
                completedRuns = listOf(
                    Accomplishment(LocalDate(2025, 8, 2)),
                    Accomplishment(null),
                    Accomplishment(LocalDate(2024, 3, 10))
                ),
                platinum = Accomplishment(LocalDate(2025, 8, 2)),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "No completed runs")
    @Composable
    fun EmptyPreview() {
        DemoGameVaultTheme {
            GameHistory(
                completedRuns = emptyList(),
                platinum = null,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
