package isel.dei.pdm.demos.gamevault.search.platforms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying the search platforms screen.
 */
class SearchForPlatformActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: SearchForPlatformViewModel by viewModels {
        SearchForPlatformViewModel.factory(
            searchService = dependencies.platformSearchService,
            repository = dependencies.platformCollectionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoGameVaultTheme {
                SearchForPlatformScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }
}
