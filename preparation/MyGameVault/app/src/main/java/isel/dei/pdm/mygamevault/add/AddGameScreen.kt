package isel.dei.pdm.mygamevault.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.ports.UnauthenticatedException
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView

/**
 * The "Add Game" screen where users can search for games to add to their collection.
 */
@Composable
fun AddGameScreen(
    viewModel: AddGameViewModel,
    onAddRequested: (Game) -> Unit,
    onDetailsRequested: (Game) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val searchQuery by viewModel.query.collectAsStateWithLifecycle()

    if (state is AddGameScreenState.Error) {
        val errorMessage = when ((state as AddGameScreenState.Error).error) {
            is UnauthenticatedException -> 
                stringResource(R.string.error_storage_access) // Reusing a generic storage/config error
            else -> stringResource(R.string.error_generic)
        }
        FatalErrorView(
            message = errorMessage,
            buttonText = stringResource(R.string.retry_label),
            onButtonClicked = { viewModel.onQueryChange(searchQuery) } // Trigger search again
        )
    } else {
        AddGameScreenView(
            state = state,
            searchQuery = searchQuery,
            onQueryChange = viewModel::onQueryChange,
            onPlatformChange = viewModel::onPlatformChange,
            onCategoryChange = viewModel::onCategoryChange,
            onAddRequested = onAddRequested,
            onDetailsRequested = onDetailsRequested,
            onRecoverableErrorConsumed = viewModel::onRecoverableErrorConsumed
        )
    }
}
