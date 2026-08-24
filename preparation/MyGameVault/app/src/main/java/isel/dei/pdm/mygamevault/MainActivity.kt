package isel.dei.pdm.mygamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.dei.pdm.mygamevault.add.AddGameViewModel
import isel.dei.pdm.mygamevault.preferences.PreferencesViewModel
import isel.dei.pdm.mygamevault.ui.AppScaffold
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

class MainActivity : ComponentActivity() {

    private val searchService by lazy {
        (application as DependenciesContainer).searchService
    }

    private val secretsRepository by lazy {
        (application as DependenciesContainer).secretsRepository
    }

    private val addGameViewModel by viewModels<AddGameViewModel> {
        AddGameViewModel.factory(searchService)
    }

    private val preferencesViewModel by viewModels<PreferencesViewModel> {
        PreferencesViewModel.factory(secretsRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGameVaultTheme {
                AppScaffold(
                    addGameViewModel = addGameViewModel,
                    preferencesViewModel = preferencesViewModel
                )
            }
        }
    }
}
