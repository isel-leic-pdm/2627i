package isel.dei.pdm.demos.gamevault.collection.games

import androidx.lifecycle.SavedStateHandle
import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.COMPLETION_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.NAME_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.PLATFORM_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.STATE_KEY
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionViewModel.Companion.STOP_TIMEOUT
import isel.dei.pdm.demos.gamevault.domain.LimitedResult
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository.Companion.DEFAULT_LIMIT
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class GameCollectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val ps5Pro = Platform(2L, "PS5", "PlayStation 5", version = "Pro")
    private val switch = Platform(3L, "Switch", "Nintendo Switch")

    private fun entry(id: Long, name: String, state: State, platform: Platform) =
        CollectionEntry(
            game = Game(id, name),
            platforms = setOf(platform),
            playStatus = PlayStatus(PlayTime(1, 0), state)
        )

    private val sampleEntries = listOf(
        entry(1L, "Celeste", State.BACKLOG, ps5),
        entry(2L, "Elden Ring", State.PLAYING, ps5Pro),
        entry(3L, "Hades", State.PLAYING, switch),
        entry(4L, "Zelda", State.PAUSED, switch)
    )

    private fun viewModel(
        gameRepo: GameCollectionRepository = FakeGameCollectionRepository(sampleEntries),
        platformRepo: PlatformCollectionRepository = StubPlatformRepository(listOf(ps5, ps5Pro, switch)),
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) = GameCollectionViewModel(gameRepo, platformRepo, savedStateHandle)

    /**
     * Collects the states published by the view model until the test ends, as the UI does. The
     * published state is only kept up to date while it is being collected.
     * @return The job of the collector, which can be cancelled to simulate the UI going away.
     */
    private fun TestScope.collectStates(viewModel: GameCollectionViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

    private val GameCollectionScreenState.listedIds: List<Long>?
        get() = assertIs<GameCollectionScreenState.Browsing>(this)
            .sections?.flatMap { section -> section.items.map { it.gameId } }

    // Loading

    @Test
    fun state_is_initially_loading() = runTest {
        // Arrange & Act
        val viewModel = viewModel()

        // Assert
        assertSame(GameCollectionScreenState.Loading, viewModel.state.value)
    }

    @Test
    fun without_initial_query_all_games_are_listed_grouped_by_initial() = runTest {
        // Arrange
        val viewModel = viewModel()

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(CollectionQuery(), state.query)
        assertEquals(listOf('C', 'E', 'H', 'Z'), state.sections?.map { it.initial })
        assertEquals(listOf(1L, 2L, 3L, 4L), state.listedIds)
    }

    @Test
    fun all_games_are_listed_even_beyond_the_default_limit() = runTest {
        // Arrange
        val entries = (1L..DEFAULT_LIMIT + 50L).map { entry(it, "Game $it", State.BACKLOG, ps5) }
        val viewModel = viewModel(gameRepo = FakeGameCollectionRepository(entries))

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(entries.size, viewModel.state.value.listedIds?.size)
    }

    @Test
    fun platforms_offered_are_distinct_as_far_as_games_are_concerned() = runTest {
        // Arrange
        val viewModel = viewModel()

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(listOf(ps5, switch), state.platforms)
    }

    // Initial query

    @Test
    fun initial_query_is_read_from_the_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle(
            mapOf(STATE_KEY to State.PLAYING.name, NAME_KEY to "ha")
        )
        val viewModel = viewModel(savedStateHandle = savedStateHandle)

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(
            CollectionQuery(state = State.PLAYING, nameContains = NonBlankString("ha")),
            state.query
        )
        assertEquals(listOf(3L), state.listedIds)
    }

    @Test
    fun initial_platform_is_resolved_to_the_one_offered_for_all_its_versions() = runTest {
        // Arrange
        val viewModel = viewModel(savedStateHandle = SavedStateHandle(mapOf(PLATFORM_KEY to ps5Pro.id)))

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(ps5, state.query.platform)
        assertEquals(listOf(1L, 2L), state.listedIds)
    }

    @Test
    fun initial_platform_not_in_the_collection_is_ignored() = runTest {
        // Arrange
        val viewModel = viewModel(savedStateHandle = SavedStateHandle(mapOf(PLATFORM_KEY to 99L)))

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertNull(state.query.platform)
        assertEquals(listOf(1L, 2L, 3L, 4L), state.listedIds)
    }

    // Query changes

    @Test
    fun query_change_lists_the_games_that_meet_it() = runTest {
        // Arrange
        val viewModel = viewModel()
        collectStates(viewModel)
        val query = CollectionQuery(state = State.PLAYING, platform = switch)

        // Act
        viewModel.onQueryChange(query)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(query, state.query)
        assertEquals(listOf(3L), state.listedIds)
    }

    @Test
    fun query_change_is_preserved_in_the_saved_state() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle(mapOf(STATE_KEY to State.PAUSED.name))
        val viewModel = viewModel(savedStateHandle = savedStateHandle)
        collectStates(viewModel)

        // Act
        viewModel.onQueryChange(
            CollectionQuery(
                completion = Completion.NOT_FINISHED,
                platform = switch,
                nameContains = NonBlankString("ha")
            )
        )

        // Assert
        assertNull(savedStateHandle[STATE_KEY])
        assertEquals(Completion.NOT_FINISHED.name, savedStateHandle[COMPLETION_KEY])
        assertEquals(switch.id, savedStateHandle[PLATFORM_KEY])
        assertEquals("ha", savedStateHandle[NAME_KEY])
    }

    @Test
    fun previous_games_are_kept_while_those_of_the_new_query_are_read() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries)
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        val query = CollectionQuery(state = State.PAUSED)
        repository.pendingQuery = query

        // Act
        viewModel.onQueryChange(query)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(query, state.query)
        assertEquals(listOf(1L, 2L, 3L, 4L), state.listedIds)
    }

    @Test
    fun listing_is_updated_when_the_collection_changes() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(sampleEntries)
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        repository.add(NewGame("Astro Bot"), ps5)

        // Assert
        val state = assertIs<GameCollectionScreenState.Browsing>(viewModel.state.value)
        assertEquals(listOf('A', 'C', 'E', 'H', 'Z'), state.sections?.map { it.initial })
    }

    // Observation lifecycle

    @Test
    fun last_games_are_kept_while_observation_restarts() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries)
        val viewModel = viewModel(gameRepo = repository)
        val collector = collectStates(viewModel)

        // Act - The app returns from the background, and the collection takes a while to be read
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        repository.pendingQuery = CollectionQuery()
        collectStates(viewModel)

        // Assert
        assertEquals(2, repository.itemsObservations)
        assertEquals(listOf(1L, 2L, 3L, 4L), viewModel.state.value.listedIds)
    }

    @Test
    fun last_games_are_kept_while_observation_restarts_after_a_retry() = runTest {
        // Arrange - The collection is browsed after recovering from an error
        val repository = ControlledRepository(sampleEntries, fails = true)
        val viewModel = viewModel(gameRepo = repository)
        val collector = collectStates(viewModel)
        repository.fails = false
        viewModel.onRetry()

        // Act - The app returns from the background, and the collection takes a while to be read
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        repository.pendingQuery = CollectionQuery()
        val published = mutableListOf<GameCollectionScreenState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { published += it }
        }

        // Assert - The restart is not taken for a retry, so the screen does not move to Loading,
        // even briefly, which would discard the view's state (e.g. the listing's scroll)
        assertEquals(3, repository.itemsObservations)
        assertTrue(published.none { it is GameCollectionScreenState.Loading })
        assertEquals(listOf(1L, 2L, 3L, 4L), viewModel.state.value.listedIds)
    }

    // Errors and retries

    @Test
    fun failure_to_read_the_collection_moves_to_error() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, fails = true)
        val viewModel = viewModel(gameRepo = repository)

        // Act
        collectStates(viewModel)

        // Assert - The error may be a copy of the failure, as coroutines recover stack traces by
        // copying the exceptions that cross coroutine boundaries (e.g. in combine)
        val state = assertIs<GameCollectionScreenState.Error>(viewModel.state.value)
        val error = assertIs<RecoverablePersistenceException>(state.error)
        assertEquals(repository.failure.message, error.message)
    }

    @Test
    fun retrying_after_an_error_reads_the_collection_again() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, fails = true)
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        repository.fails = false

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(listOf(1L, 2L, 3L, 4L), viewModel.state.value.listedIds)
    }

    @Test
    fun retrying_when_not_in_error_is_ignored() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries)
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(1, repository.itemsObservations)
    }

    @Test
    fun query_change_in_error_is_ignored() = runTest {
        // Arrange
        val savedStateHandle = SavedStateHandle()
        val repository = ControlledRepository(sampleEntries, fails = true)
        val viewModel = viewModel(gameRepo = repository, savedStateHandle = savedStateHandle)
        collectStates(viewModel)

        // Act
        viewModel.onQueryChange(CollectionQuery(state = State.PLAYING))

        // Assert
        assertIs<GameCollectionScreenState.Error>(viewModel.state.value)
        assertNull(savedStateHandle[STATE_KEY])
    }
}

