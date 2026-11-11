package isel.dei.pdm.demos.gamevault.search.platforms

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
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import isel.dei.pdm.demos.gamevault.domain.search.toSearchStringOrNull
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenState.Error
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenState.Results
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenState.Search
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenState.Start
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the search platforms screen. The view model is responsible for managing the
 * state of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports searching for existing platforms and adding them to the user's collection.
 * The search query is preserved in the [SavedStateHandle], so that the search is resumed if the
 * process is killed while the screen is in the background.
 *
 * @param searchService The service used to search for platforms.
 * @param collectionRepo The repository used to store the user's collection of platforms.
 * @param savedStateHandle The handle used to preserve the search query.
 * @param searchIndicatorDelay How long a search must take before the loading indicator is
 * displayed, so that it does not flash on screen when the search is almost instantaneous.
 */
class SearchForPlatformViewModel(
    private val searchService: PlatformSearchService,
    private val collectionRepo: PlatformCollectionRepository,
    private val savedStateHandle: SavedStateHandle,
    private val searchIndicatorDelay: Duration = DEFAULT_SEARCH_INDICATOR_DELAY
) : ViewModel() {

    /**
     * The state of the screen regarding the search. In [Results], all items are
     * [AddStatus.NOT_ADDED]: their actual status is computed when publishing [state].
     */
    private val _searchState = MutableStateFlow<SearchForPlatformScreenState>(Start)

    /**
     * The additions to the collection requested in this screen: [AddStatus.ADDING] while
     * in progress, and [AddStatus.ADDED] once completed. Completed additions are kept so
     * that the platform is displayed as added even before the collection reflects it. Failed
     * additions are removed.
     */
    private val _additions = MutableStateFlow<Map<PlatformInfo, AddStatus>>(emptyMap())

    /**
     * The add outcomes to be reported to the user, in the order they occurred.
     */
    private val _addOutcomes = MutableStateFlow<List<AddPlatformOutcome>>(emptyList())

    /**
     * The platforms in the user's collection. If they cannot be read, none of the search results
     * is displayed as added, which does not prevent the user from adding them.
     */
    private val collection = collectionRepo.platforms.catch { error ->
        if (error is PersistenceException) emit(emptyList()) else throw error
    }

    /**
     * The ongoing search, if any. Entering [Search] or [Start] cancels it.
     */
    private var searchJob: Job? = null

    /**
     * The state of the screen. The sources it is computed from (namely the collection, which may
     * be backed by a database query) are only observed while the state is being collected, and
     * for [STOP_TIMEOUT] afterward, so that observation is not restarted on configuration changes.
     */
    val state: StateFlow<SearchForPlatformScreenState> =
        combine(_searchState, collection, _additions) { searchState, collection, additions ->
            if (searchState !is Results) searchState
            else searchState.copy(
                items = searchState.items.map { item ->
                    item.copy(addStatus = addStatusOf(item.platform, collection, additions))
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = _searchState.value
        )

    /**
     * The add outcome to be reported to the user, if any. Once it has been reported, the view
     * model must be notified through [onAddOutcomeShown], so that the next one (if any) is published.
     */
    val addOutcome: StateFlow<AddPlatformOutcome?> = _addOutcomes
        .map { it.firstOrNull() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
            initialValue = null
        )

    init {
        // Resumes the search if the process was killed while the screen was in the background
        savedStateHandle.get<String>(QUERY_KEY)?.toSearchStringOrNull()?.let { startSearch(it) }
    }

    /**
     * Implementation of the state transitions that result from the user entering a new query.
     * The screen moves to [Search], cancelling the ongoing search, if any.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     * @param query The new search query.
     */
    fun onSearchQueryChange(query: SearchString) {
        if (_searchState.value is Error)
            return
        startSearch(query)
    }

    /**
     * Implementation of the state transitions that result from the user clearing the query.
     * The screen moves to [Start], cancelling the ongoing search, if any.
     * If the current state is [Error], no action is taken: the only way out of it is [onRetry].
     */
    fun onSearchQueryCleared() {
        if (_searchState.value is Error)
            return
        searchJob?.cancel()
        savedStateHandle.remove<String>(QUERY_KEY)
        _searchState.value = Start
    }

    /**
     * Implementation of the state transition that results from the user retrying a failed search.
     * The screen moves to [Search] with the same query.
     * If the current state is not [Error], no action is taken.
     */
    fun onRetry() {
        val state = _searchState.value
        if (state is Error)
            startSearch(state.query)
    }

    /**
     * Adds the given platform to the user's collection. The addition completes regardless of
     * subsequent state transitions. Its outcome is published in [addOutcome] if it fails, or if
     * it succeeds but the platform is no longer displayed.
     * If the current state is not [Results], or the platform is not one of the results that can
     * be added, no action is taken. It relies on [state] being collected, which is the case when
     * invoked from the UI.
     * @param platform The platform to add.
     */
    fun onAddPlatform(platform: PlatformInfo) {
        val state = state.value
        if (state !is Results)
            return
        val item = state.items.find { it.platform == platform }
        if (item == null || item.addStatus != AddStatus.NOT_ADDED)
            return
        // The published state may not yet reflect a previous request (e.g. a double tap)
        if (platform in _additions.value)
            return

        _additions.update { it + (platform to AddStatus.ADDING) }
        viewModelScope.launch {
            val outcome = try {
                collectionRepo.add(platform.toNewPlatform())
                _additions.update { it + (platform to AddStatus.ADDED) }
                AddPlatformOutcome.Added(platform)
            } catch (e: PersistenceException) {
                _additions.update { it - platform }
                AddPlatformOutcome.Failed(platform, e)
            }
            if (outcome is AddPlatformOutcome.Failed || !isDisplayed(platform))
                _addOutcomes.update { it + outcome }
        }
    }

    /**
     * Implementation of the state transition that results from the current add outcome having
     * been shown to the user. The next outcome, if any, is published.
     */
    fun onAddOutcomeShown() {
        _addOutcomes.update { it.drop(1) }
    }

    /**
     * Starts the search for the given query, cancelling the ongoing one, if any. The screen moves
     * to [Search] and then, depending on the outcome, to [Results] or to [Error].
     */
    private fun startSearch(query: SearchString) {
        searchJob?.cancel()
        savedStateHandle[QUERY_KEY] = query.value
        _searchState.value = Search(query)

        searchJob = viewModelScope.launch {
            // The loading indicator is only displayed if the search takes longer than the delay
            val indicatorTimer = launch {
                delay(searchIndicatorDelay)
                _searchState.value = Search(query, isIndicatorVisible = true)
            }
            val outcome = try {
                Results(query, searchService.search(query).map { PlatformSearchItemUiState(it) })
            } catch (e: SearchServiceException) {
                Error(query, e)
            }
            // The timer is canceled before publishing the outcome, so it cannot overwrite it
            indicatorTimer.cancel()
            _searchState.value = outcome
        }
    }

    /**
     * Checks whether the given platform is currently displayed in the search results.
     */
    private fun isDisplayed(platform: PlatformInfo): Boolean {
        val state = _searchState.value
        return state is Results && state.items.any { it.platform == platform }
    }

    companion object {
        /**
         * The key used to preserve the search query in the [SavedStateHandle].
         */
        const val QUERY_KEY = "query"

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
         * Factory for creating instances of [SearchForPlatformViewModel] with the given dependencies.
         */
        fun factory(
            searchService: PlatformSearchService,
            repository: PlatformCollectionRepository
        ): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    SearchForPlatformViewModel(searchService, repository, createSavedStateHandle())
                }
            }
    }
}

