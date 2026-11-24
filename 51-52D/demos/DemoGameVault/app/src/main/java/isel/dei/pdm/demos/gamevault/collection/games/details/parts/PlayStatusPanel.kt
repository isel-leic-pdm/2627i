package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.AddPlatinumChip
import isel.dei.pdm.demos.gamevault.collection.games.PlatinumChip
import isel.dei.pdm.demos.gamevault.collection.games.PlayStateChip
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.ui.common.Placeholder
import isel.dei.pdm.demos.gamevault.ui.common.TextPlaceholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.datetime.LocalDate

/**
 * The play status of a game, as displayed on its details screen, one row per aspect, each with
 * the action that changes it:
 * - The play state (see [PlayStateChip]), which can be changed.
 * - The time spent playing, which can be edited.
 * - The number of completed runs, to which a run can be added.
 *
 * Below them, a chip stands for the platinum, and is also its action: [PlatinumChip] once
 * obtained, clicked to edit it (e.g. to correct its date), or [AddPlatinumChip] until then,
 * clicked to register it.
 *
 * The panel only emits the user's intents: how each change is made (e.g. which dialog is
 * displayed) is up to the caller. It also does not enforce the domain rules: the caller decides
 * which actions are available (e.g. while a play session is active, the game cannot leave the
 * PLAYING state, nor be finished for the first time, which also applies to registering the
 * platinum without completed runs; see
 * [isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry]).
 *
 * @param playStatus The play status to be displayed.
 * @param onChangeState Callback invoked when the user asks to change the play state.
 * @param onEditPlayTime Callback invoked when the user asks to edit the time spent playing.
 * @param onAddRun Callback invoked when the user asks to add a completed run.
 * @param onRegisterPlatinum Callback invoked when the user asks to register the platinum, or to
 * edit it, if already registered.
 * @param modifier Modifier to be applied to the panel's layout.
 * @param isStateChangeEnabled Whether the play state can be changed.
 * @param isAddRunEnabled Whether a completed run can be added.
 * @param isRegisterPlatinumEnabled Whether the platinum can be registered (or edited).
 */
@Composable
fun PlayStatusPanel(
    playStatus: PlayStatus,
    onChangeState: () -> Unit,
    onEditPlayTime: () -> Unit,
    onAddRun: () -> Unit,
    onRegisterPlatinum: () -> Unit,
    modifier: Modifier = Modifier,
    isStateChangeEnabled: Boolean = true,
    isAddRunEnabled: Boolean = true,
    isRegisterPlatinumEnabled: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        PlayStatusRow(
            label = stringResource(R.string.game_details_play_status_label),
            actionIcon = Icons.Filled.Edit,
            actionDescription = stringResource(R.string.game_details_play_status_edit_description),
            onAction = onChangeState,
            isActionEnabled = isStateChangeEnabled,
            actionTag = PlayStatusPanelTestTags.CHANGE_STATE_BUTTON
        ) {
            PlayStateChip(state = playStatus.state)
        }
        PlayStatusRow(
            label = stringResource(R.string.game_details_play_time_label),
            actionIcon = Icons.Filled.Edit,
            actionDescription = stringResource(R.string.game_details_play_time_edit_description),
            onAction = onEditPlayTime,
            actionTag = PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON
        ) {
            if (playStatus.timeSpent.isNotRecorded) {
                Text(
                    text = stringResource(R.string.game_details_play_time_not_recorded),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(PlayStatusPanelTestTags.PLAY_TIME)
                )
            } else {
                ValueText(
                    text = playStatus.timeSpent.toDisplayText(),
                    tag = PlayStatusPanelTestTags.PLAY_TIME
                )
            }
        }
        PlayStatusRow(
            label = stringResource(R.string.game_details_completed_runs_label),
            actionIcon = Icons.Filled.Add,
            actionDescription = stringResource(R.string.game_details_completed_runs_add_description),
            onAction = onAddRun,
            isActionEnabled = isAddRunEnabled,
            actionTag = PlayStatusPanelTestTags.ADD_RUN_BUTTON
        ) {
            ValueText(
                text = playStatus.completedRuns.size.toString(),
                tag = PlayStatusPanelTestTags.COMPLETED_RUNS
            )
        }
        if (playStatus.platinum != null) {
            PlatinumChip(
                onClick = onRegisterPlatinum,
                onClickLabel = stringResource(R.string.game_details_platinum_edit_description),
                enabled = isRegisterPlatinumEnabled
            )
        } else {
            AddPlatinumChip(
                onClick = onRegisterPlatinum,
                onClickLabel = stringResource(R.string.game_details_platinum_add_description),
                enabled = isRegisterPlatinumEnabled
            )
        }
    }
}

/**
 * The panel while the game is being read: the rows with their labels and their actions disabled,
 * and placeholders instead of the values and of the platinum chip, so that the layout does not
 * change once they are displayed.
 *
 * @param modifier Modifier to be applied to the panel's layout.
 */
