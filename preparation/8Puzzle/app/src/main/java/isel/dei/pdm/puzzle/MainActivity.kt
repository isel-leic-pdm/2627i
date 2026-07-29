package isel.dei.pdm.puzzle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import isel.dei.pdm.puzzle.about.AboutScreen
import isel.dei.pdm.puzzle.infrastructure.PuzzleKey
import isel.dei.pdm.puzzle.play.PlayScreen
import isel.dei.pdm.puzzle.play.views.TerminationConfirmationDialog
import isel.dei.pdm.puzzle.start.StartScreen
import isel.dei.pdm.puzzle.ui.theme.Demo8PuzzleTheme

/**
 * The single activity that hosts all screens in the application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val backStack = rememberNavBackStack(PuzzleKey.Start as NavKey)

            Demo8PuzzleTheme {
                NavDisplay(
                    backStack = backStack,
                    onBack = { backStack.removeAt(backStack.size - 1) },
                    entryProvider = { key ->
                        when (key) {
                            is PuzzleKey.Start -> NavEntry(key) {
                                StartScreen(onStartRequested = {
                                    // Replicate noHistory=true behavior: replace Start with Play
                                    backStack.clear()
                                    backStack.add(PuzzleKey.Play)
                                })
                            }
                            is PuzzleKey.Play -> NavEntry(key) {
                                PlayScreenContent(
                                    onInfoRequested = { backStack.add(PuzzleKey.About) },
                                    onExitConfirmed = { finish() }
                                )
                            }
                            is PuzzleKey.About -> NavEntry(key) {
                                AboutScreen(onOpenGitHubRequested = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        "https://github.com/isel-leic-pdm".toUri()
                                    )
                                    startActivity(intent)
                                })
                            }
                            else -> NavEntry(key) { }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun PlayScreenContent(
    onInfoRequested: () -> Unit,
    onExitConfirmed: () -> Unit
) {
    var isConfirmingExit by remember { mutableStateOf(false) }

    // Intercept back to show the confirmation dialog
    BackHandler {
        isConfirmingExit = true
    }
    
    PlayScreen(onInfoRequested = onInfoRequested)

    if (isConfirmingExit) {
        TerminationConfirmationDialog(
            onConfirm = onExitConfirmed,
            onDismiss = { isConfirmingExit = false }
        )
    }
}