/**
 * Computes the add status of the given platform. The status of an addition requested in this
 * screen prevails. Otherwise, the platform is considered to be in the collection if the collection
 * holds a platform with the same name and version (ignoring case). Platforms in the collection are
 * not linked to the search results, because they can be added (and edited) by hand.
 */
private fun addStatusOf(
    platform: PlatformInfo,
    collection: List<Platform>,
    additions: Map<PlatformInfo, AddStatus>
): AddStatus =
    additions[platform]
        ?: if (collection.any { platform.matches(it) }) AddStatus.ADDED
        else AddStatus.NOT_ADDED

/**
 * Checks whether this platform and the given platform in the collection have the same name and
 * version, ignoring case.
 */
private fun PlatformInfo.matches(platform: Platform): Boolean =
    name.value.equals(platform.name.value, ignoreCase = true) &&
        version?.value.equals(platform.version?.value, ignoreCase = true)

/**
 * Converts this platform, as provided by the search service, to a platform to be added to the
 * user's collection, keeping a reference to its source.
 */
private fun PlatformInfo.toNewPlatform(): NewPlatform =
    NewPlatform(
        abbreviation = abbreviation,
        name = name,
        logoUri = logoUri,
        version = version,
        source = SourceReference(SourceReference.Provider.IGDB, NonBlankString(id.toString()))
    )
