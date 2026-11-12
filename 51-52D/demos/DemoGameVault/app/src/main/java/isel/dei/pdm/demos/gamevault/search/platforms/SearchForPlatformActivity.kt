package isel.dei.pdm.demos.gamevault.search.platforms

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
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
            repository = dependencies.platformCollectionRepository,
            imageStore = dependencies.imageStore
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                DemoGameVaultTheme {
                    SearchForPlatformScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        fun navigate(from: ComponentActivity) {
            from.startActivity(Intent(from, SearchForPlatformActivity::class.java))
        }
    }
}
