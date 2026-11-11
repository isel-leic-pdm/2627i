package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.lifecycle.SavedStateHandle
import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference.Provider.IGDB
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformViewModel.Companion.DEFAULT_SEARCH_INDICATOR_DELAY
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformViewModel.Companion.QUERY_KEY
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformViewModel.Companion.STOP_TIMEOUT
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchForPlatformViewModelTest {

    private companion object {
        val PS5_QUERY = SearchString("PS5")
        val SWITCH_QUERY = SearchString("Switch")
        val PLAYSTATION_QUERY = SearchString("PlayStation")
        val PS5 = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Standard")
        val PS5_PRO = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Pro")
        val SWITCH = PlatformInfo(7L, "NSW", "Nintendo Switch", "https://fake.url/switch.png")
        val SWITCH_OLED = PlatformInfo(7L, "NSW", "Nintendo Switch", "https://fake.url/switch.png", "OLED")
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchService = ControlledPlatformSearchService()
    private val repository = ControlledPlatformCollectionRepository()

    /**
     * Collects the states published by the view model until the test ends, as the UI does. The
     * published states are only kept up to date while they are being collected.
     * @return The job of the collector, which can be cancelled to simulate the UI going away.
     */
    private fun TestScope.collectStates(viewModel: SearchForPlatformViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.state.collect {} }
            launch { viewModel.addOutcome.collect {} }
        }

    /**
     * Creates a view model whose published states are being collected.
     */
    private fun TestScope.createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        collectionRepo: PlatformCollectionRepository = repository
    ): SearchForPlatformViewModel {
        val viewModel = SearchForPlatformViewModel(searchService, collectionRepo, savedStateHandle)
        collectStates(viewModel)
        return viewModel
    }

    /**
     * Creates a view model whose screen is displaying the results of searching for PS5.
     */
    private fun TestScope.createViewModelWithPs5Results(): SearchForPlatformViewModel {
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)
        searchService.complete(PS5_QUERY, listOf(PS5, PS5_PRO))
        return viewModel
    }

    /**
     * Creates a view model whose screen is displaying the failure of searching for PS5.
     */
    private fun TestScope.createViewModelWithPs5Error(): SearchForPlatformViewModel {
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)
        searchService.fail(PS5_QUERY, NoConnectivityException())
        return viewModel
    }

    /**
     * Asserts that the current add outcome is a failure for the given platform. The error instance
     * is not compared, because coroutines may replace it with a copy when it crosses coroutine
     * boundaries (stack trace recovery, enabled in debug mode).
     */
    private fun SearchForPlatformViewModel.assertFailedOutcomeFor(platform: PlatformInfo) {
        val outcome = addOutcome.value
        assertIs<AddPlatformOutcome.Failed>(outcome)
        assertEquals(platform, outcome.platform)
        assertIs<UnrecoverablePersistenceException>(outcome.error)
    }

    private fun SearchForPlatformViewModel.statusOf(platform: PlatformInfo): AddStatus {
        val state = state.value
        assertIs<SearchForPlatformScreenState.Results>(state)
        return state.items.first { it.platform == platform }.addStatus
    }

    // Start

    @Test
    fun viewmodel_initializes_in_start_state() = runTest {
        // Act
        val viewModel = createViewModel()

        // Assert
        assertEquals(SearchForPlatformScreenState.Start, viewModel.state.value)
        assertNull(viewModel.addOutcome.value)
        assertTrue(searchService.requestedQueries.isEmpty())
    }

    @Test
    fun viewmodel_resumes_search_for_the_query_preserved_in_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle(mapOf(QUERY_KEY to PS5_QUERY.value))

        // Act
        val viewModel = createViewModel(savedStateHandle)

        // Assert
        assertEquals(SearchForPlatformScreenState.Search(PS5_QUERY), viewModel.state.value)
        assertEquals(listOf(PS5_QUERY), searchService.requestedQueries)
    }

    @Test
    fun viewmodel_ignores_an_invalid_query_preserved_in_saved_state() = runTest {
        // Arrange - Too short to be a SearchString
        val savedStateHandle = SavedStateHandle(mapOf(QUERY_KEY to "P"))

        // Act
        val viewModel = createViewModel(savedStateHandle)

        // Assert
        assertEquals(SearchForPlatformScreenState.Start, viewModel.state.value)
        assertTrue(searchService.requestedQueries.isEmpty())
    }

    // Observation of the sources

    @Test
    fun collection_is_only_observed_while_the_state_is_collected() = runTest {
        // Arrange
        val viewModel = SearchForPlatformViewModel(searchService, repository, SavedStateHandle())
        assertEquals(0, repository.observers)

        // Act & Assert - The UI starts collecting
        val collector = collectStates(viewModel)
        assertEquals(1, repository.observers)

        // Act & Assert - The UI stops collecting (e.g. the app goes to the background)
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        runCurrent()
        assertEquals(0, repository.observers)
    }

    @Test
    fun collection_observation_survives_short_interruptions() = runTest {
        // Arrange
        val viewModel = SearchForPlatformViewModel(searchService, repository, SavedStateHandle())
        val collector = collectStates(viewModel)

        // Act - The UI stops collecting and resumes shortly after (e.g. rotation)
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT - 1.milliseconds)
        runCurrent()
        assertEquals(1, repository.observers)
        collectStates(viewModel)
        advanceTimeBy(STOP_TIMEOUT * 2)
        runCurrent()

        // Assert - The collection is still observed, by a single collector
        assertEquals(1, repository.observers)
    }

    // Search

    @Test
    fun new_query_transitions_to_search_state_without_indicator() = runTest {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Assert
        assertEquals(SearchForPlatformScreenState.Search(PS5_QUERY), viewModel.state.value)
        assertEquals(listOf(PS5_QUERY), searchService.requestedQueries)
    }

    @Test
    fun new_query_is_preserved_in_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)

        // Act
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Assert
        assertEquals(PS5_QUERY.value, savedStateHandle.get<String>(QUERY_KEY))
    }

    @Test
    fun search_indicator_is_displayed_once_the_delay_elapses() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act & Assert
        advanceTimeBy(DEFAULT_SEARCH_INDICATOR_DELAY - 1.milliseconds)
        runCurrent()
        assertEquals(SearchForPlatformScreenState.Search(PS5_QUERY), viewModel.state.value)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(
            SearchForPlatformScreenState.Search(PS5_QUERY, isIndicatorVisible = true),
            viewModel.state.value
        )
    }

    @Test
    fun results_arrival_transitions_to_results_state() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        searchService.complete(PS5_QUERY, listOf(PS5, PS5_PRO))

        // Assert
        val state = viewModel.state.value
        assertIs<SearchForPlatformScreenState.Results>(state)
        assertEquals(PS5_QUERY, state.query)
        assertEquals(listOf(PS5, PS5_PRO), state.items.map { it.platform })
        assertTrue(state.items.all { it.addStatus == AddStatus.NOT_ADDED })
    }

    @Test
    fun results_arriving_after_the_indicator_delay_replace_the_indicator() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)
        advanceTimeBy(DEFAULT_SEARCH_INDICATOR_DELAY * 2)
        runCurrent()

        // Act
        searchService.complete(PS5_QUERY, emptyList())
        advanceTimeBy(DEFAULT_SEARCH_INDICATOR_DELAY * 2)
        runCurrent()

        // Assert
        assertEquals(
            SearchForPlatformScreenState.Results(PS5_QUERY, emptyList()),
            viewModel.state.value
        )
    }

    @Test
    fun search_failure_transitions_to_error_state() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        searchService.fail(PS5_QUERY, NoConnectivityException())

        // Assert
        val state = viewModel.state.value
        assertIs<SearchForPlatformScreenState.Error>(state)
        assertEquals(PS5_QUERY, state.query)
        assertIs<NoConnectivityException>(state.error)
    }

    @Test
    fun new_query_while_searching_cancels_the_ongoing_search() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        viewModel.onSearchQueryChange(SWITCH_QUERY)
        runCurrent()
        searchService.complete(PS5_QUERY, listOf(PS5))

        // Assert - The request itself is cancelled, not just its outcome ignored
        assertEquals(listOf(PS5_QUERY), searchService.cancelledQueries)
        assertEquals(SearchForPlatformScreenState.Search(SWITCH_QUERY), viewModel.state.value)

        searchService.complete(SWITCH_QUERY, listOf(SWITCH))
        assertEquals(
            SearchForPlatformScreenState.Results(SWITCH_QUERY, listOf(PlatformSearchItemUiState(SWITCH))),
            viewModel.state.value
        )
    }

    @Test
    fun clearing_query_while_searching_cancels_the_search_and_transitions_to_start() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        viewModel.onSearchQueryCleared()
        runCurrent()
        searchService.complete(PS5_QUERY, listOf(PS5))

        // Assert
        assertEquals(listOf(PS5_QUERY), searchService.cancelledQueries)
        assertEquals(SearchForPlatformScreenState.Start, viewModel.state.value)
        assertNull(savedStateHandle.get<String>(QUERY_KEY))
    }

    @Test
    fun indicator_of_a_cancelled_search_is_not_displayed() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        viewModel.onSearchQueryCleared()
        advanceTimeBy(DEFAULT_SEARCH_INDICATOR_DELAY * 2)
        runCurrent()

        // Assert
        assertEquals(SearchForPlatformScreenState.Start, viewModel.state.value)
    }

    // Results

    @Test
    fun new_query_from_results_transitions_to_search_state() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()

        // Act
        viewModel.onSearchQueryChange(SWITCH_QUERY)

        // Assert
        assertEquals(SearchForPlatformScreenState.Search(SWITCH_QUERY), viewModel.state.value)
    }

    @Test
    fun clearing_query_from_results_transitions_to_start_state() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()

        // Act
        viewModel.onSearchQueryCleared()

        // Assert
        assertEquals(SearchForPlatformScreenState.Start, viewModel.state.value)
    }

    // Error

    @Test
    fun retry_from_error_searches_again_for_the_same_query() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Error()

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(SearchForPlatformScreenState.Search(PS5_QUERY), viewModel.state.value)
        assertEquals(listOf(PS5_QUERY, PS5_QUERY), searchService.requestedQueries)

        searchService.complete(PS5_QUERY, listOf(PS5))
        assertIs<SearchForPlatformScreenState.Results>(viewModel.state.value)
    }

    @Test
    fun new_query_in_error_state_has_no_effect() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onSearchQueryChange(PS5_QUERY)
        searchService.fail(PS5_QUERY, NoConnectivityException())
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onSearchQueryChange(SWITCH_QUERY)

        // Assert
        assertEquals(stateBefore, viewModel.state.value)
        assertEquals(listOf(PS5_QUERY), searchService.requestedQueries)
        assertEquals(PS5_QUERY.value, savedStateHandle.get<String>(QUERY_KEY))
    }

    @Test
    fun clearing_query_in_error_state_has_no_effect() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onSearchQueryChange(PS5_QUERY)
        searchService.fail(PS5_QUERY, NoConnectivityException())
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onSearchQueryCleared()

        // Assert - The query is kept, so that the search can still be retried
        assertEquals(stateBefore, viewModel.state.value)
        assertEquals(PS5_QUERY.value, savedStateHandle.get<String>(QUERY_KEY))
    }

    @Test
    fun retry_when_not_in_error_state_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        val stateBefore = viewModel.state.value

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(stateBefore, viewModel.state.value)
        assertEquals(listOf(PS5_QUERY), searchService.requestedQueries)
    }

    // Add status

    @Test
    fun results_already_in_the_collection_are_marked_as_added() = runTest {
        // Arrange - Same name and version as PS5_PRO, differing in case only
        repository.preload(Platform(1L, "PS5", "playstation 5", ownedSince = null, version = "PRO"))
        val viewModel = createViewModelWithPs5Results()

        // Assert
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(PS5))
        assertEquals(AddStatus.ADDED, viewModel.statusOf(PS5_PRO))
    }

    @Test
    fun result_without_version_matches_collection_entry_without_version() = runTest {
        // Arrange
        repository.preload(Platform(1L, "NSW", "Nintendo Switch"))
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(SWITCH_QUERY)

        // Act
        searchService.complete(SWITCH_QUERY, listOf(SWITCH, SWITCH_OLED))

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(SWITCH))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(SWITCH_OLED))
    }

    @Test
    fun result_without_version_does_not_match_collection_entry_with_version() = runTest {
        // Arrange
        repository.preload(Platform(1L, "NSW", "Nintendo Switch", version = "OLED"))
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(SWITCH_QUERY)

        // Act
        searchService.complete(SWITCH_QUERY, listOf(SWITCH, SWITCH_OLED))

        // Assert
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(SWITCH))
        assertEquals(AddStatus.ADDED, viewModel.statusOf(SWITCH_OLED))
    }

    @Test
    fun results_are_marked_as_added_when_the_collection_changes() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()

        // Act - e.g. added in another screen
        repository.preload(Platform(1L, "PS5", "PlayStation 5", version = "Standard"))

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(PS5))
    }

    @Test
    fun results_are_not_marked_as_added_when_the_collection_cannot_be_read() = runTest {
        // Arrange
        val failingRepository = ControlledPlatformCollectionRepository(
            readError = UnrecoverablePersistenceException()
        )
        val viewModel = createViewModel(collectionRepo = failingRepository)
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        searchService.complete(PS5_QUERY, listOf(PS5))

        // Assert
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(PS5))
    }

    // Adding platforms

    @Test
    fun adding_platform_marks_it_as_adding() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()

        // Act
        viewModel.onAddPlatform(PS5_PRO)

        // Assert
        assertEquals(AddStatus.ADDING, viewModel.statusOf(PS5_PRO))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(PS5))
    }

    @Test
    fun successful_addition_marks_platform_as_added_without_reporting_it() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)

        // Act
        repository.completeAdd()

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(PS5_PRO))
        assertNull(viewModel.addOutcome.value)
        val added = repository.platforms.first().single()
        assertEquals(PS5_PRO.name, added.name)
        assertEquals(PS5_PRO.abbreviation, added.abbreviation)
        assertEquals(PS5_PRO.version, added.version)
        assertEquals(PS5_PRO.logoUri, added.logoUri)
        assertEquals(SourceReference(IGDB, NonBlankString("5")), added.source)
    }

    @Test
    fun failed_addition_marks_platform_as_not_added_and_reports_it() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)

        // Act
        repository.failAdd(UnrecoverablePersistenceException())

        // Assert
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(PS5_PRO))
        viewModel.assertFailedOutcomeFor(PS5_PRO)
    }

    @Test
    fun addition_completes_and_is_reported_when_the_search_changes_meanwhile() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)
        viewModel.onSearchQueryChange(SWITCH_QUERY)
        searchService.complete(SWITCH_QUERY, listOf(SWITCH))

        // Act
        repository.completeAdd()

        // Assert
        assertEquals(AddPlatformOutcome.Added(PS5_PRO), viewModel.addOutcome.value)
        assertEquals(1, repository.platforms.first().size)
    }

    @Test
    fun addition_completes_and_is_reported_when_the_query_is_cleared_meanwhile() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)
        viewModel.onSearchQueryCleared()

        // Act
        repository.completeAdd()

        // Assert
        assertEquals(AddPlatformOutcome.Added(PS5_PRO), viewModel.addOutcome.value)
    }

    @Test
    fun addition_is_not_reported_when_the_new_search_still_displays_the_platform() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)
        viewModel.onSearchQueryChange(PLAYSTATION_QUERY)
        searchService.complete(PLAYSTATION_QUERY, listOf(PS5, PS5_PRO))

        // Act
        repository.completeAdd()

        // Assert - The user can see it was added, so there is nothing to report
        assertNull(viewModel.addOutcome.value)
        assertEquals(AddStatus.ADDED, viewModel.statusOf(PS5_PRO))
    }

    @Test
    fun added_platform_is_displayed_as_added_when_it_reappears_in_the_results() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)
        repository.completeAdd()

        // Act
        viewModel.onSearchQueryChange(SWITCH_QUERY)
        viewModel.onSearchQueryChange(PS5_QUERY)
        searchService.complete(PS5_QUERY, listOf(PS5, PS5_PRO))

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(PS5_PRO))
    }

    @Test
    fun outcomes_are_reported_one_at_a_time_in_the_order_they_occurred() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5)
        viewModel.onAddPlatform(PS5_PRO)
        repository.failAdd(UnrecoverablePersistenceException())
        repository.failAdd(UnrecoverablePersistenceException())

        // Act & Assert
        viewModel.assertFailedOutcomeFor(PS5)
        viewModel.onAddOutcomeShown()
        viewModel.assertFailedOutcomeFor(PS5_PRO)
        viewModel.onAddOutcomeShown()
        assertNull(viewModel.addOutcome.value)
    }

    @Test
    fun adding_platform_that_is_being_added_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()
        viewModel.onAddPlatform(PS5_PRO)

        // Act
        viewModel.onAddPlatform(PS5_PRO)

        // Assert
        assertEquals(1, repository.pendingAdds)
    }

    @Test
    fun adding_platform_that_is_already_added_has_no_effect() = runTest {
        // Arrange
        repository.preload(Platform(1L, "PS5", "PlayStation 5", version = "Pro"))
        val viewModel = createViewModelWithPs5Results()

        // Act
        viewModel.onAddPlatform(PS5_PRO)

        // Assert
        assertEquals(0, repository.pendingAdds)
    }

    @Test
    fun adding_platform_that_is_not_among_the_results_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModelWithPs5Results()

        // Act
        viewModel.onAddPlatform(SWITCH)

        // Assert
        assertEquals(0, repository.pendingAdds)
    }

    @Test
    fun adding_platform_when_not_in_results_state_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(PS5_QUERY)

        // Act
        viewModel.onAddPlatform(PS5)

        // Assert
        assertEquals(0, repository.pendingAdds)
        assertFalse(viewModel.state.value is SearchForPlatformScreenState.Results)
    }
}

