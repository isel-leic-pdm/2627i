package isel.dei.pdm.demos.demo8puzzle.play

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.dei.pdm.demos.demo8puzzle.about.AboutActivity
import isel.dei.pdm.demos.demo8puzzle.shared.BaseActivity
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The activity used to host the puzzle screen.
 * It allows the user to play the puzzle game, or have it solved automatically.
 */
class PlayActivity : BaseActivity() {

    private val viewModel by viewModels<PuzzleScreenViewModel>()

    companion object {
        fun navigate(origin: ComponentActivity) {
            val msg = Intent(origin, PlayActivity::class.java)
            origin.startActivity(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(
                    viewModel = viewModel,
                    onAbout = { AboutActivity.navigate(this) }
                )
            }
        }
    }
}
