package isel.dei.pdm.mygamevault

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.add.AddGameScreen
import isel.dei.pdm.mygamevault.add.AddGameViewModel
import isel.dei.pdm.mygamevault.infrastructure.FakeSearchService
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

class MainActivity : ComponentActivity() {

    private val searchService = FakeSearchService()

    private val addGameViewModel by viewModels<AddGameViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddGameViewModel(searchService) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyGameVaultTheme {
                val state by addGameViewModel.state.collectAsStateWithLifecycle()
                val query by addGameViewModel.query.collectAsStateWithLifecycle()

                AddGameScreen(
                    state = state,
                    searchQuery = query,
                    onQueryChange = addGameViewModel::onQueryChange,
                    onGameSelected = { game ->
                        Log.d("MainActivity", "Selected game: ${game.name}")
                    }
                )
            }
        }
    }
}
