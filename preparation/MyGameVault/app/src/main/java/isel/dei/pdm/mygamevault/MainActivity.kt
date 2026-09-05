package isel.dei.pdm.mygamevault

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import isel.dei.pdm.mygamevault.infrastructure.SessionNotificationManager
import isel.dei.pdm.mygamevault.ui.AppRoute
import isel.dei.pdm.mygamevault.ui.AppScaffold
import isel.dei.pdm.mygamevault.ui.common.LocalImageLoader
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

class MainActivity : ComponentActivity() {

    private val dependencies by lazy { application as DependenciesContainer }
    private var initialRoute by mutableStateOf<AppRoute?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalImageLoader provides dependencies.imageLoader) {
                MyGameVaultTheme {
                    AppScaffold(
                        dependencies = dependencies,
                        initialRoute = initialRoute
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val gameId = intent.getLongExtra(SessionNotificationManager.EXTRA_GAME_ID, -1L)
        val platformId = intent.getLongExtra(SessionNotificationManager.EXTRA_PLATFORM_ID, -1L)
        if (gameId != -1L && platformId != -1L) {
            initialRoute = AppRoute.EntryDetails(gameId, platformId)
            intent.removeExtra(SessionNotificationManager.EXTRA_GAME_ID)
            intent.removeExtra(SessionNotificationManager.EXTRA_PLATFORM_ID)
        }
    }
}
