package isel.dei.pdm.demos.gamevault.collection.games

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.COMPLETION_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.NAME_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.PLATFORM_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.STATE_KEY
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for browsing the games in the user's collection, starting with the query
 * it was navigated to with (see [navigate]).
 */
class GameCollectionActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    /**
     * The view model, whose initial query is read from the intent's extras, which the
     * [androidx.lifecycle.SavedStateHandle] is initialized with.
     */
    private val viewModel: GameCollectionViewModel by viewModels {
        GameCollectionViewModel.factory(
            gameRepository = dependencies.gameCollectionRepository,
            platformRepository = dependencies.platformCollectionRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                DemoGameVaultTheme {
                    GameCollectionScreen(
                        viewModel = viewModel,
                        onBack = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Navigates to the games collection, browsed with the given query. Its criteria are passed
         * as the intent's extras, under the keys the view model reads them from. The initial (see
         * [CollectionQuery.initial]) is not passed, as it is not a criterion of this screen.
         * @param from The activity to navigate from.
         * @param query The query to start browsing with. By default, the whole collection.
         */
        fun navigate(from: ComponentActivity, query: CollectionQuery = CollectionQuery()) {
            val intent = Intent(from, GameCollectionActivity::class.java).apply {
                query.state?.let { putExtra(STATE_KEY, it.name) }
                query.completion?.let { putExtra(COMPLETION_KEY, it.name) }
                query.platform?.let { putExtra(PLATFORM_KEY, it.id) }
                query.nameContains?.let { putExtra(NAME_KEY, it.value) }
            }
            from.startActivity(intent)
        }
    }
}
