package isel.dei.pdm.demos.demo8puzzle

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import isel.dei.pdm.demos.demo8puzzle.play.PuzzleScreen
import isel.dei.pdm.demos.demo8puzzle.play.PuzzleScreenViewModel
import isel.dei.pdm.demos.demo8puzzle.ui.theme.Demo8PuzzleTheme

const val APP_TAG = "Demo8PuzzleApp"
fun buildLLogTag(className: String) = "$APP_TAG.$className"

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<PuzzleScreenViewModel>()

    private val logTag = buildLLogTag(this::class.java.simpleName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(logTag, "onCreate() on ${hashCode()}")
        enableEdgeToEdge()
        setContent {
            Demo8PuzzleTheme {
                PuzzleScreen(viewModel = viewModel)
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