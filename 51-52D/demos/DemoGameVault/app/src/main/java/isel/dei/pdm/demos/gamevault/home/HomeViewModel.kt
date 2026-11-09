package isel.dei.pdm.demos.gamevault.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.Year
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the home screen. The view model is responsible for managing the state of the
 * screen and for interacting with the remaining parts of the application.
 *
 * The screen has two regions whose contents are computed from the collection: the games being
 * played and the summary of the user's accomplishments. Each one is published through its own
 * state, with its own state machine (Loading -> Loaded, or Error, from which it can be retried),
 * so that a failure in one of them does not prevent the other from being displayed.
 *
 * The collection is only observed while the states are being collected, and for [STOP_TIMEOUT]
 * afterward, so that observation is not restarted on configuration changes. When observation is
 * restarted (e.g. when the app returns from the background), the last published state is kept
 * until the new one is available, so that the screen does not flash the loading state.
 *
 * @param collectionRepo The repository where the user's collection of games is stored.
 * @param nowPlayingLimit The maximum number of games being played that are displayed.
 * @param currentYear Provides the current year, to which the summary's yearly figures refer. It
 * is read each time observation (re)starts, so that the summary moves on to the new year when the
 * app returns from the background.
 */
class HomeViewModel(
    private val collectionRepo: GameCollectionRepository,
    private val nowPlayingLimit: Int = DEFAULT_NOW_PLAYING_LIMIT,
    private val currentYear: () -> Year = { Year.now() }
) : ViewModel() {

    /**
     * The user's requests to retry loading the games being played.
     */
    private val nowPlayingRetries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The user's requests to retry computing the summary.
     */
    private val summaryRetries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The state of the "now playing" region of the screen.
     */
    val nowPlaying: StateFlow<NowPlayingState> = regionState(
        retries = nowPlayingRetries,
        loading = NowPlayingState.Loading,
        observe = {
            collectionRepo
                .observeItems(CollectionQuery(state = PlayStatus.State.PLAYING), nowPlayingLimit)
                .map { NowPlayingState.Loaded(it.items, it.hasMore) }
        },
        error = { NowPlayingState.Error(it) }
    )

    /**
     * The state of the summary region of the screen.
     */
    val summary: StateFlow<SummaryState> = regionState(
        retries = summaryRetries,
        loading = SummaryState.Loading,
        observe = {
            collectionRepo.observeSummary(currentYear()).map { SummaryState.Loaded(it) }
        },
        error = { SummaryState.Error(it) }
    )

    /**
     * Implementation of the state transition that results from the user retrying to load the
     * games being played after an error. The region moves to [NowPlayingState.Loading] and the
     * collection is observed again.
     * If the current state is not [NowPlayingState.Error], no action is taken.
     */
    fun onRetryNowPlaying() {
        if (nowPlaying.value is NowPlayingState.Error)
            nowPlayingRetries.tryEmit(Unit)
    }

    /**
     * Implementation of the state transition that results from the user retrying to compute the
     * summary after an error. The region moves to [SummaryState.Loading] and the collection is
     * observed again.
     * If the current state is not [SummaryState.Error], no action is taken.
     */
    fun onRetrySummary() {
        if (summary.value is SummaryState.Error)
            summaryRetries.tryEmit(Unit)
    }

    /**
     * Publishes the state of a region of the screen, computed by the given observation of the
     * collection. The observation is started with the region in the [loading] state, and moves
     * to the [error] state if the collection cannot be read, which ends it. Each retry request
     * restarts the observation, moving the region back to [loading].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <S> regionState(
        retries: Flow<Unit>,
        loading: S,
        observe: () -> Flow<S>,
        error: (PersistenceException) -> S
    ): StateFlow<S> =
        retries
            .map { true }
            // The first observation is not a retry: the region is already in the loading state,
            // or keeps its last state if observation is being restarted
            .onStart { emit(false) }
            .flatMapLatest { isRetry ->
                observe()
                    .onStart { if (isRetry) emit(loading) }
                    .catch { if (it is PersistenceException) emit(error(it)) else throw it }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
                initialValue = loading
            )

    companion object {
        /**
         * The default maximum number of games being played that are displayed. Few games are
         * expected to be in the PLAYING state at any time, as those without recent sessions are
         * meant to be paused.
         */
        const val DEFAULT_NOW_PLAYING_LIMIT = 20

        /**
         * How long the collection keeps being observed after the last collector stops, so that
         * observation survives configuration changes (e.g. rotation).
         */
        val STOP_TIMEOUT: Duration = 5.seconds

        /**
         * Factory for creating instances of [HomeViewModel] with the given dependencies.
         */
        fun factory(repository: GameCollectionRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HomeViewModel(repository) }
            }
    }
}
