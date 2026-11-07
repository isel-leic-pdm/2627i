package isel.dei.pdm.demos.gamevault.collection.platforms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformActivity
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying the platforms in the user's collection.
 */
class PlatformCollectionActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    private val viewModel: PlatformCollectionViewModel by viewModels {
        PlatformCollectionViewModel.factory(
            repository = dependencies.platformCollectionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoGameVaultTheme {
                PlatformCollectionScreen(
                    viewModel = viewModel,
                    onAddPlatform = { SearchForPlatformActivity.navigate(this) },
                    onBack = { finish() }
                )
            }
        }
    }
}
