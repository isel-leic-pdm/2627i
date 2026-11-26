package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Closed
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Displaying
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Error
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Loading
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.StartSessionResult
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionReminders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the game details screen. The view model is responsible for managing the
 * state of the screen and for interacting with the remaining parts of the application.
 *
 * The game is identified by its identifier, expected to be found in the [SavedStateHandle] under
 * [GAME_ID_KEY] (e.g. passed by the screens that navigate to it). Its entry, which holds all that
 * the screen displays, the user's notes included, is observed, so that the screen reflects every
 * change to it (e.g. made through this screen). If the game is not in the collection (e.g. it was
 * just removed), the screen is [Closed], as it also is once the user leaves it (see
 * [onStopSession]), so that closing the screen is signalled in a single way.
 *
 * While a play session is active, the time elapsed since it started is kept up to date: it is
 * recomputed whenever a whole minute elapses, as it is displayed in whole minutes (see
 * [Displaying.sessionElapsedTime]).
 *
 * The game is only observed (and the elapsed time only kept up to date) while the state is being
 * collected, and for [STOP_TIMEOUT] afterward, so that observation is not restarted on
 * configuration changes. When observation is restarted (e.g. when the app returns from the
 * background), the last state is kept until the game is read again.
 *
 * @param gameRepo The repository where the user's collection of games is stored.
 * @param savedStateHandle The handle from which the game's identifier is read.
 * @param reminders The scheduler of the reminders of the sessions, which become due once they
 * are long (e.g. if forgotten).
 * @param clock The clock used to compute the time elapsed since the session started.
 * @throws IllegalStateException If the game's identifier is not in the [savedStateHandle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailsViewModel(
    private val gameRepo: GameCollectionRepository,
    savedStateHandle: SavedStateHandle,
    private val reminders: PlaySessionReminders,
    private val clock: Clock = Clock.System
) : ViewModel() {

    private val gameId: Long = checkNotNull(savedStateHandle.get<Long>(GAME_ID_KEY)) {
        "The game's identifier must be provided under $GAME_ID_KEY"
    }

    /**
     * What causes the game to be observed: the start of the observation, whether the first or a
     * restart (e.g. when the app returns from the background), or the user retrying after an
     * error.
     */
    private enum class Trigger { Start, Retry }

    /**
     * The triggers of the observation of the game. Not replayed, so that restarting the
     * observation is not taken for a retry.
     */
    private val triggers = MutableSharedFlow<Trigger>(extraBufferCapacity = 1)

    /**
     * Whether the user left the screen, once its play session was stopped (see [onStopSession]).
     * Declared before [state], which depends on it.
     */
    private val hasLeft = MutableStateFlow(false)

    /**
     * The state of the screen. Once the user leaves it, it is [Closed], whatever the game's state.
     */
    val state: StateFlow<GameDetailsScreenState> = triggers
        .onStart { emit(Trigger.Start) }
        .flatMapLatest { trigger ->
            observeGame()
                // On start, the screen is already loading, or keeps its last state if observation
                // is being restarted
                .onStart { if (trigger == Trigger.Retry) emit(Loading) }
                .catch { if (it is PersistenceException) emit(Error(it)) else throw it }
        }
        .combine(hasLeft) { observed, hasLeft ->
            if (hasLeft) Closed(Closed.Reason.LEFT) else observed
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = Loading
        )

    /**
     * Implementation of the state transition that results from the user retrying after an error.
     * The screen moves to [Loading] and the game is observed again.
     * If the current state is not [Error], no action is taken. It relies on [state] being
     * collected, which is the case when invoked from the UI.
     */
    fun onRetry() {
        if (state.value is Error)
            triggers.tryEmit(Trigger.Retry)
    }

    private val _actionFailure = MutableStateFlow<GameActionFailure?>(null)

    /**
     * The failure of the last action on the game (e.g. starting a session), to be shown to the
     * user, or null if there is none to show. Once shown, [onActionFailureShown] must be invoked.
     */
    val actionFailure: StateFlow<GameActionFailure?> = _actionFailure.asStateFlow()

    /**
     * Implementation of the state transition that results from the action failure having been
     * shown to the user.
     */
    fun onActionFailureShown() {
        _actionFailure.value = null
    }

    /**
     * Starts a play session of the game, now, and schedules its reminder. The screen reflects it
     * once stored, as the game is observed. If starting it fails, the failure is published in
     * [actionFailure].
     * If the current state is not [Displaying], or a session is already active, no action is
     * taken. It relies on [state] being collected, which is the case when invoked from the UI.
     */
    fun onStartSession() {
        val state = state.value
        if (state !is Displaying || state.entry.isSessionActive)
            return
        viewModelScope.launch {
            try {
                // The remaining outcomes require no action: if the game is not in the collection,
                // the screen is closed, and the session of another game cannot be active,
                // as the screen cannot be left without stopping it (see onStopSession).
                val result = gameRepo.startSession(gameId, clock.now())
                if (result is StartSessionResult.Started)
                    reminders.schedule(result.entry)
            } catch (e: PersistenceException) {
                _actionFailure.value = GameActionFailure(GameActionFailure.Action.START_SESSION, e)
            }
        }
    }

    /**
     * Stops the active play session, now, either registering it (i.e. adding the elapsed time to
     * the time spent playing) or discarding it (e.g. if short, see [Displaying.isSessionShort]),
     * and cancels its reminder.
     * The screen reflects it once stored, as the game is observed. If [leave] is true, the screen
     * is [Closed] once the session is stopped, as the session is bound to the screen. If stopping
     * it fails, the failure is published in [actionFailure], and the screen is not left.
     * If the current state is not [Displaying], or no session is active, no action is taken. It
     * relies on [state] being collected, which is the case when invoked from the UI.
     * @param register Whether the session is registered, or discarded.
     * @param leave Whether the user is leaving the screen.
     */
    fun onStopSession(register: Boolean, leave: Boolean = false) {
        val state = state.value
        if (state !is Displaying || !state.entry.isSessionActive)
            return
        viewModelScope.launch {
            try {
                gameRepo.updateEntry(gameId) {
                    if (register) it.endSessionNow() else it.cancelSession()
                }
                reminders.cancel()
                if (leave)
                    hasLeft.value = true
            } catch (e: PersistenceException) {
                _actionFailure.value = GameActionFailure(GameActionFailure.Action.STOP_SESSION, e)
            }
        }
    }

    /**
     * Ends the entry's session now, if active (see [CollectionEntry.endSession]). If the device's
     * clock was set back, the session is taken as just started, instead of failing.
     */
    private fun CollectionEntry.endSessionNow(): CollectionEntry {
        val start = sessionStartTime ?: return this
        return endSession(maxOf(clock.now(), start))
    }

    /**
     * The states of the screen while observing the game: [Displaying] it, with the time elapsed
     * since its session started, if any, or [Closed] if it is not in the collection. Whenever
     * the entry changes, the elapsed time is recomputed from it.
     */
    private fun observeGame(): Flow<GameDetailsScreenState> =
        gameRepo.observeEntry(gameId).flatMapLatest { entry ->
            if (entry == null) flowOf(Closed(Closed.Reason.GAME_NOT_FOUND))
            else displaying(entry)
        }

    /**
     * The given entry being displayed, with the time elapsed since its session started (and
     * whether the session is short), emitted now and whenever a whole minute elapses since then.
     * If no session is active, the entry is emitted just once.
     */
    private fun displaying(entry: CollectionEntry): Flow<Displaying> {
        val start = entry.sessionStartTime ?: return flowOf(Displaying(entry, null))
        return flow {
            while (true) {
                // If the device's clock was set back, the session is taken as just started,
                // instead of failing (see CollectionEntry.sessionElapsedTime)
                val now = maxOf(clock.now(), start)
                val elapsed = entry.sessionElapsedTime(now)
                emit(Displaying(entry, elapsed, entry.isShortSession(now)))
                // As sessions are short below a whole number of minutes, it is also updated then
                delay(1.minutes - (elapsed - elapsed.inWholeMinutes.minutes))
            }
        }
    }

    companion object {
        /**
         * The key under which the identifier of the game to be displayed is expected to be found
         * in the [SavedStateHandle].
         */
        const val GAME_ID_KEY = "gameId"

        /**
         * How long the game keeps being observed after the last collector stops, so that
         * observation survives configuration changes (e.g. rotation).
         */
        val STOP_TIMEOUT: Duration = 5.seconds

        /**
         * Factory for creating instances of [GameDetailsViewModel] with the given dependencies.
         */
        fun factory(
            gameRepository: GameCollectionRepository,
            reminders: PlaySessionReminders
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    GameDetailsViewModel(gameRepository, createSavedStateHandle(), reminders)
                }
            }
    }
}
