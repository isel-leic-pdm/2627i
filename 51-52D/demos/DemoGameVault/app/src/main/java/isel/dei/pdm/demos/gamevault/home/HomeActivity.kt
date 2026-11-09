package isel.dei.pdm.demos.gamevault.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionActivity
import isel.dei.pdm.demos.gamevault.settings.SettingsActivity
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The app's entry point, which displays the home screen.
 */
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoGameVaultTheme {
                // TODO: Replace the sample data with the screen's state, once the view model exists
                HomeScreen(
                    playing = HomeScreenSampleData.playing,
                    stats = HomeScreenSampleData.stats,
                    onOpenSettings = { SettingsActivity.navigate(this) },
                    onOpenPlatforms = { PlatformCollectionActivity.navigate(this) }
                )
            }
        }
    }
}
