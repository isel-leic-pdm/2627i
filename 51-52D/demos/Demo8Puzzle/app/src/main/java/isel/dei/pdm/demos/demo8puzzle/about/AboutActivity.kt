package isel.dei.pdm.demos.demo8puzzle.about

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.net.toUri
import isel.dei.pdm.demos.demo8puzzle.shared.BaseActivity
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme


/**
 * The activity that corresponds to the About screen.
 * It displays information about the app and the context in which it was created.
 */
class AboutActivity : BaseActivity() {

    companion object {
        fun navigate(origin: ComponentActivity) {
            val msg = Intent(origin, AboutActivity::class.java)
            origin.startActivity(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                AboutScreen(onNavigateToGitHub = ::navigateToGitHub)
            }
        }
    }

    private fun navigateToGitHub() {
        val url = "https://github.com/isel-leic-pdm"
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        intent.resolveActivity(packageManager)?.let {
            startActivity(intent)
        }
    }
}
