package isel.dei.pdm.demos.gamevault.collection.games.details

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsViewModel.Companion.GAME_ID_KEY
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying a game of the user's collection, identified by the game
 * it was navigated to with (see [navigate]). It is closed if the game is not in the collection
 * (e.g. once it is removed).
 */
class GameDetailsActivity : ComponentActivity() {

    /**
     * The dependencies to be used.
     */
    private val dependencies: DependenciesContainer by lazy {
        application as DependenciesContainer
    }

    /**
     * The view model, whose game's identifier is read from the intent's extras, which the
     * [androidx.lifecycle.SavedStateHandle] is initialized with.
     */
    private val viewModel: GameDetailsViewModel by viewModels {
        GameDetailsViewModel.factory(gameRepository = dependencies.gameCollectionRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                DemoGameVaultTheme {
                    GameDetailsScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onGameNotFound = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Navigates to the given game of the collection. Its identifier is passed as the intent's
         * extra, under the key the view model reads it from.
         * @param from The activity to navigate from.
         * @param gameId The identifier of the game to be displayed.
         */
        fun navigate(from: ComponentActivity, gameId: Long) {
            val intent = Intent(from, GameDetailsActivity::class.java)
                .putExtra(GAME_ID_KEY, gameId)
            from.startActivity(intent)
        }
    }
}
