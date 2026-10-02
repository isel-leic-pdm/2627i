package isel.dei.pdm.demos.demo8puzzle.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.dei.pdm.demos.demo8puzzle.play.buildLLogTag
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The activity that corresponds to the About screen
 */
class AboutActivity : ComponentActivity() {

    private val logTag = buildLLogTag(this::class.java.simpleName)

    companion object {
        fun navigateFrom(origin: ComponentActivity) {
            val msg = Intent(origin, AboutActivity::class.java)
            origin.startActivity(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                AboutScreen(onNavigateToGitHub = { /* TODO */ })
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.v(logTag, "onStart() on ${hashCode()}")
    }

    override fun onStop() {
        super.onStop()
        Log.v(logTag, "onStop() on ${hashCode()}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(logTag, "onDestroy() on ${hashCode()}")
    }
}
