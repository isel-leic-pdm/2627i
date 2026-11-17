package isel.dei.pdm.demos.gamevault.collection.games

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenState.Browsing
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenState.Error
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenState.Loading
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the games collection screen. The view model is responsible for managing the
 * state of the screen and for interacting with the remaining parts of the application.
 *
 * The collection is browsed with a [CollectionQuery], changed by the user as a whole (see
 * [onQueryChange]) and applied immediately. Its criteria are preserved in the [SavedStateHandle],
 * where the screen's initial query is also expected to be found (e.g. passed by the screens that
 * navigate to it, such as the home screen's shortcuts), under the keys defined in the companion
 * object. The initial (i.e. [CollectionQuery.initial]) is not a criterion of this screen, which
 * navigates the listing by initial instead (see [InitialsIndex]).
 *
 * The query's platform is preserved by its identifier, and resolved among the platforms of the
 * collection, which are observed. As filtering by a platform includes all its versions, it is
 * resolved to the platform offered as filter for all of them (see [distinctGamePlatforms]). A
 * platform that is not in the collection (e.g. removed after the filter was applied) is ignored.
 *
 * All the games that meet the query are read (i.e. without limit), as the listing is lazy and
 * navigable by initial, and a collection of a few thousand games is light enough to be held in
 * memory. While the games of a new query are being read, those of the previous one are kept
 * (see [Browsing.sections]).
 *
 * The collection is only observed while the state is being collected, and for [STOP_TIMEOUT]
 * afterward, so that observation is not restarted on configuration changes. When observation is
 * restarted (e.g. when the app returns from the background), the last games are kept until the
 * new ones are read.
 *
 * @param gameRepo The repository where the user's collection of games is stored.
 * @param platformRepo The repository of the user's collection of platforms, which the games can
 * be filtered by.
 * @param savedStateHandle The handle used to preserve the query, and from which the initial query
 * is read.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameCollectionViewModel(
    private val gameRepo: GameCollectionRepository,
    private val platformRepo: PlatformCollectionRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    /**
     * The criteria of the query, as preserved in the [SavedStateHandle]: the platform is
     * identified by its identifier, as the platforms are not known when the criteria are
     * restored (see [queryOf]).
     */
    private data class Criteria(
        val state: PlayStatus.State? = null,
        val completion: Completion? = null,
        val platformId: Long? = null,
        val nameContains: NonBlankString? = null
    )

    private val _criteria = MutableStateFlow(
        Criteria(
            state = savedStateHandle.get<String>(STATE_KEY)
                ?.let { name -> PlayStatus.State.entries.find { it.name == name } },
            completion = savedStateHandle.get<String>(COMPLETION_KEY)
                ?.let { name -> Completion.entries.find { it.name == name } },
            platformId = savedStateHandle.get<Long>(PLATFORM_KEY),
            nameContains = savedStateHandle.get<String>(NAME_KEY)
                ?.takeIf { it.isNotBlank() }
                ?.let { NonBlankString(it) }
        )
    )

    /**
     * What causes the collection to be observed: the start of the observation, whether the first
     * or a restart (e.g. when the app returns from the background), or the user retrying after
     * an error.
     */
    private enum class Trigger { Start, Retry }

    /**
     * The triggers of the observation of the collection. Not replayed, so that restarting the
     * observation is not taken for a retry.
     */
    private val triggers = MutableSharedFlow<Trigger>(extraBufferCapacity = 1)

    /**
     * The state of the screen.
     */
    val state: StateFlow<GameCollectionScreenState> = triggers
        .onStart { emit(Trigger.Start) }
        .flatMapLatest { trigger ->
            browse()
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
     * Implementation of the state transition that results from the user changing the query (e.g.
     * searching, adding or removing filters). The screen stays in [Browsing], with the new query,
     * and the games that meet it are read, replacing those of the previous query once read.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     * @param query The new query.
     */
    fun onQueryChange(query: CollectionQuery) {
        if (state.value is Error)
            return
        val criteria = Criteria(
            state = query.state,
            completion = query.completion,
            platformId = query.platform?.id,
            nameContains = query.nameContains
        )
        savedStateHandle[STATE_KEY] = criteria.state?.name
        savedStateHandle[COMPLETION_KEY] = criteria.completion?.name
        savedStateHandle[PLATFORM_KEY] = criteria.platformId
        savedStateHandle[NAME_KEY] = criteria.nameContains?.value
        _criteria.value = criteria
    }

    /**
     * Implementation of the state transition that results from the user retrying after an error.
     * The screen moves to [Loading] and the collection is observed again.
     * If the current state is not [Error], no action is taken. It relies on [state] being
     * collected, which is the case when invoked from the UI.
     */
    fun onRetry() {
        if (state.value is Error)
            triggers.tryEmit(Trigger.Retry)
    }

    /**
     * The states of the screen while browsing the collection: the query, with the platforms it
     * is resolved among, and the games that meet it, as they are read. Whenever the query or the
     * platforms change, the games are read again, the previous ones being kept until then.
     */
    private fun browse(): Flow<GameCollectionScreenState> {
        // The games kept if observation is being restarted, until the new ones are read
        val lastSections = (state.value as? Browsing)?.sections
        return combine(platformRepo.platforms, _criteria) { platforms, criteria ->
            val offered = platforms.distinctGamePlatforms()
            queryOf(criteria, platforms, offered) to offered
        }
            .distinctUntilChanged()
            .flatMapLatest { (query, platforms) ->
                gameRepo.observeItems(query, limit = Int.MAX_VALUE)
                    .map { Browsing(query, it.items.toSections(), platforms) }
                    .onStart { emit(Browsing(query, sections = null, platforms)) }
            }
            // While the games of the query are being read, the previous ones are kept
            .runningFold<Browsing, Browsing?>(null) { previous, next ->
                if (next.sections != null) next
                else next.copy(sections = previous?.sections ?: lastSections)
            }
            .filterNotNull()
    }

    /**
     * The query with the given criteria. The platform is resolved among the [platforms] of the
     * collection by its identifier, and then to the one [offered] as filter for all its versions.
     * If it is not among them, the query has no platform.
     */
    private fun queryOf(
        criteria: Criteria,
        platforms: List<Platform>,
        offered: List<Platform>
    ): CollectionQuery =
        CollectionQuery(
            state = criteria.state,
            completion = criteria.completion,
            platform = platforms.find { it.id == criteria.platformId }
                ?.let { selected -> offered.find { it.isSameGamePlatformAs(selected) } ?: selected },
            nameContains = criteria.nameContains
        )

    companion object {
        /**
         * The key used to preserve the name of the play state of the query in the
         * [SavedStateHandle].
         */
        const val STATE_KEY = "state"

        /**
         * The key used to preserve the name of the completion of the query in the
         * [SavedStateHandle].
         */
        const val COMPLETION_KEY = "completion"

        /**
         * The key used to preserve the identifier of the platform of the query in the
         * [SavedStateHandle].
         */
        const val PLATFORM_KEY = "platformId"

        /**
         * The key used to preserve the text searched for in the games' names in the
         * [SavedStateHandle].
         */
        const val NAME_KEY = "nameContains"

        /**
         * How long the collection keeps being observed after the last collector stops, so that
         * observation survives configuration changes (e.g. rotation).
         */
        val STOP_TIMEOUT: Duration = 5.seconds

        /**
         * Factory for creating instances of [GameCollectionViewModel] with the given dependencies.
         */
        fun factory(
            gameRepository: GameCollectionRepository,
            platformRepository: PlatformCollectionRepository
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    GameCollectionViewModel(gameRepository, platformRepository, createSavedStateHandle())
                }
            }
    }
}