@Composable
fun PlayStatusPanelPlaceholder(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
    ) {
        PlayStatusRow(
            label = stringResource(R.string.game_details_play_status_label),
            actionIcon = Icons.Filled.Edit,
            actionDescription = stringResource(R.string.game_details_play_status_edit_description),
            onAction = { },
            isActionEnabled = false,
            actionTag = PlayStatusPanelTestTags.CHANGE_STATE_BUTTON
        ) {
            ChipPlaceholder(width = 96.dp)
        }
        PlayStatusRow(
            label = stringResource(R.string.game_details_play_time_label),
            actionIcon = Icons.Filled.Edit,
            actionDescription = stringResource(R.string.game_details_play_time_edit_description),
            onAction = { },
            isActionEnabled = false,
            actionTag = PlayStatusPanelTestTags.EDIT_PLAY_TIME_BUTTON
        ) {
            TextPlaceholder(style = MaterialTheme.typography.headlineSmall, width = 96.dp)
        }
        PlayStatusRow(
            label = stringResource(R.string.game_details_completed_runs_label),
            actionIcon = Icons.Filled.Add,
            actionDescription = stringResource(R.string.game_details_completed_runs_add_description),
            onAction = { },
            isActionEnabled = false,
            actionTag = PlayStatusPanelTestTags.ADD_RUN_BUTTON
        ) {
            TextPlaceholder(style = MaterialTheme.typography.headlineSmall, width = 32.dp)
        }
        ChipPlaceholder(width = 104.dp)
    }
}

/**
 * A placeholder with the size and shape of a chip (see [PlayStateChip]): the height of its label,
 * plus its padding.
 */
@Composable
private fun ChipPlaceholder(width: Dp) {
    val labelHeight = with(LocalDensity.current) {
        MaterialTheme.typography.labelSmall.lineHeight.toDp()
    }
    Placeholder(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.size(width = width, height = labelHeight + 8.dp)
    )
}

/**
 * A row of the panel: the aspect's [label], above its value ([content]), and the action that
 * changes it, at the end.
 *
 * The value is given at least the height of a line of [ValueText], so that all rows have the same
 * height, whatever their value (a number, a chip, or a placeholder). The height is derived from the
 * typography, so that it follows the user's font scale.
 */
@Composable
private fun PlayStatusRow(
    label: String,
    actionIcon: ImageVector,
    actionDescription: String,
    onAction: () -> Unit,
    actionTag: String,
    isActionEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val valueMinHeight = with(LocalDensity.current) {
                MaterialTheme.typography.headlineSmall.lineHeight.toDp()
            }
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .heightIn(min = valueMinHeight)
            ) {
                content()
            }
        }
        IconButton(
            onClick = onAction,
            enabled = isActionEnabled,
            modifier = Modifier.testTag(actionTag)
        ) {
            Icon(imageVector = actionIcon, contentDescription = actionDescription)
        }
    }
}

/**
 * The value of a row of the panel, when displayed as text.
 */
@Composable
private fun ValueText(text: String, tag: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.testTag(tag)
    )
}

// Testing & preview utilities

object PlayStatusPanelTestTags {
    const val CHANGE_STATE_BUTTON = "PlayStatusPanelChangeStateButton"
    const val PLAY_TIME = "PlayStatusPanelPlayTime"
    const val EDIT_PLAY_TIME_BUTTON = "PlayStatusPanelEditPlayTimeButton"
    const val COMPLETED_RUNS = "PlayStatusPanelCompletedRuns"
    const val ADD_RUN_BUTTON = "PlayStatusPanelAddRunButton"
}

@Suppress("Unused")
class PlayStatusPanelPreviews {

    @Preview(showBackground = true, name = "Just started playing")
    @Composable
    fun JustStartedPreview() {
        DemoGameVaultTheme {
            PlayStatusPanel(
                playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
                onChangeState = { },
                onEditPlayTime = { },
                onAddRun = { },
                onRegisterPlatinum = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Finished twice, with the platinum")
    @Composable
    fun FinishedWithPlatinumPreview() {
        DemoGameVaultTheme {
            PlayStatusPanel(
                playStatus = PlayStatus(
                    timeSpent = PlayTime(hours = 132, minutes = 45),
                    state = PlayStatus.State.FINISHED,
                    completedRuns = listOf(
                        Accomplishment(LocalDate(2024, 3, 10)),
                        Accomplishment(LocalDate(2025, 8, 2))
                    ),
                    platinum = Accomplishment(LocalDate(2025, 8, 2))
                ),
                onChangeState = { },
                onEditPlayTime = { },
                onAddRun = { },
                onRegisterPlatinum = { },
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "During a session of a game not finished yet")
    @Composable
    fun DuringSessionPreview() {
        DemoGameVaultTheme {
            PlayStatusPanel(
                playStatus = PlayStatus(
                    timeSpent = PlayTime(hours = 3),
                    state = PlayStatus.State.PLAYING
                ),
                onChangeState = { },
                onEditPlayTime = { },
                onAddRun = { },
                onRegisterPlatinum = { },
                isStateChangeEnabled = false,
                isAddRunEnabled = false,
                isRegisterPlatinumEnabled = false,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
