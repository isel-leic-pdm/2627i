package isel.dei.pdm.demos.gamevault.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying the settings screen.
 */
class SettingsActivity : ComponentActivity() {

    /**
     * The dependency for the credentials repository to be used by the app.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel by viewModels<SettingsViewModel> {
        SettingsViewModel.factory(
            credentialsRepository = dependencies.credentialsRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoGameVaultTheme {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}