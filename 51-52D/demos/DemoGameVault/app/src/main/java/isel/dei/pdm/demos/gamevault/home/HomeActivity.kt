package isel.dei.pdm.demos.gamevault.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionActivity
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesActivity
import isel.dei.pdm.demos.gamevault.settings.SettingsActivity
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The app's entry point, which displays the home screen.
 */
class HomeActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModel.factory(repository = dependencies.gameCollectionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoGameVaultTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onOpenSettings = { SettingsActivity.navigate(this) },
                    onOpenPlatforms = { PlatformCollectionActivity.navigate(this) },
                    onAddGame = { SearchForGamesActivity.navigate(this) }
                )
            }
        }
    }
}
