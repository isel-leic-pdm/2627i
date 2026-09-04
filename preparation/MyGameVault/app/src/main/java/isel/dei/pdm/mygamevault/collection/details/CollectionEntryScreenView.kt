package isel.dei.pdm.mygamevault.collection.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.PlayTime
import isel.dei.pdm.mygamevault.ui.common.AsyncImage
import isel.dei.pdm.mygamevault.ui.common.PlayStatusTag
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val COLLECTION_ENTRY_SCREEN_TAG = "CollectionEntryScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionEntryScreenView(
    state: CollectionEntryScreenState,
    onBackRequested: () -> Unit,
    onRemoveRequested: () -> Unit,
    onStatusChangeRequested: (PlayStatus.State) -> Unit,
    onStartStopLoggingRequested: () -> Unit,
    onHoursEditRequested: (Long) -> Unit,
    onAddRunRequested: () -> Unit,
    onRecoverableErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val transientErrorHint = stringResource(R.string.error_storage_transient_hint)

    val recoverableErrorMsgId = when (state) {
        is CollectionEntryScreenState.Idle -> state.recoverableErrorMsgId
        is CollectionEntryScreenState.Logging -> state.recoverableErrorMsgId
        else -> null
    }

    val transientErrorMessage = if (recoverableErrorMsgId != null) {
        "${stringResource(recoverableErrorMsgId)} $transientErrorHint"
    } else ""

    LaunchedEffect(recoverableErrorMsgId) {
        if (recoverableErrorMsgId != null) {
            val result = snackbarHostState.showSnackbar(
                message = transientErrorMessage,
                actionLabel = "OK",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                onRecoverableErrorConsumed()
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(COLLECTION_ENTRY_SCREEN_TAG),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.game_details_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBackRequested) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.game_details_back)
                        )
                    }
                },
                actions = {
                    if (state is CollectionEntryScreenState.Idle || state is CollectionEntryScreenState.Logging) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.game_details_remove),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (showDeleteConfirmation) {
            val entry = when (state) {
                is CollectionEntryScreenState.Idle -> state.entry
                is CollectionEntryScreenState.Logging -> state.entry
                else -> null
            }
            if (entry != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text(stringResource(R.string.game_details_remove_title)) },
                    text = { Text(stringResource(R.string.game_details_remove_confirmation)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRemoveRequested()
                                showDeleteConfirmation = false
                            }
                        ) {
                            Text(stringResource(R.string.game_details_remove_confirm))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text(stringResource(R.string.game_details_remove_dismiss))
                        }
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state) {
                is CollectionEntryScreenState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                is CollectionEntryScreenState.Idle -> CollectionEntryContent(
                    entry = state.entry,
                    isLoggingTime = false,
                    onStatusChangeRequested = onStatusChangeRequested,
                    onStartStopLoggingRequested = onStartStopLoggingRequested,
                    onHoursEditRequested = onHoursEditRequested,
                    onAddRunRequested = onAddRunRequested
                )
                is CollectionEntryScreenState.Logging -> CollectionEntryContent(
                    entry = state.entry,
                    isLoggingTime = true,
                    onStatusChangeRequested = onStatusChangeRequested,
                    onStartStopLoggingRequested = onStartStopLoggingRequested,
                    onHoursEditRequested = onHoursEditRequested,
                    onAddRunRequested = onAddRunRequested
                )
                is CollectionEntryScreenState.FatalError -> { }
            }
        }
    }
}

@Composable
private fun CollectionEntryContent(
    entry: CollectionEntry,
    isLoggingTime: Boolean,
    onStatusChangeRequested: (PlayStatus.State) -> Unit,
    onStartStopLoggingRequested: () -> Unit,
    onHoursEditRequested: (Long) -> Unit,
    onAddRunRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Header Section: Title and Platform ---
            Text(
                text = entry.game.name(),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = entry.platform.name(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Main Section: Image and Stats Row ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    uri = entry.game.coverUri ?: entry.game.thumbnailUri,
                    contentDescription = entry.game.name(),
                    modifier = Modifier
                        .width(180.dp)
                        .height(270.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillBounds
                )

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PlayStatusSection(
                        currentStatus = entry.playStatus.state,
                        onStatusChange = onStatusChangeRequested
                    )

                    PlayTimeSection(
                        playTime = entry.playStatus.timeSpent,
                        onHoursEdit = onHoursEditRequested
                    )

                    CompletedRunsSection(
                        completedRuns = entry.playStatus.completedRuns,
                        onAddRun = onAddRunRequested
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // --- Metadata Section: Dates ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally)
            ) {
                entry.game.releaseDate?.let { date ->
                    MetadataItem(
                        icon = Icons.Default.CalendarToday,
                        label = stringResource(R.string.released_label, date.year)
                    )
                }
                MetadataItem(
                    icon = Icons.Default.Event,
                    label = stringResource(R.string.added_on_label, entry.addedAt.toString())
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoggingTime) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.game_details_logging_active),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Button(
            onClick = onStartStopLoggingRequested,
            modifier = Modifier.fillMaxWidth(),
            colors = if (isLoggingTime)
                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            else
                ButtonDefaults.buttonColors()
        ) {
            Icon(
                imageVector = if (isLoggingTime) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isLoggingTime)
                    stringResource(R.string.game_details_stop_playing)
                else
                    stringResource(R.string.game_details_start_playing)
            )
        }
    }
}

