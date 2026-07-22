package isel.dei.pdm.puzzle.play

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import isel.dei.pdm.puzzle.about.AboutActivity
import isel.dei.pdm.puzzle.play.views.TerminationConfirmationDialog
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The activity that hosts the main gameplay screen.
 * This activity is responsible for displaying the [PlayScreen].
 */
class PlayActivity : ComponentActivity() {

    private var isConfirmingExit by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isConfirmingExit = true
            }
        })

        setContent {
            Demo8PuzzleTheme {
                PlayScreen(
                    onInfoRequested = { AboutActivity.navigateTo(origin = this) }
                )
                if (isConfirmingExit) {
                    TerminationConfirmationDialog(
                        onConfirm = { finish() },
                        onDismiss = { isConfirmingExit = false }
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Navigates to the [PlayActivity] from the given [origin] context.
         * This method encapsulates the creation and launching of the [Intent] for this activity.
         * @param origin the context from which the navigation is requested.
         */
        internal fun navigateTo(origin: Context) {
            val intent = Intent(origin, PlayActivity::class.java)
            origin.startActivity(intent)
        }
    }
}
