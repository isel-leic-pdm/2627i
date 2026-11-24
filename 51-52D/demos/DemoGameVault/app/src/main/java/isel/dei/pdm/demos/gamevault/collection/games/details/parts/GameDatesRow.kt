package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.ui.common.TextPlaceholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate

/**
 * The dates of a game, as displayed on its details screen, below a divider: its release year and
 * the date it was added to the collection, side by side.
 *
 * Only the release year is displayed, as it is the one that matters to the user (e.g. it
 * distinguishes games with the same name). It is omitted if unknown, instead of being displayed
 * with a placeholder.
 *
 * @param releaseYear The game's release year, or null if unknown.
 * @param addedOn The date the game was added to the collection, in the user's time zone. The
 * conversion from the instant it was added at is up to the caller.
 * @param modifier Modifier to be applied to the row's layout.
 */
@Composable
fun GameDatesRow(
    releaseYear: Year?,
    addedOn: LocalDate,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (releaseYear != null) {
                DateItem(
                    icon = Icons.Filled.CalendarToday,
                    text = stringResource(R.string.game_details_released, releaseYear.toString()),
                    tag = GameDatesRowTestTags.RELEASED
                )
            }
            DateItem(
                icon = Icons.Filled.Event,
                text = stringResource(R.string.game_details_added_on, addedOn.toString()),
                tag = GameDatesRowTestTags.ADDED_ON
            )
        }
    }
}

/**
 * The dates row while the game is being read: the divider, above placeholders instead of the
 * dates.
 *
 * @param modifier Modifier to be applied to the row's layout.
 */
@Composable
fun GameDatesRowPlaceholder(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        HorizontalDivider()
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextPlaceholder(style = MaterialTheme.typography.bodyMedium, width = 120.dp)
            TextPlaceholder(style = MaterialTheme.typography.bodyMedium, width = 160.dp)
        }
    }
}

/**
 * One of the dates of the row: a decorative [icon], followed by the [text] that describes it.
 */
@Composable
private fun DateItem(icon: ImageVector, text: String, tag: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(tag)
        )
    }
}

// Testing & preview utilities

object GameDatesRowTestTags {
    const val RELEASED = "GameDatesRowReleased"
    const val ADDED_ON = "GameDatesRowAddedOn"
}

@Suppress("Unused")
class GameDatesRowPreviews {

    @Preview(showBackground = true, name = "Known release year")
    @Composable
    fun KnownReleaseYearPreview() {
        DemoGameVaultTheme {
            GameDatesRow(
                releaseYear = Year(2022),
                addedOn = LocalDate(2026, 11, 18),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Unknown release year")
    @Composable
    fun UnknownReleaseYearPreview() {
        DemoGameVaultTheme {
            GameDatesRow(
                releaseYear = null,
                addedOn = LocalDate(2026, 11, 18),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