/**
 * A repository of platforms with the given ones, which can be changed through [platforms].
 */
private class StubPlatformRepository(initial: List<Platform>) : PlatformCollectionRepository {

    override val platforms = MutableStateFlow(initial)

    override suspend fun add(platform: NewPlatform): Long = error("Not used")

    override suspend fun getById(id: Long): Platform? = platforms.value.find { it.id == id }

    override suspend fun getAll(): List<Platform> = platforms.value
}

/**
 * A repository whose observation of the items can be made to fail, or to never emit for a given
 * query, and that counts how many times it was started. Otherwise, it behaves as a
 * [FakeGameCollectionRepository] with the given entries.
 */
private class ControlledRepository(
    entries: List<CollectionEntry>,
    var fails: Boolean = false,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    val failure = RecoverablePersistenceException("Simulated failure")

    /**
     * The query whose items are never emitted (i.e. are being read), if any.
     */
    var pendingQuery: CollectionQuery? = null

    var itemsObservations = 0
        private set

    override fun observeItems(
        query: CollectionQuery,
        limit: Int
    ): Flow<LimitedResult<CollectionItem>> = flow {
        itemsObservations++
        when {
            fails -> throw failure
            query == pendingQuery -> awaitCancellation()
            else -> emitAll(delegate.observeItems(query, limit))
        }
    }
}
