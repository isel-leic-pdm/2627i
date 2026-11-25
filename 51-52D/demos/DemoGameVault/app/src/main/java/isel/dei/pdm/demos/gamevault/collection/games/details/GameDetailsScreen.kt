package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialog
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.home.HomeScreenSampleData
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The composable function that defines the game details screen: the app bar (see
 * [GameDetailsTopBar]) and, depending on the state, the game's view (see [GameDetailsScreenView]),
 * the view displayed while it is being read (see [GameDetailsLoadingView]), or an error view from
 * which reading it can be retried.
 *
 * The game can only be removed while it is displayed. If it is not in the collection (e.g. it was
 * just removed), there is nothing left to display, so [onGameNotFound] is invoked (e.g. to close
 * the screen), the loading view being displayed meanwhile, as it has no information.
 *
 * Play sessions are bound to the screen: stopping one is confirmed through a dialog (see
 * [StopSessionDialog]), and so is leaving the screen while one is active (through the app bar or
 * the system's back), as it stops the session. The screen is only left once the session is
 * stopped. Failures to start or stop sessions are shown in a snackbar.
 *
 * DRAFT: The actions that are not implemented yet (i.e. those that change the game, which require
 * dialogs that do not exist yet, and removing it) are no-ops.
 *
 * @param viewModel The view model for the screen.
 * @param onBack The callback to be invoked when the back button is pressed.
 * @param onGameNotFound The callback to be invoked when the game is not in the collection.
 */
@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBack: () -> Unit,
    onGameNotFound: () -> Unit
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    val isReadyToLeave = viewModel.isReadyToLeave.collectAsStateWithLifecycle().value
    val actionFailure = viewModel.actionFailure.collectAsStateWithLifecycle().value

    // Invoked once per arrival at the state, even if the callback changes meanwhile
    val currentOnGameNotFound by rememberUpdatedState(onGameNotFound)
    val isNotFound = currentState is GameDetailsScreenState.NotFound
    LaunchedEffect(isNotFound) {
        if (isNotFound) currentOnGameNotFound()
    }

    // The screen is left once the session is stopped, as asked by the user
    val currentOnBack by rememberUpdatedState(onBack)
    LaunchedEffect(isReadyToLeave) {
        if (isReadyToLeave) currentOnBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val actionFailureMessage = stringResource(R.string.game_details_session_failure)
    LaunchedEffect(actionFailure) {
        if (actionFailure != null) {
            snackbarHostState.showSnackbar(actionFailureMessage)
            viewModel.onActionFailureShown()
        }
    }

    // Which dialog is open, if any, is UI state, so it is kept here, surviving configuration
    // changes. Once the session is no longer active (e.g. once stopped), it is closed.
    var stopDialog by rememberSaveable { mutableStateOf<StopDialog?>(null) }
    val activeSession = (currentState as? GameDetailsScreenState.Displaying)
        ?.takeIf { it.sessionElapsedTime != null }
    val hasNoSession = currentState is GameDetailsScreenState.Displaying && activeSession == null
    LaunchedEffect(hasNoSession) {
        if (hasNoSession) stopDialog = null
    }

    // Leaving the screen stops the active session, so it is confirmed first
    BackHandler(enabled = activeSession != null) {
        stopDialog = StopDialog.LEAVING
    }
    val onLeave = {
        if (activeSession != null) stopDialog = StopDialog.LEAVING
        else onBack()
    }

    val dialog = stopDialog
    if (dialog != null && activeSession?.sessionElapsedTime != null) {
        StopSessionDialog(
            elapsedTime = activeSession.sessionElapsedTime,
            isShort = activeSession.isSessionShort,
            isLeaving = dialog == StopDialog.LEAVING,
            onStop = { register ->
                viewModel.onStopSession(register, leave = dialog == StopDialog.LEAVING)
                stopDialog = null
            },
            onDismiss = { stopDialog = null }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SCREEN_TAG),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            GameDetailsTopBar(
                onBack = onLeave,
                onRemove = { },
                isRemoveEnabled = currentState is GameDetailsScreenState.Displaying
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentState) {
            is GameDetailsScreenState.Loading,
            is GameDetailsScreenState.NotFound -> GameDetailsLoadingView(modifier = contentModifier)
            is GameDetailsScreenState.Displaying -> GameDetailsScreenView(
                entry = currentState.entry,
                sessionElapsedTime = currentState.sessionElapsedTime,
                onChangeState = { },
                onEditPlayTime = { },
                onAddRun = { },
                onRegisterPlatinum = { },
                onEditNotes = { },
                onStartSession = viewModel::onStartSession,
                onStopSession = { stopDialog = StopDialog.STOPPING },
                modifier = contentModifier
            )
            is GameDetailsScreenState.Error -> ErrorViewOverlay(
                message = stringResource(R.string.game_details_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.game_details_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
        }
    }
}

/**
 * The dialogs through which stopping the active play session is confirmed: when the user asks to
 * stop it, or when they are leaving the screen, which stops it (see [StopSessionDialog]).
 */
private enum class StopDialog { STOPPING, LEAVING }

/**
 * The tip displayed to the user for the given persistence error, hinting on what can be done
 * about it. The tips are those of the platform collection screen, as the storage is the same.
 */
@StringRes
private fun PersistenceException.tipResId(): Int =
    when (this) {
        is RecoverablePersistenceException -> R.string.platform_collection_error_tip_recoverable
        is UnrecoverablePersistenceException -> R.string.platform_collection_error_tip_unrecoverable
    }

// Testing & preview utilities

object GameDetailsScreenTestTags {
    const val SCREEN_TAG = "GameDetailsScreen"
}

@Suppress("Unused")
class GameDetailsScreenPreviews {

    @Preview(showBackground = true, showSystemUi = true, name = "Game details screen")
    @Composable
    fun GameDetailsScreenPreview() {
        val entry = HomeScreenSampleData.entries.first()
        DemoGameVaultTheme {
            GameDetailsScreen(
                viewModel = GameDetailsViewModel(
                    gameRepo = FakeGameCollectionRepository(HomeScreenSampleData.entries),
                    savedStateHandle = SavedStateHandle(
                        mapOf(GameDetailsViewModel.GAME_ID_KEY to entry.game.id)
                    )
                ),
                onBack = { },
                onGameNotFound = { }
            )
        }
    }
}
