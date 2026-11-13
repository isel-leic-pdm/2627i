package isel.dei.pdm.demos.gamevault.search.games

import androidx.lifecycle.SavedStateHandle
import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeImageStore
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference.Provider.IGDB
import isel.dei.pdm.demos.gamevault.domain.collection.games.AddGameResult
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfoDetails
import isel.dei.pdm.demos.gamevault.domain.search.games.GamePlatform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.CATEGORY_KEY
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.DEFAULT_SEARCH_INDICATOR_DELAY
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.PLATFORM_KEY
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.QUERY_KEY
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesViewModel.Companion.STOP_TIMEOUT
import isel.dei.pdm.demos.gamevault.search.platforms.ControlledPlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchForGamesViewModelTest {

    private companion object {
        val ELDEN_QUERY = SearchString("Elden")
        val HADES_QUERY = SearchString("Hades")

        fun igdb(id: String) = SourceReference(IGDB, NonBlankString(id))

        // The platforms of the user's collection: the PS5 in two versions, and a Switch
        // inserted manually (so it has no source)
        val PS5 = Platform(1L, "PS5", "PlayStation 5", source = igdb("167"))
        val PS5_PRO = Platform(2L, "PS5", "PlayStation 5", version = "Pro", source = igdb("167"))
        val PC = Platform(3L, "PC", "PC (Microsoft Windows)", source = igdb("6"))
        val SWITCH = Platform(4L, "NSW", "Nintendo Switch")

        // The platforms games are released for, as provided by the search service
        val GAME_PS3 = gamePlatform(9L, "PlayStation 3", "PS3")
        val GAME_PS5 = gamePlatform(167L, "PlayStation 5", "PS5")
        val GAME_PC = gamePlatform(6L, "PC (Microsoft Windows)", "PC")
        val GAME_SWITCH = gamePlatform(130L, "Nintendo Switch", "Switch")

        val ELDEN_RING = GameInfo(
            id = 1L,
            name = "Elden Ring",
            coverUri = "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg",
            thumbnailUri = "https://images.igdb.com/igdb/image/upload/t_cover_small/co4jni.jpg",
            platforms = listOf(GAME_PS5, GAME_PC)
        )
        val HADES = GameInfo(2L, "Hades", platforms = listOf(GAME_PC, GAME_SWITCH))
        val DEMONS_SOULS = GameInfo(3L, "Demon's Souls", platforms = listOf(GAME_PS3))

        fun gamePlatform(id: Long, name: String, abbreviation: String) =
            GamePlatform(id, NonBlankString(name), NonBlankString(abbreviation))

        fun byId(id: String) = PlatformCriterion.ById(NonBlankString(id))

        fun byName(name: String, abbreviation: String) =
            PlatformCriterion.ByName(NonBlankString(name), NonBlankString(abbreviation))
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchService = ControlledGameSearchService()
    private val platformRepo = ControlledPlatformCollectionRepository().apply {
        listOf(PS5, PS5_PRO, PC, SWITCH).forEach { preload(it) }
    }
    private val gameRepo = ControlledGameCollectionRepository()
    private val imageStore = FakeImageStore()

    /**
     * Collects the states published by the view model until the test ends, as the UI does. The
     * published states are only kept up to date while they are being collected.
     * @return The job of the collector, which can be cancelled to simulate the UI going away.
     */
    private fun TestScope.collectStates(viewModel: SearchForGamesViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.state.collect {} }
            launch { viewModel.addOutcome.collect {} }
        }

    /**
     * Creates a view model whose published states are being collected.
     */
    private fun TestScope.createViewModel(
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
        platformRepository: ControlledPlatformCollectionRepository = platformRepo,
        gameRepository: GameCollectionRepository = gameRepo
    ): SearchForGamesViewModel {
        val viewModel = SearchForGamesViewModel(
            searchService, gameRepository, platformRepository, imageStore, savedStateHandle
        )
        collectStates(viewModel)
        return viewModel
    }

    /**
     * Creates a view model whose screen is displaying the results of searching for Elden Ring and
     * Hades on all platforms.
     */
    private fun TestScope.createViewModelWithResults(
        gameRepository: GameCollectionRepository = gameRepo
    ): SearchForGamesViewModel {
        val viewModel = createViewModel(gameRepository = gameRepository)
        viewModel.onSearchQueryChange(ELDEN_QUERY)
        searchService.complete(ELDEN_QUERY, listOf(ELDEN_RING, HADES))
        return viewModel
    }

    /**
     * The filters offered when all platforms of the collection are available.
     */
    private fun filters(
        selectedPlatform: Platform? = null,
        category: GameInfo.Category? = null
    ) = GameSearchFilters(listOf(PS5, PC, SWITCH), selectedPlatform, category)

    private fun SearchForGamesViewModel.items(): List<GameSearchItemUiState> {
        val state = state.value
        assertIs<SearchForGamesScreenState.Results>(state)
        return state.items
    }

    private fun SearchForGamesViewModel.statusOf(game: GameInfo, platform: Platform): AddStatus =
        items().first { it.game == game && it.platform == platform }.addStatus

    /**
     * Asserts that the current add outcome is a failure for the given game and platform. The error
     * instance is not compared, because coroutines may replace it with a copy when it crosses
     * coroutine boundaries (stack trace recovery, enabled in debug mode).
     */
    private fun SearchForGamesViewModel.assertFailedOutcomeFor(game: GameInfo, platform: Platform) {
        val outcome = addOutcome.value
        assertIs<AddGameOutcome.Failed>(outcome)
        assertEquals(game, outcome.game)
        assertEquals(platform, outcome.platform)
        assertIs<UnrecoverablePersistenceException>(outcome.error)
    }

    // Platforms

    @Test
    fun viewmodel_is_loading_until_the_platforms_are_read() = runTest {
        // Arrange
        val viewModel = SearchForGamesViewModel(searchService, gameRepo, platformRepo, imageStore, SavedStateHandle())
        assertEquals(SearchForGamesScreenState.Loading, viewModel.state.value)

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(SearchForGamesScreenState.Start(filters()), viewModel.state.value)
        assertNull(viewModel.addOutcome.value)
        assertTrue(searchService.requests.isEmpty())
    }

    @Test
    fun platforms_are_offered_once_regardless_of_their_versions() = runTest {
        // Act
        val viewModel = createViewModel()

        // Assert - The PS5 is offered through the first of its versions
        val state = viewModel.state.value
        assertIs<SearchForGamesScreenState.Start>(state)
        assertEquals(listOf(PS5, PC, SWITCH), state.filters.platforms)
        assertNull(state.filters.selectedPlatform)
        assertNull(state.filters.category)
    }

    @Test
    fun collection_without_platforms_transitions_to_no_platforms() = runTest {
        // Act
        val viewModel = createViewModel(platformRepository = ControlledPlatformCollectionRepository())

        // Assert
        assertEquals(SearchForGamesScreenState.NoPlatforms, viewModel.state.value)
    }

    @Test
    fun platforms_added_meanwhile_are_offered() = runTest {
        // Arrange
        val repository = ControlledPlatformCollectionRepository()
        val viewModel = createViewModel(platformRepository = repository)

        // Act - e.g. added in another screen
        repository.preload(PC)

        // Assert
        assertEquals(
            SearchForGamesScreenState.Start(GameSearchFilters(listOf(PC))),
            viewModel.state.value
        )
    }

    @Test
    fun platforms_read_failure_transitions_to_platforms_unavailable() = runTest {
        // Arrange
        val repository = ControlledPlatformCollectionRepository(readError = UnrecoverablePersistenceException())

        // Act
        val viewModel = createViewModel(platformRepository = repository)

        // Assert
        val state = viewModel.state.value
        assertIs<SearchForGamesScreenState.PlatformsUnavailable>(state)
        assertIs<UnrecoverablePersistenceException>(state.error)
    }

    @Test
    fun retry_from_platforms_unavailable_reads_the_platforms_again() = runTest {
        // Arrange
        val repository = ControlledPlatformCollectionRepository(readError = UnrecoverablePersistenceException())
        repository.preload(PC)
        val viewModel = createViewModel(platformRepository = repository)
        repository.readError = null

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(
            SearchForGamesScreenState.Start(GameSearchFilters(listOf(PC))),
            viewModel.state.value
        )
    }

    @Test
    fun platforms_are_only_observed_while_the_state_is_collected() = runTest {
        // Arrange
        val viewModel = SearchForGamesViewModel(searchService, gameRepo, platformRepo, imageStore, SavedStateHandle())
        assertEquals(0, platformRepo.observers)

        // Act & Assert - The UI starts collecting
        val collector = collectStates(viewModel)
        assertEquals(1, platformRepo.observers)

        // Act & Assert - The UI stops collecting (e.g. the app goes to the background)
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        runCurrent()
        assertEquals(0, platformRepo.observers)
    }

    // Search

    @Test
    fun new_query_searches_for_all_platforms_by_id_or_by_name() = runTest {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Assert - By id if obtained from the provider, by name if inserted manually
        assertEquals(SearchForGamesScreenState.Search(ELDEN_QUERY, filters()), viewModel.state.value)
        assertEquals(
            ControlledGameSearchService.Request(
                ELDEN_QUERY,
                listOf(byId("167"), byId("6"), byName("Nintendo Switch", "NSW")),
                category = null
            ),
            searchService.requests.single()
        )
    }

    @Test
    fun new_query_with_selected_filters_searches_for_them_only() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onPlatformSelected(PC)
        viewModel.onCategorySelected(GameInfo.Category.REMAKE)

        // Act
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Assert
        assertEquals(
            ControlledGameSearchService.Request(ELDEN_QUERY, listOf(byId("6")), GameInfo.Category.REMAKE),
            searchService.requests.single()
        )
    }

    @Test
    fun new_query_and_filters_are_preserved_in_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)

        // Act
        viewModel.onPlatformSelected(PC)
        viewModel.onCategorySelected(GameInfo.Category.REMAKE)
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Assert
        assertEquals(ELDEN_QUERY.value, savedStateHandle.get<String>(QUERY_KEY))
        assertEquals(PC.id, savedStateHandle.get<Long>(PLATFORM_KEY))
        assertEquals(GameInfo.Category.REMAKE.name, savedStateHandle.get<String>(CATEGORY_KEY))
    }

    @Test
    fun viewmodel_resumes_search_for_the_query_and_filters_preserved_in_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle(
            mapOf(QUERY_KEY to ELDEN_QUERY.value, PLATFORM_KEY to PC.id, CATEGORY_KEY to "REMAKE")
        )

        // Act
        val viewModel = createViewModel(savedStateHandle)

        // Assert
        assertEquals(
            SearchForGamesScreenState.Search(ELDEN_QUERY, filters(PC, GameInfo.Category.REMAKE)),
            viewModel.state.value
        )
        assertEquals(
            ControlledGameSearchService.Request(ELDEN_QUERY, listOf(byId("6")), GameInfo.Category.REMAKE),
            searchService.requests.single()
        )
    }

    @Test
    fun selected_platform_no_longer_in_the_collection_is_ignored() = runTest {
        // Arrange - e.g. removed from the collection while the process was dead
        val savedStateHandle = SavedStateHandle(mapOf(PLATFORM_KEY to 99L))

        // Act
        val viewModel = createViewModel(savedStateHandle)

        // Assert - All platforms are selected
        assertEquals(SearchForGamesScreenState.Start(filters()), viewModel.state.value)
    }

    @Test
    fun search_indicator_is_displayed_once_the_delay_elapses() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act & Assert
        advanceTimeBy(DEFAULT_SEARCH_INDICATOR_DELAY - 1.milliseconds)
        runCurrent()
        assertEquals(SearchForGamesScreenState.Search(ELDEN_QUERY, filters()), viewModel.state.value)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(
            SearchForGamesScreenState.Search(ELDEN_QUERY, filters(), isIndicatorVisible = true),
            viewModel.state.value
        )
    }

    @Test
    fun results_have_one_item_per_game_and_platform_of_the_collection_it_was_released_for() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        searchService.complete(ELDEN_QUERY, listOf(ELDEN_RING, HADES, DEMONS_SOULS))

        // Assert - Once per platform, regardless of versions, and none for Demon's Souls, which
        // was released for none of them. The Switch inserted manually is recognized by its name.
        assertEquals(
            listOf(ELDEN_RING to PS5, ELDEN_RING to PC, HADES to PC, HADES to SWITCH),
            viewModel.items().map { it.game to it.platform }
        )
        assertTrue(viewModel.items().all { it.addStatus == AddStatus.NOT_ADDED })
    }

    @Test
    fun results_with_a_selected_platform_only_include_it() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onPlatformSelected(PC)
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        searchService.complete(ELDEN_QUERY, listOf(ELDEN_RING, HADES))

        // Assert
        assertEquals(
            listOf(ELDEN_RING to PC, HADES to PC),
            viewModel.items().map { it.game to it.platform }
        )
    }

    @Test
    fun search_failure_transitions_to_error_state_and_retry_searches_again() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act & Assert
        searchService.fail(ELDEN_QUERY, NoConnectivityException())
        val state = viewModel.state.value
        assertIs<SearchForGamesScreenState.Error>(state)
        assertIs<NoConnectivityException>(state.error)

        // Act & Assert
        viewModel.onRetry()
        assertEquals(SearchForGamesScreenState.Search(ELDEN_QUERY, filters()), viewModel.state.value)
        assertEquals(2, searchService.requests.size)
    }

    @Test
    fun new_query_while_searching_cancels_the_ongoing_search() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        viewModel.onSearchQueryChange(HADES_QUERY)

        // Assert
        assertEquals(listOf(ELDEN_QUERY), searchService.cancelledQueries)
        assertEquals(SearchForGamesScreenState.Search(HADES_QUERY, filters()), viewModel.state.value)
    }

    @Test
    fun search_completes_while_the_ui_is_away_and_is_not_repeated_when_it_comes_back() = runTest {
        // Arrange
        val viewModel = SearchForGamesViewModel(searchService, gameRepo, platformRepo, imageStore, SavedStateHandle())
        val collector = collectStates(viewModel)
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act - The UI goes away (e.g. to the background) for longer than the stop timeout, and
        // the search completes meanwhile
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        runCurrent()
        searchService.complete(ELDEN_QUERY, listOf(ELDEN_RING))
        collectStates(viewModel)

        // Assert
        assertEquals(1, searchService.requests.size)
        assertEquals(listOf(ELDEN_RING to PS5, ELDEN_RING to PC), viewModel.items().map { it.game to it.platform })
    }

    @Test
    fun clearing_query_transitions_to_start_and_forgets_it() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val viewModel = createViewModel(savedStateHandle)
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        viewModel.onSearchQueryCleared()

        // Assert
        assertEquals(SearchForGamesScreenState.Start(filters()), viewModel.state.value)
        assertEquals(listOf(ELDEN_QUERY), searchService.cancelledQueries)
        assertNull(savedStateHandle.get<String>(QUERY_KEY))
    }

    // Filters

    @Test
    fun selecting_filters_without_query_updates_them_without_searching() = runTest {
        // Arrange
        val viewModel = createViewModel()

        // Act
        viewModel.onPlatformSelected(PC)
        viewModel.onCategorySelected(GameInfo.Category.DLC)

        // Assert
        assertEquals(
            SearchForGamesScreenState.Start(filters(PC, GameInfo.Category.DLC)),
            viewModel.state.value
        )
        assertTrue(searchService.requests.isEmpty())
    }

    @Test
    fun selecting_a_platform_with_results_searches_again_for_it() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()

        // Act
        viewModel.onPlatformSelected(PS5)

        // Assert
        assertEquals(SearchForGamesScreenState.Search(ELDEN_QUERY, filters(PS5)), viewModel.state.value)
        assertEquals(listOf(byId("167")), searchService.requests.last().platforms)
    }

    @Test
    fun selecting_a_category_while_searching_searches_again_with_it() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        viewModel.onCategorySelected(GameInfo.Category.DLC)

        // Assert
        assertEquals(listOf(ELDEN_QUERY), searchService.cancelledQueries)
        assertEquals(GameInfo.Category.DLC, searchService.requests.last().category)
    }

    @Test
    fun events_in_error_state_have_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)
        searchService.fail(ELDEN_QUERY, NoConnectivityException())
        val stateBefore = viewModel.state.value

        // Act - The only way out of the error state is retrying
        viewModel.onSearchQueryChange(HADES_QUERY)
        viewModel.onSearchQueryCleared()
        viewModel.onPlatformSelected(PC)
        viewModel.onCategorySelected(GameInfo.Category.DLC)

        // Assert
        assertEquals(stateBefore, viewModel.state.value)
        assertEquals(1, searchService.requests.size)
    }

    // Add status

    @Test
    fun results_in_the_collection_are_marked_as_added_on_the_platforms_they_are_owned_on() = runTest {
        // Arrange - Owned on the PS5 Pro, which counts as the PS5, as games ignore versions
        val repository = ControlledGameCollectionRepository(
            FakeGameCollectionRepository(
                listOf(CollectionEntry(Game(10L, "Elden Ring", source = igdb("1")), setOf(PS5_PRO)))
            )
        )

        // Act
        val viewModel = createViewModelWithResults(gameRepository = repository)

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(ELDEN_RING, PS5))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(ELDEN_RING, PC))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(HADES, PC))
    }

    @Test
    fun results_are_not_marked_as_added_when_the_collection_cannot_be_read() = runTest {
        // Arrange
        val repository = ControlledGameCollectionRepository(
            FakeGameCollectionRepository(
                listOf(CollectionEntry(Game(10L, "Elden Ring", source = igdb("1")), setOf(PS5)))
            ),
            findError = UnrecoverablePersistenceException()
        )

        // Act
        val viewModel = createViewModelWithResults(gameRepository = repository)

        // Assert
        assertTrue(viewModel.items().all { it.addStatus == AddStatus.NOT_ADDED })
    }

    // Adding games

    @Test
    fun adding_a_game_marks_it_as_adding_on_that_platform_only() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()

        // Act
        viewModel.onAddGame(ELDEN_RING, PC)

        // Assert
        assertEquals(AddStatus.ADDING, viewModel.statusOf(ELDEN_RING, PC))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(ELDEN_RING, PS5))
    }

    @Test
    fun successful_addition_marks_it_as_added_without_reporting_it() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)

        // Act
        gameRepo.completeAdd()

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(ELDEN_RING, PC))
        assertNull(viewModel.addOutcome.value)
        val entry = gameRepo.findSameEntry(NewGame("Elden Ring"))
        assertEquals(setOf(PC), entry?.platforms)
        assertEquals(igdb("1"), entry?.game?.source)
        assertIs<AddGameResult.Added>(gameRepo.results.single())
    }

    @Test
    fun addition_stores_local_copies_of_the_cover_and_thumbnail() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)

        // Act
        gameRepo.completeAdd()

        // Assert
        val game = gameRepo.findSameEntry(NewGame("Elden Ring"))?.game
        assertEquals(imageStore.copies[ELDEN_RING.coverUri], game?.coverUri)
        assertEquals(imageStore.copies[ELDEN_RING.thumbnailUri], game?.thumbnailUri)
        assertEquals(2, imageStore.copies.size)
    }

    @Test
    fun addition_keeps_the_remote_images_if_they_cannot_be_copied() = runTest {
        // Arrange - e.g. without connectivity
        imageStore.storeFails = true
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)

        // Act
        gameRepo.completeAdd()

        // Assert - The game is added anyway
        assertEquals(AddStatus.ADDED, viewModel.statusOf(ELDEN_RING, PC))
        val game = gameRepo.findSameEntry(NewGame("Elden Ring"))?.game
        assertEquals(ELDEN_RING.coverUri, game?.coverUri)
        assertEquals(ELDEN_RING.thumbnailUri, game?.thumbnailUri)
    }

    @Test
    fun adding_a_game_on_another_platform_adds_the_platform_to_its_entry() = runTest {
        // Arrange
        val repository = ControlledGameCollectionRepository(
            FakeGameCollectionRepository(
                listOf(CollectionEntry(Game(10L, "Elden Ring", source = igdb("1")), setOf(PS5)))
            )
        )
        val viewModel = createViewModelWithResults(gameRepository = repository)

        // Act
        viewModel.onAddGame(ELDEN_RING, PC)
        repository.completeAdd()

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(ELDEN_RING, PC))
        assertIs<AddGameResult.PlatformAdded>(repository.results.single())
        assertEquals(setOf(PS5, PC), repository.findSameEntry(NewGame("Elden Ring"))?.platforms)
    }

    @Test
    fun failed_addition_marks_it_as_not_added_and_reports_it() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)

        // Act
        gameRepo.failAdd(UnrecoverablePersistenceException())

        // Assert
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(ELDEN_RING, PC))
        viewModel.assertFailedOutcomeFor(ELDEN_RING, PC)
    }

    @Test
    fun addition_completes_and_is_reported_when_the_search_changes_meanwhile() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)
        viewModel.onSearchQueryChange(HADES_QUERY)
        searchService.complete(HADES_QUERY, listOf(HADES))

        // Act
        gameRepo.completeAdd()

        // Assert
        assertEquals(AddGameOutcome.Added(ELDEN_RING, PC), viewModel.addOutcome.value)
    }

    @Test
    fun added_game_is_displayed_as_added_when_it_reappears_in_the_results() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)
        gameRepo.completeAdd()

        // Act
        viewModel.onSearchQueryChange(HADES_QUERY)
        viewModel.onSearchQueryChange(ELDEN_QUERY)
        searchService.complete(ELDEN_QUERY, listOf(ELDEN_RING))

        // Assert
        assertEquals(AddStatus.ADDED, viewModel.statusOf(ELDEN_RING, PC))
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(ELDEN_RING, PS5))
    }

    @Test
    fun outcomes_are_reported_one_at_a_time_in_the_order_they_occurred() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()
        viewModel.onAddGame(ELDEN_RING, PC)
        viewModel.onAddGame(HADES, SWITCH)
        gameRepo.failAdd(UnrecoverablePersistenceException())
        gameRepo.failAdd(UnrecoverablePersistenceException())

        // Act & Assert
        viewModel.assertFailedOutcomeFor(ELDEN_RING, PC)
        viewModel.onAddOutcomeShown()
        viewModel.assertFailedOutcomeFor(HADES, SWITCH)
        viewModel.onAddOutcomeShown()
        assertNull(viewModel.addOutcome.value)
    }

    @Test
    fun adding_a_game_that_is_being_added_has_no_effect() = runTest {
        // Arrange - From now on, the view model's coroutines only run when the scheduler runs them,
        // so the published state does not yet reflect the first request when the second one
        // arrives (e.g. a double tap). The main dispatcher is restored by mainDispatcherRule
        val viewModel = createViewModelWithResults()
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        viewModel.onAddGame(ELDEN_RING, PC)
        assertEquals(AddStatus.NOT_ADDED, viewModel.statusOf(ELDEN_RING, PC))

        // Act
        viewModel.onAddGame(ELDEN_RING, PC)
        runCurrent()

        // Assert
        assertEquals(1, gameRepo.pendingAdds)
    }

    @Test
    fun adding_a_game_that_is_already_added_on_the_platform_has_no_effect() = runTest {
        // Arrange
        val repository = ControlledGameCollectionRepository(
            FakeGameCollectionRepository(
                listOf(CollectionEntry(Game(10L, "Elden Ring", source = igdb("1")), setOf(PC)))
            )
        )
        val viewModel = createViewModelWithResults(gameRepository = repository)

        // Act
        viewModel.onAddGame(ELDEN_RING, PC)

        // Assert
        assertEquals(0, repository.pendingAdds)
    }

    @Test
    fun adding_a_game_that_is_not_among_the_results_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModelWithResults()

        // Act - Not a result, and not released for the Switch, respectively
        viewModel.onAddGame(DEMONS_SOULS, PS5)
        viewModel.onAddGame(ELDEN_RING, SWITCH)

        // Assert
        assertEquals(0, gameRepo.pendingAdds)
    }

    @Test
    fun adding_a_game_when_not_in_results_state_has_no_effect() = runTest {
        // Arrange
        val viewModel = createViewModel()
        viewModel.onSearchQueryChange(ELDEN_QUERY)

        // Act
        viewModel.onAddGame(ELDEN_RING, PC)

        // Assert
        assertEquals(0, gameRepo.pendingAdds)
    }
}

