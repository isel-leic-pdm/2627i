package isel.dei.pdm.demos.gamevault.collection.games.details

import android.os.Parcelable
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
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlaySessionReminders
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Closed
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Displaying
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.games.details.parts.StopSessionDialog
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.home.HomeScreenSampleData
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.parcelize.Parcelize

/**
 * The composable function that defines the game details screen: the app bar (see
 * [GameDetailsTopBar]) and, depending on the state, the game's view (see [GameDetailsScreenView]),
 * the view displayed while it is being read (see [GameDetailsLoadingView]), or an error view from
 * which reading it can be retried.
 *
 * The game can only be removed while it is displayed. Once the screen is [Closed], the callback
 * of the reason is invoked (e.g. to close the screen), the loading view being displayed meanwhile,
 * as there is nothing left to display: [onGameNotFound] if the game is not in the collection (e.g.
 * it was just removed), or [onBack] if the user left the screen.
 *
 * Play sessions are bound to the screen: stopping one is confirmed through a dialog (see
 * [StopSessionDialog]), and so is leaving the screen while one is active (through the app bar or
 * the system's back), as it stops the session. The screen is only left once the session is
 * stopped. Failures of the actions on the game are shown in a snackbar.
 *
 * At most one dialog is open at a time (see [DetailsDialog]). Which one is UI state, so it is kept
 * here, surviving configuration changes.
 *
 * DRAFT: The actions that are not implemented yet (i.e. those that change the game, which require
 * dialogs that do not exist yet, and removing it) are no-ops.
 *
 * @param viewModel The view model for the screen.
 * @param onBack The callback to be invoked when the user leaves the screen.
 * @param onGameNotFound The callback to be invoked when the game is not in the collection.
 * @param onStartSession The callback to be invoked when the user asks to start a play session. It
 * is up to the caller to start it (see [GameDetailsViewModel.onStartSession]), as it may require
 * platform specific preconditions (e.g. asking for the permission to post the notification it is
 * shown in), unlike the remaining actions, which the screen requests from the view model itself.
 */
@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBack: () -> Unit,
    onGameNotFound: () -> Unit,
    onStartSession: () -> Unit
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    val actionFailure = viewModel.actionFailure.collectAsStateWithLifecycle().value

    // Invoked once per closing, even if the callbacks change meanwhile
    val currentOnBack by rememberUpdatedState(onBack)
    val currentOnGameNotFound by rememberUpdatedState(onGameNotFound)
    val closeReason = (currentState as? Closed)?.reason
    LaunchedEffect(closeReason) {
        when (closeReason) {
            Closed.Reason.GAME_NOT_FOUND -> currentOnGameNotFound()
            Closed.Reason.LEFT -> currentOnBack()
            null -> Unit
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val actionFailureMessage = actionFailure?.let { stringResource(it.messageResId()) }
    LaunchedEffect(actionFailure) {
        if (actionFailureMessage != null) {
            snackbarHostState.showSnackbar(actionFailureMessage)
            viewModel.onActionFailureShown()
        }
    }

    var dialog by rememberSaveable { mutableStateOf<DetailsDialog?>(null) }
    val activeSession = currentState.activeSession

    // Leaving the screen stops the active session, so it is confirmed first
    val onLeave = {
        if (activeSession != null) dialog = DetailsDialog.StopSession(activeSession, isLeaving = true)
        else onBack()
    }
    BackHandler(enabled = activeSession != null, onBack = onLeave)

    when (val openDialog = dialog) {
        is DetailsDialog.StopSession ->
            // Only if the request is about the active session (e.g. not if it was stopped meanwhile)
            if (activeSession != null && openDialog.isAbout(activeSession)) {
                StopSessionDialog(
                    elapsedTime = checkNotNull(activeSession.sessionElapsedTime),
                    isShort = activeSession.isSessionShort,
                    isLeaving = openDialog.isLeaving,
                    onStop = { register ->
                        viewModel.onStopSession(register, leave = openDialog.isLeaving)
                        dialog = null
                    },
                    onDismiss = { dialog = null }
                )
            }
        null -> Unit
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
                isRemoveEnabled = currentState is Displaying
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentState) {
            is GameDetailsScreenState.Loading,
            is Closed -> GameDetailsLoadingView(modifier = contentModifier)
            is Displaying -> GameDetailsScreenView(
                entry = currentState.entry,
                sessionElapsedTime = currentState.sessionElapsedTime,
                onChangeState = { },
                onEditPlayTime = { },
                onAddRun = { },
                onRegisterPlatinum = { },
                onEditNotes = { },
                onStartSession = onStartSession,
                onStopSession = {
                    if (activeSession != null)
                        dialog = DetailsDialog.StopSession(activeSession, isLeaving = false)
                },
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
 * The dialogs of the screen, at most one of which is open at a time. Each one holds what it is
 * about, so that a dialog requested about something that no longer holds (e.g. a session that was
 * stopped meanwhile) is not displayed, instead of having to be closed when it changes. They are
 * parcelable, so that the open one survives configuration changes and process death.
 */
private sealed interface DetailsDialog : Parcelable {

    /**
     * The dialog through which stopping the active play session is confirmed (see
     * [StopSessionDialog]): when the user asks to stop it, or when they are leaving the screen,
     * which stops it.
     * @property sessionStartMillis The start time of the session it is about, in milliseconds
     * since the epoch, which identifies the session.
     * @property isLeaving Whether the user is leaving the screen.
     */
    @Parcelize
    data class StopSession(
        val sessionStartMillis: Long,
        val isLeaving: Boolean
    ) : DetailsDialog {

        /**
         * The dialog about the session of the given [state], which must have an active one.
         */
        constructor(state: Displaying, isLeaving: Boolean) : this(
            sessionStartMillis = checkNotNull(state.entry.sessionStartTime).toEpochMilliseconds(),
            isLeaving = isLeaving
        )

        /**
         * Whether it is about the session of the given [state].
         */
        fun isAbout(state: Displaying): Boolean =
            state.entry.sessionStartTime?.toEpochMilliseconds() == sessionStartMillis
    }
}

/**
 * The state, if the game is displayed with an active play session, or null otherwise.
 */
private val GameDetailsScreenState.activeSession: Displaying?
    get() = (this as? Displaying)?.takeIf { it.sessionElapsedTime != null }

/**
 * The message displayed to the user for the given failure.
 */
@StringRes
private fun GameActionFailure.messageResId(): Int =
    when (action) {
        GameActionFailure.Action.START_SESSION -> R.string.game_details_start_session_failure
        GameActionFailure.Action.STOP_SESSION -> R.string.game_details_stop_session_failure
    }

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
                    ),
                    reminders = FakePlaySessionReminders()
                ),
                onBack = { },
                onGameNotFound = { },
                onStartSession = { }
            )
        }
    }
}
