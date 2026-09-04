package isel.dei.pdm.mygamevault.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView

/**
 * Screen for editing the application preferences (IGDB API keys).
 */
@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state is PreferencesScreenState.Error) {
        FatalErrorView(
            message = stringResource(R.string.error_storage_access),
            buttonText = stringResource(R.string.retry_label),
            onButtonClicked = { 
                // Any change triggers state update and clears Error state in this VM implementation
                viewModel.onClientIdChange(state.clientId) 
            }
        )
    } else {
        PreferencesScreenView(
            state = state,
            onClientIdChange = viewModel::onClientIdChange,
            onClientSecretChange = viewModel::onClientSecretChange,
            onSave = viewModel::onSave
        )
    }
}
