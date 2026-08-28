package isel.dei.pdm.mygamevault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.dei.pdm.mygamevault.add.AddGameViewModel
import isel.dei.pdm.mygamevault.collection.MyCollectionViewModel
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

    private val collectionRepository by lazy {
        (application as DependenciesContainer).collectionRepository
    }

    private val addGameViewModel by viewModels<AddGameViewModel> {
        AddGameViewModel.factory(searchService, collectionRepository)
    }

    private val preferencesViewModel by viewModels<PreferencesViewModel> {
        PreferencesViewModel.factory(secretsRepository)
    }

    private val myCollectionViewModel by viewModels<MyCollectionViewModel> {
        MyCollectionViewModel.factory(collectionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGameVaultTheme {
                AppScaffold(
                    addGameViewModel = addGameViewModel,
                    preferencesViewModel = preferencesViewModel,
                    myCollectionViewModel = myCollectionViewModel
                )
            }
        }
    }
}
