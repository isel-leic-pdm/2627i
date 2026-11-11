package isel.dei.pdm.demos.gamevault.home

import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.LimitedResult
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.home.HomeViewModel.Companion.STOP_TIMEOUT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val now = Instant.parse("2026-11-09T20:00:00Z")
    private val thisYear = Year(2026)

    private fun entry(id: Long, name: String, state: State, sessionStart: Instant? = null) =
        CollectionEntry(
            game = Game(id, name),
            platforms = setOf(ps5),
            playStatus = PlayStatus(PlayTime(1, 0), state),
            sessionStartTime = sessionStart
        )

    private val sampleEntries = listOf(
        entry(1L, "Celeste", State.BACKLOG),
        entry(2L, "Elden Ring", State.PLAYING),
        entry(3L, "Hades", State.PLAYING, sessionStart = now)
    )

    /**
     * Collects the states published by the view model until the test ends, as the UI does. The
     * published states are only kept up to date while they are being collected.
     * @return The job of the collector, which can be cancelled to simulate the UI going away.
     */
    private fun TestScope.collectStates(viewModel: HomeViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            launch { viewModel.nowPlaying.collect {} }
            launch { viewModel.summary.collect {} }
        }

    // Now playing

    @Test
    fun now_playing_is_initially_loading() = runTest {
        // Arrange & Act
        val viewModel = HomeViewModel(FakeGameCollectionRepository(sampleEntries))

        // Assert
        assertSame(NowPlayingState.Loading, viewModel.nowPlaying.value)
    }

    @Test
    fun now_playing_lists_only_the_games_being_played_with_the_active_session_first() = runTest {
        // Arrange
        val viewModel = HomeViewModel(FakeGameCollectionRepository(sampleEntries))

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
        assertEquals(listOf(3L, 2L), state.items.map { it.gameId })
        assertFalse(state.hasMore)
    }

    @Test
    fun now_playing_reports_more_games_being_played_than_the_limit() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(
            sampleEntries + entry(4L, "Hollow Knight", State.PLAYING)
        )
        val viewModel = HomeViewModel(repository, nowPlayingLimit = 2)

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
        assertEquals(2, state.items.size)
        assertTrue(state.hasMore)
    }

    @Test
    fun now_playing_is_updated_when_a_game_stops_being_played() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(sampleEntries)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)

        // Act
        repository.updateEntry(2L) { it.copy(playStatus = it.playStatus.copy(state = State.PAUSED)) }

        // Assert
        val state = assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
        assertEquals(listOf(3L), state.items.map { it.gameId })
    }

    // Summary

    @Test
    fun summary_refers_to_the_current_year() = runTest {
        // Arrange
        val viewModel = HomeViewModel(
            FakeGameCollectionRepository(sampleEntries),
            currentYear = { thisYear }
        )

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<SummaryState.Loaded>(viewModel.summary.value)
        assertEquals(thisYear, state.summary.year)
        assertEquals(sampleEntries.size, state.summary.owned)
    }

    @Test
    fun summary_is_updated_when_a_game_is_added() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(sampleEntries)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)

        // Act
        repository.add(NewGame("Hollow Knight"), ps5)

        // Assert
        val state = assertIs<SummaryState.Loaded>(viewModel.summary.value)
        assertEquals(sampleEntries.size + 1, state.summary.owned)
    }

    @Test
    fun summary_moves_on_to_the_new_year_when_observation_restarts() = runTest {
        // Arrange
        var year = thisYear
        val viewModel = HomeViewModel(
            FakeGameCollectionRepository(sampleEntries),
            currentYear = { year }
        )
        val collector = collectStates(viewModel)

        // Act - The app goes to the background and returns after the year changed
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        year = Year(thisYear.value + 1)
        collectStates(viewModel)

        // Assert
        val state = assertIs<SummaryState.Loaded>(viewModel.summary.value)
        assertEquals(Year(thisYear.value + 1), state.summary.year)
    }

    // Observation lifecycle

    @Test
    fun last_states_are_kept_while_observation_restarts() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries)
        val viewModel = HomeViewModel(repository)
        val collector = collectStates(viewModel)

        // Act - The app returns from the background, and the collection takes a while to be read
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        repository.itemsOutcome = Outcome.NEVER_EMITS
        repository.summaryOutcome = Outcome.NEVER_EMITS
        collectStates(viewModel)

        // Assert
        assertEquals(2, repository.itemsObservations)
        assertEquals(2, repository.summaryObservations)
        assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
        assertIs<SummaryState.Loaded>(viewModel.summary.value)
    }

    @Test
    fun error_is_recovered_from_when_observation_restarts() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, itemsOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)
        val collector = collectStates(viewModel)
        assertIs<NowPlayingState.Error>(viewModel.nowPlaying.value)

        // Act - The app goes to the background in error, and returns once the storage recovered
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        repository.itemsOutcome = Outcome.EMITS
        collectStates(viewModel)

        // Assert - Without the user having to retry
        assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
    }

    // Errors and retries

    @Test
    fun failure_to_load_now_playing_does_not_affect_the_summary() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, itemsOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<NowPlayingState.Error>(viewModel.nowPlaying.value)
        assertSame(repository.failure, state.error)
        assertIs<SummaryState.Loaded>(viewModel.summary.value)
    }

    @Test
    fun failure_to_compute_the_summary_does_not_affect_now_playing() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, summaryOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)

        // Act
        collectStates(viewModel)

        // Assert
        val state = assertIs<SummaryState.Error>(viewModel.summary.value)
        assertSame(repository.failure, state.error)
        assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
    }

    @Test
    fun retrying_now_playing_after_an_error_moves_it_to_loading() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, itemsOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)
        repository.itemsOutcome = Outcome.NEVER_EMITS

        // Act
        viewModel.onRetryNowPlaying()

        // Assert
        assertSame(NowPlayingState.Loading, viewModel.nowPlaying.value)
    }

    @Test
    fun retrying_now_playing_after_an_error_loads_it_again() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, itemsOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)
        repository.itemsOutcome = Outcome.EMITS

        // Act
        viewModel.onRetryNowPlaying()

        // Assert
        assertIs<NowPlayingState.Loaded>(viewModel.nowPlaying.value)
        assertEquals(1, repository.summaryObservations)
    }

    @Test
    fun retrying_the_summary_after_an_error_computes_it_again() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries, summaryOutcome = Outcome.FAILS)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)
        repository.summaryOutcome = Outcome.EMITS

        // Act
        viewModel.onRetrySummary()

        // Assert
        assertIs<SummaryState.Loaded>(viewModel.summary.value)
        assertEquals(1, repository.itemsObservations)
    }

    @Test
    fun retrying_when_not_in_error_is_ignored() = runTest {
        // Arrange
        val repository = ControlledRepository(sampleEntries)
        val viewModel = HomeViewModel(repository)
        collectStates(viewModel)

        // Act
        viewModel.onRetryNowPlaying()
        viewModel.onRetrySummary()

        // Assert
        assertEquals(1, repository.itemsObservations)
        assertEquals(1, repository.summaryObservations)
    }
}

