package isel.dei.pdm.demos.demo8puzzle.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import isel.dei.pdm.demos.demo8puzzle.play.PlayActivity
import isel.dei.pdm.demos.demo8puzzle.play.buildLLogTag
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

/**
 * Represents the activity that hosts the main screen of the app.
 */
class MainActivity : ComponentActivity() {

    private val logTag = buildLLogTag(this::class.java.simpleName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                MainScreen(onPlay = { PlayActivity.navigateFrom(this) })
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