@Composable
private fun MetadataItem(
    icon: ImageVector,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompletedRunsSection(
    completedRuns: Int,
    onAddRun: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.game_details_completed_runs_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = completedRuns.toString(),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(
                onClick = onAddRun,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.game_details_add_run),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayStatusSection(
    currentStatus: PlayStatus.State,
    onStatusChange: (PlayStatus.State) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.game_details_play_status_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PlayStatusTag(
                state = currentStatus,
                textStyle = MaterialTheme.typography.bodyMedium,
                iconSize = 20.dp,
                modifier = Modifier.weight(1f, fill = false)
            )
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.game_details_change_status),
                        modifier = Modifier.size(16.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    PlayStatus.State.entries.filter { it != PlayStatus.State.NONE }.forEach { state ->
                        DropdownMenuItem(
                            text = { Text(stringResource(when(state) {
                                PlayStatus.State.BACKLOG -> R.string.status_backlog
                                PlayStatus.State.PLAYING -> R.string.status_playing
                                PlayStatus.State.FINISHED -> R.string.status_finished
                                PlayStatus.State.PLATINUM -> R.string.status_platinum
                                PlayStatus.State.PAUSED -> R.string.status_paused
                                PlayStatus.State.DROPPED -> R.string.status_dropped
                                else -> R.string.status_none
                            })) },
                            onClick = {
                                onStatusChange(state)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayTimeSection(
    playTime: PlayTime,
    onHoursEdit: (Long) -> Unit
) {
    var isEditingHours by remember { mutableStateOf(false) }
    var editedHours by remember { mutableStateOf(playTime.hours.toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.game_details_play_time_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isEditingHours) {
                OutlinedTextField(
                    value = editedHours,
                    onValueChange = { editedHours = it },
                    label = { Text(stringResource(R.string.game_details_hours_label)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { isEditingHours = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.game_details_cancel_edit),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = {
                        editedHours.toLongOrNull()?.let { onHoursEdit(it) }
                        isEditingHours = false
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.game_details_save_hours),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Text(
                    text = if (playTime.minutes > 0)
                        "${playTime.hours}h ${playTime.minutes}m"
                    else
                        stringResource(R.string.hours_spent_label, playTime.hours.toLong()),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { isEditingHours = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.game_details_edit_hours),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionEntryScreenPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        addedAt = LocalDate.now()
    )
    MyGameVaultTheme {
        CollectionEntryScreenView(
            state = CollectionEntryScreenState.Idle(sampleEntry),
            onBackRequested = {},
            onRemoveRequested = {},
            onStatusChangeRequested = {},
            onStartStopLoggingRequested = {},
            onHoursEditRequested = {},
            onAddRunRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionEntryScreenLoggingPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        addedAt = LocalDate.now()
    )
    MyGameVaultTheme {
        CollectionEntryScreenView(
            state = CollectionEntryScreenState.Logging(
                entry = sampleEntry,
                startTime = java.time.Instant.now()
            ),
            onBackRequested = {},
            onRemoveRequested = {},
            onStatusChangeRequested = {},
            onStartStopLoggingRequested = {},
            onHoursEditRequested = {},
            onAddRunRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Recoverable Error")
@Composable
fun CollectionEntryScreenRecoverableErrorPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        addedAt = LocalDate.now()
    )
    MyGameVaultTheme {
        CollectionEntryScreenView(
            state = CollectionEntryScreenState.Idle(
                entry = sampleEntry,
                recoverableErrorMsgId = R.string.error_storage_save_failed
            ),
            onBackRequested = {},
            onRemoveRequested = {},
            onStatusChangeRequested = {},
            onStartStopLoggingRequested = {},
            onHoursEditRequested = {},
            onAddRunRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}
