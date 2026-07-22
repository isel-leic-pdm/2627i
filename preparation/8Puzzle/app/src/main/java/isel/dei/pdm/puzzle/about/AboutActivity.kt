package isel.dei.pdm.puzzle.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The activity that hosts the About screen.
 */
class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                AboutScreen(
                    onOpenGitHubRequested = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/isel-leic-pdm".toUri()
                        )
                        startActivity(intent)
                    }
                )
            }
        }
    }

    companion object {
        /**
         * Navigates to the [AboutActivity] from the given [origin] context.
         * @param origin the context from which the navigation is requested.
         */
        internal fun navigateTo(origin: Context) {
            val intent = Intent(origin, AboutActivity::class.java)
            origin.startActivity(intent)
        }
    }
}
