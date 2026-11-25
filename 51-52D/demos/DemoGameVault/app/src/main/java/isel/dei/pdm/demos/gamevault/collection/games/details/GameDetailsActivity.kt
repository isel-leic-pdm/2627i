package isel.dei.pdm.demos.gamevault.collection.games.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
 *
 * While the activity is not visible (e.g. after the user pressed the home button, or with the
 * device's screen off), the active play session, if any, is shown in a notification (see
 * [PlaySessionNotificationObserver]).
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

    /**
     * The request of the permission to post notifications, which the active play session is shown
     * in (see [isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier]). The session is started
     * whatever the answer, once given, so that it is shown if granted.
     *
     * It is registered along with the activity, as required, and its callback does not depend on
     * the request, so that the answer still starts the session if the activity is recreated
     * meanwhile (e.g. if the device is rotated while the system's dialog is displayed).
     */
    private val notificationsPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onStartSession()
        }

    /**
     * Starts a play session, asking for the permission to post notifications first, if needed.
     * Since Android 13, it is required to show the session while the user is not looking at the
     * app, and is asked for when starting a session, as that is when it is needed.
     */
    private fun startSession() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            )
        if (needsPermission)
            notificationsPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
        else
            viewModel.onStartSession()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(
            PlaySessionNotificationObserver(
                notifier = dependencies.playSessionNotifier,
                // The last state is kept, even if no longer collected
                activeSession = {
                    (viewModel.state.value as? GameDetailsScreenState.Displaying)?.entry
                },
                isChangingConfigurations = { isChangingConfigurations }
            )
        )
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                DemoGameVaultTheme {
                    GameDetailsScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onGameNotFound = { finish() },
                        onStartSession = ::startSession
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Navigates to the given game of the collection.
         * @param from The activity to navigate from.
         * @param gameId The identifier of the game to be displayed.
         */
        fun navigate(from: ComponentActivity, gameId: Long) {
            from.startActivity(intent(from, gameId))
        }

        /**
         * The intent that starts the activity with the given game of the collection (e.g. to
         * be started from outside an activity). Its identifier is passed as the intent's extra,
         * under the key the view model reads it from.
         * @param context The context the intent is created in.
         * @param gameId The identifier of the game to be displayed.
         */
        fun intent(context: Context, gameId: Long): Intent =
            Intent(context, GameDetailsActivity::class.java).putExtra(GAME_ID_KEY, gameId)
    }
}