/**
 * A [PlatformSearchService] implementation whose searches only complete when told so by tests.
 */
class ControlledPlatformSearchService : PlatformSearchService {

    private val pending = mutableMapOf<SearchString, CompletableDeferred<List<PlatformInfo>>>()

    /**
     * The queries searched for, in the order they were requested.
     */
    val requestedQueries = mutableListOf<SearchString>()

    /**
     * The queries whose searches were cancelled while pending, in the order they were cancelled.
     */
    val cancelledQueries = mutableListOf<SearchString>()

    override suspend fun search(query: SearchString): List<PlatformInfo> {
        requestedQueries += query
        val result = CompletableDeferred<List<PlatformInfo>>()
        pending[query] = result
        try {
            return result.await()
        } catch (e: CancellationException) {
            cancelledQueries += query
            // A new search for the same query may already be pending (e.g. a retry)
            if (pending[query] === result) pending.remove(query)
            throw e
        }
    }

    /**
     * Completes the pending search for [query] with the given [results].
     */
    fun complete(query: SearchString, results: List<PlatformInfo>) {
        pending.remove(query)?.complete(results)
    }

    /**
     * Completes the pending search for [query] by throwing the given [error].
     */
    fun fail(query: SearchString, error: Throwable) {
        pending.remove(query)?.completeExceptionally(error)
    }
}

