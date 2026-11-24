package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Displaying
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Error
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Loading
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.NotFound
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
 * just removed), the screen moves to [NotFound].
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
 * @param clock The clock used to compute the time elapsed since the session started.
 * @throws IllegalStateException If the game's identifier is not in the [savedStateHandle].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailsViewModel(
    private val gameRepo: GameCollectionRepository,
    savedStateHandle: SavedStateHandle,
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
     * The state of the screen.
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

    /**
     * The states of the screen while observing the game: [Displaying] it, with the time elapsed
     * since its session started, if any, or [NotFound] if it is not in the collection. Whenever
     * the entry changes, the elapsed time is recomputed from it.
     */
    private fun observeGame(): Flow<GameDetailsScreenState> =
        gameRepo.observeEntry(gameId).flatMapLatest { entry ->
            if (entry == null) flowOf(NotFound)
            else sessionElapsedTimes(entry).map { Displaying(entry, it) }
        }

    /**
     * The time elapsed since the session of the given entry started, emitted now and whenever a
     * whole minute elapses since then, or just null if no session is active.
     */
    private fun sessionElapsedTimes(entry: CollectionEntry): Flow<Duration?> {
        val start = entry.sessionStartTime ?: return flowOf(null)
        return flow {
            while (true) {
                // If the device's clock was set back, the session is taken as just started,
                // instead of failing (see CollectionEntry.sessionElapsedTime)
                val elapsed = entry.sessionElapsedTime(maxOf(clock.now(), start))
                emit(elapsed)
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
        fun factory(gameRepository: GameCollectionRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    GameDetailsViewModel(gameRepository, createSavedStateHandle())
                }
            }
    }
}
