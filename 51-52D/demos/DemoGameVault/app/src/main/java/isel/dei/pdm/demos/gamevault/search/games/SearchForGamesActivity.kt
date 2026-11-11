package isel.dei.pdm.demos.gamevault.search.games

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformActivity
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying the search games screen.
 */
class SearchForGamesActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: SearchForGamesViewModel by viewModels {
        SearchForGamesViewModel.factory(
            searchService = dependencies.gameSearchService,
            gameRepository = dependencies.gameCollectionRepository,
            platformRepository = dependencies.platformCollectionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoGameVaultTheme {
                SearchForGamesScreen(
                    viewModel = viewModel,
                    // Once the user comes back, the platforms are read again, as the screen
                    // observes them again
                    onAddPlatform = { SearchForPlatformActivity.navigate(this) },
                    onBack = { finish() }
                )
            }
        }
    }

    companion object {
        fun navigate(from: ComponentActivity) {
            from.startActivity(Intent(from, SearchForGamesActivity::class.java))
        }
    }
}