/**
 * A [PlatformCollectionRepository] implementation whose additions only complete when told so by
 * tests, in the order they were requested.
 * @param readError If not null, the error thrown when collecting [platforms]. It can be changed
 * afterward, affecting subsequent collections (e.g. to simulate a recovery).
 */
class ControlledPlatformCollectionRepository(
    var readError: PersistenceException? = null
) : PlatformCollectionRepository {

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())
    private val pending = ArrayDeque<CompletableDeferred<Unit>>()

    /**
     * The number of collectors currently observing the collection.
     */
    val observers: Int
        get() = _platforms.subscriptionCount.value

    /**
     * The number of additions waiting to be completed.
     */
    val pendingAdds: Int
        get() = pending.size

    override val platforms: Flow<List<Platform>> = flow {
        readError?.let { throw it }
        emitAll(_platforms)
    }

    override suspend fun add(platform: NewPlatform): Long {
        val completion = CompletableDeferred<Unit>()
        pending.addLast(completion)
        completion.await()
        val id = (_platforms.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _platforms.update {
            it + Platform(
                id = id,
                abbreviation = platform.abbreviation,
                name = platform.name,
                logoUri = platform.logoUri,
                ownedSince = platform.ownedSince,
                version = platform.version,
                source = platform.source
            )
        }
        return id
    }

    override suspend fun getById(id: Long): Platform? = _platforms.value.find { it.id == id }

    override suspend fun getAll(): List<Platform> = _platforms.value

    /**
     * Adds the given platform to the collection, without going through [add].
     */
    fun preload(platform: Platform) {
        _platforms.update { it + platform }
    }

    /**
     * Completes the oldest pending addition successfully.
     */
    fun completeAdd() {
        pending.removeFirst().complete(Unit)
    }

    /**
     * Completes the oldest pending addition by throwing the given [error].
     */
    fun failAdd(error: PersistenceException) {
        pending.removeFirst().completeExceptionally(error)
    }
}
