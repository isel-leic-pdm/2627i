package isel.dei.pdm.mygamevault.collection.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView

@Composable
fun CollectionEntryScreen(
    viewModel: CollectionEntryViewModel,
    gameId: Long,
    platformId: Long,
    onBackRequested: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(gameId, platformId) {
        viewModel.fetchEntryDetails(gameId, platformId)
    }

    val currentState = state
    if (currentState is CollectionEntryScreenState.FatalError) {
        FatalErrorView(
            message = stringResource(currentState.messageResId),
            buttonText = stringResource(
                if (currentState.canRetry) R.string.retry_label else R.string.game_details_back
            ),
            onButtonClicked = {
                if (currentState.canRetry) {
                    viewModel.fetchEntryDetails(gameId, platformId)
                } else {
                    onBackRequested()
                }
            }
        )
    } else {
        CollectionEntryScreenView(
            state = currentState,
            onBackRequested = onBackRequested,
            onRemoveRequested = {
                viewModel.removeGame(onSuccess = onBackRequested)
            },
            onStatusChangeRequested = viewModel::updateStatus,
            onStartStopLoggingRequested = {
                if (currentState is CollectionEntryScreenState.Logging) {
                    viewModel.stopLogging()
                } else {
                    viewModel.startLogging()
                }
            },
            onHoursEditRequested = viewModel::updateHours,
            onAddRunRequested = viewModel::incrementRuns,
            onRecoverableErrorConsumed = viewModel::onRecoverableErrorConsumed
        )
    }
}
