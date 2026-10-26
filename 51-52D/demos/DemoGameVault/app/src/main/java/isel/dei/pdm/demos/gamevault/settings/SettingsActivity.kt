package isel.dei.pdm.demos.gamevault.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Activity responsible for displaying the settings screen.
 */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoGameVaultTheme {
                // TODO: Implement
            }
        }
    }
}