/**
 * The possible outcomes of observing the collection, in [ControlledRepository].
 */
private enum class Outcome { EMITS, FAILS, NEVER_EMITS }

/**
 * A repository whose observations of the games being played and of the summary can be made to
 * fail or to never emit, and that counts how many times each one was started. Otherwise, it
 * behaves as a [FakeGameCollectionRepository] with the given entries.
 */
private class ControlledRepository(
    entries: List<CollectionEntry>,
    var itemsOutcome: Outcome = Outcome.EMITS,
    var summaryOutcome: Outcome = Outcome.EMITS,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    val failure = RecoverablePersistenceException("Simulated failure")

    var itemsObservations = 0
        private set

    var summaryObservations = 0
        private set

    override fun observeItems(
        query: CollectionQuery,
        limit: Int
    ): Flow<LimitedResult<CollectionItem>> = flow {
        itemsObservations++
        when (itemsOutcome) {
            Outcome.EMITS -> emitAll(delegate.observeItems(query, limit))
            Outcome.FAILS -> throw failure
            Outcome.NEVER_EMITS -> awaitCancellation()
        }
    }

    override fun observeSummary(year: Year): Flow<CollectionSummary> = flow {
        summaryObservations++
        when (summaryOutcome) {
            Outcome.EMITS -> emitAll(delegate.observeSummary(year))
            Outcome.FAILS -> throw failure
            Outcome.NEVER_EMITS -> awaitCancellation()
        }
    }
}
