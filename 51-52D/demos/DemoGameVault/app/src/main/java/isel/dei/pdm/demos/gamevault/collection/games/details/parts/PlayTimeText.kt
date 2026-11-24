package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime

/**
 * The play time as displayed on the game details screen: hours, followed by the minutes, if there
 * are any, or just the minutes, if less than an hour.
 */
@Composable
internal fun PlayTime.toDisplayText(): String =
    when {
        hours == 0 -> stringResource(R.string.game_details_play_time_minutes, minutes)
        minutes == 0 -> stringResource(R.string.game_details_play_time_hours, hours)
        else -> stringResource(R.string.game_details_play_time_hours_and_minutes, hours, minutes)
    }
