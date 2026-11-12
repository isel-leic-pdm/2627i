package isel.dei.pdm.demos.gamevault.search.games

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.games.releasedFor
import isel.dei.pdm.demos.gamevault.domain.search.games.toSearchCriterion
import isel.dei.pdm.demos.gamevault.domain.search.toSearchStringOrNull
import isel.dei.pdm.demos.gamevault.images.ImageStore
import isel.dei.pdm.demos.gamevault.images.storeOrKeep
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.Error
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.Loading
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.NoPlatforms
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.PlatformsUnavailable
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.Results
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.Search
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenState.Start
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the search games screen. The view model is responsible for managing the
 * state of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports searching for games, on the platforms of the user's collection, and adding
 * them to the collection, on one of those platforms. The search query and filters are preserved in
 * the [SavedStateHandle], so that the search is resumed if the process is killed while the screen
 * is in the background.
 *
 * Games are oblivious of the platforms' versions (see [Platform.isSameGamePlatformAs]), so games are
 * searched for each platform of the collection once, regardless of how many of its versions the
 * user owns.
 *
 * Both reading the platforms and searching are modelled the same way: as a flow of requests, each
 * one producing the flow of its outcomes. A new request (e.g. a retry, a new query or new filters)
 * replaces the ongoing one, which is cancelled. They are shared differently, though:
 * - Reading the platforms is an observation, only needed while the state is being collected.
 * - Searching is an operation whose outcome must survive the UI going away for a while (e.g. to
 * the background), so it is not restarted when the UI comes back. Its requests are therefore
 * processed as soon as they are made, regardless of the state being collected.
 *
 * @param searchService The service used to search for games.
 * @param gameRepo The repository used to store the user's collection of games.
 * @param platformRepo The repository of the user's collection of platforms, which games are
 * searched for (and added on).
 * @param imageStore The store where the cover and thumbnail of the added games are copied to, so
 * that they do not depend on the network.
 * @param savedStateHandle The handle used to preserve the search query and filters.
 * @param searchIndicatorDelay How long a search must take before the loading indicator is
 * displayed, so that it does not flash on screen when the search is almost instantaneous.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchForGamesViewModel(
    private val searchService: GameSearchService,
    private val gameRepo: GameCollectionRepository,
    platformRepo: PlatformCollectionRepository,
    private val imageStore: ImageStore,
    private val savedStateHandle: SavedStateHandle,
    private val searchIndicatorDelay: Duration = DEFAULT_SEARCH_INDICATOR_DELAY
) : ViewModel() {

    /**
     * The outcome of reading the platforms of the user's collection.
     */
    private sealed interface PlatformsRead {
        data object Reading : PlatformsRead
        data class Read(val platforms: List<Platform>) : PlatformsRead
        data class Failed(val error: PersistenceException) : PlatformsRead
    }

    /**
     * A request to search for the given query. Retrying produces a new request for the same
     * query, distinguished by its [attempt], so that it replaces the failed one.
     */
    private data class SearchRequest(val query: SearchString, val attempt: Int = 0)

    /**
     * The phase of the search, which, combined with the platforms and the selected filters,
     * determines the state of the screen.
     */
    private sealed interface SearchPhase {
        data object Idle : SearchPhase
        data class Searching(
            val query: SearchString,
            val isIndicatorVisible: Boolean = false
        ) : SearchPhase

        /**
         * The search found the given games. The entries of the games that were already in the
         * collection when the search completed are kept, indexed by the found games' identifiers,
         * to determine the platforms they are owned on.
         */
        data class Found(
            val query: SearchString,
            val games: List<GameInfo>,
            val entries: Map<Long, CollectionEntry>
        ) : SearchPhase

        data class Failed(val query: SearchString, val error: SearchServiceException) : SearchPhase
    }

    /**
     * The filters selected by the user. The platform is identified by its identifier, because the
     * platforms may change (e.g. if they are edited) and are not known when the selection is
     * restored from the [SavedStateHandle].
     */
    private data class Selection(
        val platformId: Long? = null,
        val category: GameInfo.Category? = null
    )

    /**
     * Identifies a search result: a game on one of the platforms of the user's collection.
     */
    private data class ItemKey(val gameId: Long, val platformId: Long)

    /**
     * The requests to read the platforms: incremented to read them again, after reading them
     * failed.
     */
    private val platformsReadAttempt = MutableStateFlow(0)

    /**
     * The platforms of the user's collection games can be searched for (see
     * [distinctGamePlatforms]), shared by the [state] and the searches waiting for them. They are
     * only observed while either of them is, without timeout: the [state]'s timeout already keeps
     * them observed on configuration changes, and adding up both timeouts would make the
     * observation last longer than intended.
     */
    private val platforms: StateFlow<PlatformsRead> = platformsReadAttempt
        .flatMapLatest {
            platformRepo.platforms
                .map<List<Platform>, PlatformsRead> { PlatformsRead.Read(it.distinctGamePlatforms()) }
                .onStart { emit(PlatformsRead.Reading) }
                .catch { error ->
                    if (error is PersistenceException) emit(PlatformsRead.Failed(error)) else throw error
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = PlatformsRead.Reading
        )

    private val _selection = MutableStateFlow(
        Selection(
            platformId = savedStateHandle.get<Long>(PLATFORM_KEY),
            category = savedStateHandle.get<String>(CATEGORY_KEY)
                ?.let { name -> GameInfo.Category.entries.find { it.name == name } }
        )
    )

    /**
     * The current search request, or null if there is no query. Initialized with the query
     * preserved in the [SavedStateHandle], if any, so that the search is resumed if the process was
     * killed while the screen was in the background.
     */
    private val _request = MutableStateFlow(
        savedStateHandle.get<String>(QUERY_KEY)?.toSearchStringOrNull()?.let { SearchRequest(it) }
    )

    /**
     * The phase of the search for the current request, with the selected filters. Whenever
     * either of them changes, the ongoing search, if any, is cancelled and replaced by a new one.
     */
    private val searchPhase: StateFlow<SearchPhase> =
        combine(_request, _selection) { request, selection -> request to selection }
            .flatMapLatest { (request, selection) ->
                if (request == null) flowOf(SearchPhase.Idle) else search(request.query, selection)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SearchPhase.Idle
            )

    /**
     * The additions to the collection requested in this screen: [AddStatus.ADDING] while in
     * progress, and [AddStatus.ADDED] once completed. Completed additions are kept so that the
     * game is displayed as added on the platform even if the search is repeated. Failed additions
     * are removed.
     */
    private val _additions = MutableStateFlow<Map<ItemKey, AddStatus>>(emptyMap())

    /**
     * The add outcomes to be reported to the user, in the order they occurred.
     */
    private val _addOutcomes = MutableStateFlow<List<AddGameOutcome>>(emptyList())

    /**
     * The state of the screen. The sources it is computed from (namely the platforms of the
     * collection, which may be backed by a database query) are only observed while the state is
     * being collected, and for [STOP_TIMEOUT] afterward, so that observation is not restarted on
     * configuration changes.
     */
    val state: StateFlow<SearchForGamesScreenState> =
        combine(platforms, _selection, searchPhase, _additions) { platforms, selection, search, additions ->
            when (platforms) {
                is PlatformsRead.Reading -> Loading
                is PlatformsRead.Failed -> PlatformsUnavailable(platforms.error)
                is PlatformsRead.Read -> {
                    val filters = filtersOf(platforms.platforms, selection)
                    if (filters == null) NoPlatforms else search.toScreenState(filters, additions)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = Loading
        )

    /**
     * The add outcome to be reported to the user, if any. Once it has been reported, the view
     * model must be notified through [onAddOutcomeShown], so that the next one (if any) is published.
     */
    val addOutcome: StateFlow<AddGameOutcome?> = _addOutcomes
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )

    /**
     * Implementation of the state transitions that result from the user entering a new query.
     * The screen moves to [Search], cancelling the ongoing search, if any.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     * @param query The new search query.
     */
    fun onSearchQueryChange(query: SearchString) {
        if (searchPhase.value is SearchPhase.Failed)
            return
        savedStateHandle[QUERY_KEY] = query.value
        _request.value = SearchRequest(query)
    }

    /**
     * Implementation of the state transitions that result from the user clearing the query.
     * The screen moves to [Start], cancelling the ongoing search, if any.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     */
    fun onSearchQueryCleared() {
        if (searchPhase.value is SearchPhase.Failed)
            return
        savedStateHandle.remove<String>(QUERY_KEY)
        _request.value = null
    }

    /**
     * Implementation of the state transitions that result from the user selecting another
     * platform to search games for, or null for all platforms. If there is a query, it is searched
     * for again, with the new filters.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     * @param platform The selected platform, or null for all platforms.
     */
    fun onPlatformSelected(platform: Platform?) {
        if (searchPhase.value is SearchPhase.Failed)
            return
        savedStateHandle[PLATFORM_KEY] = platform?.id
        _selection.update { it.copy(platformId = platform?.id) }
    }

    /**
     * Implementation of the state transitions that result from the user selecting another
     * category, or null for any category. If there is a query, it is searched for again, with the
     * new filters.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     * @param category The selected category, or null for any category.
     */
    fun onCategorySelected(category: GameInfo.Category?) {
        if (searchPhase.value is SearchPhase.Failed)
            return
        savedStateHandle[CATEGORY_KEY] = category?.name
        _selection.update { it.copy(category = category) }
    }

    /**
     * Implementation of the state transitions that result from the user retrying after a failure.
     * In [PlatformsUnavailable], the platforms are read again, moving to [Loading]. In [Error],
     * the screen moves to [Search] with the same query. Otherwise, no action is taken.
     * It relies on [state] being collected, which is the case when invoked from the UI.
     */
    fun onRetry() {
        when {
            platforms.value is PlatformsRead.Failed -> platformsReadAttempt.update { it + 1 }
            searchPhase.value is SearchPhase.Failed ->
                _request.update { it?.copy(attempt = it.attempt + 1) }
        }
    }

    /**
     * Adds the given game to the user's collection, on the given platform. The addition completes
     * regardless of subsequent state transitions. Its outcome is published in [addOutcome] if it
     * fails, or if it succeeds but the game is no longer displayed.
     * If the current state is not [Results], or the game on the platform is not one of the results
     * that can be added, no action is taken. It relies on [state] being collected, which is the
     * case when invoked from the UI.
     * @param game The game to add.
     * @param platform The platform the game is owned on.
     */
    fun onAddGame(game: GameInfo, platform: Platform) {
        val state = state.value
        if (state !is Results)
            return
        val item = state.items.find { it.game == game && it.platform == platform }
        if (item == null || item.addStatus != AddStatus.NOT_ADDED)
            return
        // The published state may not yet reflect a previous request (e.g. a double tap)
        val key = ItemKey(game.id, platform.id)
        if (key in _additions.value)
            return

        _additions.update { it + (key to AddStatus.ADDING) }
        viewModelScope.launch {
            val outcome = try {
                // Whatever the result (added, platform added to the existing entry, or already
                // owned on the platform), the game ends up owned on the platform
                gameRepo.add(game.toNewGame().withLocalImages(), platform)
                _additions.update { it + (key to AddStatus.ADDED) }
                AddGameOutcome.Added(game, platform)
            } catch (e: PersistenceException) {
                _additions.update { it - key }
                AddGameOutcome.Failed(game, platform, e)
            }
            if (outcome is AddGameOutcome.Failed || !isDisplayed(game))
                _addOutcomes.update { it + outcome }
        }
    }

    /**
     * This game, with local copies of its images instead of the remote ones. Images that cannot
     * be copied (e.g. without connectivity) are kept remote. If the game is already in the
     * collection, the copies already exist, as they are identified by their source, so they are
     * not downloaded again.
     */
    private suspend fun NewGame.withLocalImages(): NewGame =
        copy(
            coverUri = imageStore.storeOrKeep(coverUri),
            thumbnailUri = imageStore.storeOrKeep(thumbnailUri)
        )

    /**
     * Implementation of the state transition that results from the current add outcome having
     * been shown to the user. The next outcome, if any, is published.
     */
    fun onAddOutcomeShown() {
        _addOutcomes.update { it.drop(1) }
    }

    /**
     * The phases of the search for the given query, with the given selection: [SearchPhase.Searching]
     * and then, depending on the outcome, [SearchPhase.Found] or [SearchPhase.Failed]. If the
     * platforms of the collection are not known yet (e.g. when the search is resumed), the search
     * waits for them. Cancelling the collection of the flow cancels the search.
     */
    private fun search(query: SearchString, selection: Selection): Flow<SearchPhase> = channelFlow {
        send(SearchPhase.Searching(query))
        // The loading indicator is only displayed if the search takes longer than the delay
        val indicatorTimer = launch {
            delay(searchIndicatorDelay)
            send(SearchPhase.Searching(query, isIndicatorVisible = true))
        }
        val filters = filtersFor(selection)
        val outcome = try {
            val games = searchService.searchGames(
                name = query,
                platforms = filters.searchedPlatforms.map { it.toSearchCriterion() },
                category = filters.category
            )
            SearchPhase.Found(query, games, entriesOf(games))
        } catch (e: SearchServiceException) {
            SearchPhase.Failed(query, e)
        }
        // The timer is canceled before sending the outcome, so it cannot overwrite it
        indicatorTimer.cancel()
        send(outcome)
    }

    /**
     * The filters for the given selection, waiting for the platforms of the collection to be read,
     * if needed.
     */
    private suspend fun filtersFor(selection: Selection): GameSearchFilters =
        platforms.filterIsInstance<PlatformsRead.Read>()
            .mapNotNull { filtersOf(it.platforms, selection) }
            .first()

    /**
     * The entries of the given games that are already in the collection, indexed by the games'
     * identifiers. If the collection cannot be read, none of the games is considered to be in it,
     * which does not prevent the user from adding them.
     */
    private suspend fun entriesOf(games: List<GameInfo>): Map<Long, CollectionEntry> =
        try {
            games.mapNotNull { game -> gameRepo.findSameEntry(game.toNewGame())?.let { game.id to it } }
                .toMap()
        } catch (_: PersistenceException) {
            emptyMap()
        }

    /**
     * Checks whether the given game is currently displayed in the search results (on any of its
     * platforms).
     */
    private fun isDisplayed(game: GameInfo): Boolean {
        val search = searchPhase.value
        return search is SearchPhase.Found && search.games.any { it.id == game.id }
    }

    /**
     * The state of the screen in this phase of the search, with the given filters.
     */
    private fun SearchPhase.toScreenState(
        filters: GameSearchFilters,
        additions: Map<ItemKey, AddStatus>
    ): SearchForGamesScreenState =
        when (this) {
            is SearchPhase.Idle -> Start(filters)
            is SearchPhase.Searching -> Search(query, filters, isIndicatorVisible)
            is SearchPhase.Found -> Results(
                query = query,
                filters = filters,
                items = games.flatMap { game ->
                    game.releasedFor(filters.searchedPlatforms).map { platform ->
                        GameSearchItemUiState(
                            game = game,
                            platform = platform,
                            addStatus = addStatusOf(game, platform, entries[game.id], additions)
                        )
                    }
                }
            )
            is SearchPhase.Failed -> Error(query, filters, error)
        }

    /**
     * Computes the add status of the given game on the given platform. The status of an addition
     * requested in this screen prevails. Otherwise, the game is added if its entry in the
     * collection, if any, is owned on the platform (regardless of its version).
     */
    private fun addStatusOf(
        game: GameInfo,
        platform: Platform,
        entry: CollectionEntry?,
        additions: Map<ItemKey, AddStatus>
    ): AddStatus =
        additions[ItemKey(game.id, platform.id)]
            ?: if (entry?.isOwnedOn(platform) == true) AddStatus.ADDED
            else AddStatus.NOT_ADDED

    /**
     * The filters for the given platforms and selection, or null if there are no platforms. A
     * selected platform that is not among the platforms (e.g. removed from the collection) is
     * ignored, i.e. all platforms are selected.
     */
    private fun filtersOf(platforms: List<Platform>, selection: Selection): GameSearchFilters? =
        if (platforms.isEmpty()) null
        else GameSearchFilters(
            platforms = platforms,
            selectedPlatform = platforms.find { it.id == selection.platformId },
            category = selection.category
        )

    companion object {
        /**
         * The key used to preserve the search query in the [SavedStateHandle].
         */
        const val QUERY_KEY = "query"

        /**
         * The key used to preserve the identifier of the selected platform in the
         * [SavedStateHandle].
         */
        const val PLATFORM_KEY = "platformId"

        /**
         * The key used to preserve the name of the selected category in the [SavedStateHandle].
         */
        const val CATEGORY_KEY = "category"

        /**
         * The default time a search must take before the loading indicator is displayed.
         */
        val DEFAULT_SEARCH_INDICATOR_DELAY: Duration = 300.milliseconds

        /**
         * How long the sources of the published states keep being observed after the last
         * collector stops, so that observation survives configuration changes (e.g. rotation).
         */
        val STOP_TIMEOUT: Duration = 5.seconds

        /**
         * Factory for creating instances of [SearchForGamesViewModel] with the given dependencies.
         */
        fun factory(
            searchService: GameSearchService,
            gameRepository: GameCollectionRepository,
            platformRepository: PlatformCollectionRepository,
            imageStore: ImageStore
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SearchForGamesViewModel(
                        searchService,
                        gameRepository,
                        platformRepository,
                        imageStore,
                        createSavedStateHandle()
                    )
                }
            }
    }
}

/**
 * Converts this game, as provided by the search service, to a game to be added to the user's
 * collection, keeping a reference to its source (the app's single search provider, see
 * [GameSearchService]).
 */
private fun GameInfo.toNewGame(): NewGame =
    NewGame(
        name = name,
        releaseDate = releaseDate,
        coverUri = coverUri,
        thumbnailUri = thumbnailUri,
        source = SourceReference(SourceReference.Provider.IGDB, NonBlankString(id.toString()))
    )
