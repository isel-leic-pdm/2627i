package isel.dei.pdm.puzzle.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.dei.pdm.puzzle.play.PlayActivity
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The application's entry point.
 * This activity displays the [StartScreen] and handles the navigation to the [PlayActivity].
 * It is configured with `noHistory=true` in the manifest, so it is removed from the back stack
 * once the user navigates away from it.
 */
class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                StartScreen(onStartRequested = { PlayActivity.navigateTo(origin = this) })
            }
        }
    }
}