/**
 * A [GameSearchService] implementation whose searches only complete when told so by tests.
 */
class ControlledGameSearchService : GameSearchService {

    /**
     * A search requested to the service.
     */
    data class Request(
        val name: SearchString,
        val platforms: List<PlatformCriterion>,
        val category: GameInfo.Category?
    )

    private val pending = mutableMapOf<SearchString, CompletableDeferred<List<GameInfo>>>()

    /**
     * The searches requested, in the order they were requested.
     */
    val requests = mutableListOf<Request>()

    /**
     * The queries whose searches were cancelled while pending, in the order they were cancelled.
     */
    val cancelledQueries = mutableListOf<SearchString>()

    override suspend fun searchGames(
        name: SearchString,
        platforms: List<PlatformCriterion>,
        category: GameInfo.Category?
    ): List<GameInfo> {
        requests += Request(name, platforms, category)
        val result = CompletableDeferred<List<GameInfo>>()
        pending[name] = result
        try {
            return result.await()
        } catch (e: CancellationException) {
            cancelledQueries += name
            // A new search for the same query may already be pending (e.g. a retry)
            if (pending[name] === result) pending.remove(name)
            throw e
        }
    }

    override suspend fun fetchGameDetails(gameId: Long): GameInfoDetails? = null

    /**
     * Completes the pending search for [query] with the given [results].
     */
    fun complete(query: SearchString, results: List<GameInfo>) {
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
 * A [GameCollectionRepository] implementation whose additions only complete when told so by
 * tests, in the order they were requested. The remaining operations are those of the given
 * [delegate].
 * @param findError If not null, the error thrown when looking for the same game in the collection.
 */
class ControlledGameCollectionRepository(
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(),
    private val findError: PersistenceException? = null
) : GameCollectionRepository by delegate {

    private val pending = ArrayDeque<CompletableDeferred<Unit>>()

    /**
     * The number of additions waiting to be completed.
     */
    val pendingAdds: Int
        get() = pending.size

    /**
     * The results of the completed additions, in the order they completed.
     */
    val results = mutableListOf<AddGameResult>()

    override suspend fun findSameEntry(game: NewGame): CollectionEntry? {
        findError?.let { throw it }
        return delegate.findSameEntry(game)
    }

    override suspend fun add(
        game: NewGame,
        platform: Platform,
        playStatus: PlayStatus,
        details: NewGameDetails
    ): AddGameResult {
        val completion = CompletableDeferred<Unit>()
        pending.addLast(completion)
        completion.await()
        return delegate.add(game, platform, playStatus, details).also { results += it }
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
