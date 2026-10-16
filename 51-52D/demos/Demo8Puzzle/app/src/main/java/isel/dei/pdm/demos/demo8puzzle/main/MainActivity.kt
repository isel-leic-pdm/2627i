package isel.dei.pdm.demos.demo8puzzle.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.dei.pdm.demos.demo8puzzle.play.PlayActivity
import isel.dei.pdm.demos.demo8puzzle.shared.BaseActivity
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Represents the activity that hosts the main screen of the app.
 * This screen is a simple splash screen, used to show the app's logo and to direct the user to
 * the play screen.
 */
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                MainScreen(onPlay = { PlayActivity.navigate(this) })
            }
        }
    }
}
