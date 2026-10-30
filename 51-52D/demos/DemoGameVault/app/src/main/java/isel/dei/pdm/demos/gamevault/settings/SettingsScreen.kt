package isel.dei.pdm.demos.gamevault.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Represents the settings screen.
 * @param viewModel The view model for the screen.
 * @param onBack The callback to be invoked when the back button is pressed.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    SettingsScreenView(
        state = currentState,
        onClientIdChange = viewModel::onClientIdChange,
        onClientSecretChange = viewModel::onClientSecretChange,
        onSave = viewModel::onSave,
        onBack = onBack
    )
}
