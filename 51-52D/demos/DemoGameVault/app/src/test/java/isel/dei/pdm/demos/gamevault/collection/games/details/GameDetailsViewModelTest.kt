package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.lifecycle.SavedStateHandle
import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlaySessionReminders
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Closed
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsScreenState.Displaying
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsViewModel.Companion.GAME_ID_KEY
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsViewModel.Companion.STOP_TIMEOUT
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.StartSessionResult
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val game = Game(1L, "Elden Ring")
    private val otherGame = Game(2L, "Hades")

    /**
     * The instant the test starts at, according to [virtualClock].
     */
    private val testStart = Instant.parse("2026-11-18T21:00:00Z")

    private fun entry(
        game: Game = this.game,
        sessionStartTime: Instant? = null,
        notes: NonBlankString? = null
    ) = CollectionEntry(
        game = game,
        platforms = setOf(ps5),
        playStatus = PlayStatus(PlayTime(10, 0), State.PLAYING),
        addedAt = Instant.parse("2024-03-01T10:00:00Z"),
        sessionStartTime = sessionStartTime,
        notes = notes
    )

    /**
     * A clock that follows the virtual time of the test, starting at [testStart], so that the
     * elapsed time of the sessions advances with it.
     */
    private fun TestScope.virtualClock() = object : Clock {
        override fun now(): Instant = testStart + testScheduler.currentTime.milliseconds
    }

    private val reminders = FakePlaySessionReminders()

    private fun TestScope.viewModel(
        gameRepo: GameCollectionRepository = FakeGameCollectionRepository(listOf(entry())),
        savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf(GAME_ID_KEY to game.id))
    ) = GameDetailsViewModel(gameRepo, savedStateHandle, reminders, virtualClock())

    /**
     * Collects the states published by the view model until the test ends, as the UI does. The
     * published state is only kept up to date while it is being collected.
     * @return The job of the collector, which can be cancelled to simulate the UI going away.
     */
    private fun TestScope.collectStates(viewModel: GameDetailsViewModel): Job =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

    private val GameDetailsViewModel.displayed: Displaying
        get() = assertIs<Displaying>(state.value)

    // Creation

    @Test
    fun game_identifier_must_be_provided() = runTest {
        assertFailsWith<IllegalStateException> {
            viewModel(savedStateHandle = SavedStateHandle())
        }
    }

    // Loading and displaying

    @Test
    fun state_is_initially_loading() = runTest {
        // Arrange & Act
        val viewModel = viewModel()

        // Assert
        assertSame(GameDetailsScreenState.Loading, viewModel.state.value)
    }

    @Test
    fun the_game_is_displayed_with_its_entry() = runTest {
        // Arrange
        val entry = entry(notes = NonBlankString("Stopped at Malenia."))
        val viewModel = viewModel(gameRepo = FakeGameCollectionRepository(listOf(entry)))

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(entry, viewModel.displayed.entry)
    }

    @Test
    fun the_game_identified_in_the_saved_state_is_displayed() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(listOf(entry(), entry(game = otherGame)))
        val viewModel = viewModel(
            gameRepo = repository,
            savedStateHandle = SavedStateHandle(mapOf(GAME_ID_KEY to otherGame.id))
        )

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(otherGame, viewModel.displayed.entry.game)
    }

    @Test
    fun changes_to_the_game_are_displayed() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        repository.updateEntry(game.id) { it.addPlayTime(90.minutes) }
        repository.updateEntry(game.id) { it.copy(notes = NonBlankString("Replay on hard.")) }

        // Assert
        assertEquals(PlayTime(11, 30), viewModel.displayed.entry.playStatus.timeSpent)
        assertEquals(NonBlankString("Replay on hard."), viewModel.displayed.entry.notes)
    }

    @Test
    fun game_not_in_the_collection_closes_the_screen() = runTest {
        // Arrange
        val viewModel = viewModel(gameRepo = FakeGameCollectionRepository())

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(Closed(Closed.Reason.GAME_NOT_FOUND), viewModel.state.value)
    }

    @Test
    fun removing_the_game_closes_the_screen() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        repository.remove(game.id)

        // Assert
        assertEquals(Closed(Closed.Reason.GAME_NOT_FOUND), viewModel.state.value)
    }

    // Session elapsed time

    @Test
    fun without_an_active_session_there_is_no_elapsed_time() = runTest {
        // Arrange
        val viewModel = viewModel()

        // Act
        collectStates(viewModel)

        // Assert
        assertNull(viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun with_an_active_session_the_elapsed_time_is_that_since_it_started() = runTest {
        // Arrange
        val sessionStart = testStart - 72.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(72.minutes, viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun the_elapsed_time_is_updated_whenever_a_whole_minute_elapses() = runTest {
        // Arrange - The session started 1 min 30 s ago
        val sessionStart = testStart - 90.seconds
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act & Assert - It is only updated once the session completes 2 minutes
        advanceTimeBy(29.seconds)
        assertEquals(90.seconds, viewModel.displayed.sessionElapsedTime)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertEquals(2.minutes, viewModel.displayed.sessionElapsedTime)
        advanceTimeBy(1.minutes)
        assertEquals(3.minutes, viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun starting_a_session_starts_the_elapsed_time() = runTest {
        // Arrange
        val repository = FakeGameCollectionRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        repository.startSession(game.id, now = testStart)
        advanceTimeBy(5.minutes + 1.milliseconds)

        // Assert
        assertEquals(5.minutes, viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun ending_the_session_stops_the_elapsed_time() = runTest {
        // Arrange
        val sessionStart = testStart - 10.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        repository.updateEntry(game.id) { it.endSession(playedFor = 10.minutes) }
        advanceTimeBy(5.minutes)

        // Assert
        assertNull(viewModel.displayed.sessionElapsedTime)
        assertEquals(PlayTime(10, 10), viewModel.displayed.entry.playStatus.timeSpent)
    }

    @Test
    fun a_session_starting_after_the_clock_is_taken_as_just_started() = runTest {
        // Arrange - e.g. the device's clock was set back after the session started
        val sessionStart = testStart + 10.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)

        // Act
        collectStates(viewModel)

        // Assert
        assertEquals(Duration.ZERO, viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun the_session_is_short_until_the_short_session_duration_elapses() = runTest {
        // Arrange - The session started 4 min 30 s ago
        val sessionStart = testStart - CollectionEntry.SHORT_SESSION + 30.seconds
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act & Assert
        assertTrue(viewModel.displayed.isSessionShort)
        advanceTimeBy(30.seconds + 1.milliseconds)
        assertFalse(viewModel.displayed.isSessionShort)
    }

    @Test
    fun without_an_active_session_it_is_not_short() = runTest {
        // Arrange
        val viewModel = viewModel()

        // Act
        collectStates(viewModel)

        // Assert
        assertFalse(viewModel.displayed.isSessionShort)
    }

    // Starting sessions

    @Test
    fun starting_a_session_starts_it_now() = runTest {
        // Arrange
        val viewModel = viewModel()
        collectStates(viewModel)

        // Act
        viewModel.onStartSession()

        // Assert
        assertEquals(testStart, viewModel.displayed.entry.sessionStartTime)
        assertEquals(Duration.ZERO, viewModel.displayed.sessionElapsedTime)
    }

    @Test
    fun starting_a_session_schedules_its_reminder() = runTest {
        // Arrange
        val viewModel = viewModel()
        collectStates(viewModel)

        // Act
        viewModel.onStartSession()

        // Assert
        assertEquals(testStart, reminders.scheduled?.sessionStartTime)
    }

    @Test
    fun starting_a_session_while_one_is_active_is_ignored() = runTest {
        // Arrange
        val sessionStart = testStart - 10.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onStartSession()

        // Assert
        assertEquals(sessionStart, viewModel.displayed.entry.sessionStartTime)
        assertNull(viewModel.actionFailure.value)
    }

    @Test
    fun failure_to_start_a_session_is_published() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        repository.failsChanges = true

        // Act
        viewModel.onStartSession()

        // Assert
        val failure = assertNotNull(viewModel.actionFailure.value)
        assertEquals(GameActionFailure.Action.START_SESSION, failure.action)
        assertEquals(repository.failure.message, failure.error.message)
        assertNull(viewModel.displayed.entry.sessionStartTime)
        assertNull(reminders.scheduled)
    }

    // Stopping sessions

    @Test
    fun stopping_and_registering_the_session_adds_the_elapsed_time() = runTest {
        // Arrange
        val sessionStart = testStart - 72.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onStopSession(register = true)

        // Assert
        assertNull(viewModel.displayed.sessionElapsedTime)
        assertEquals(PlayTime(11, 12), viewModel.displayed.entry.playStatus.timeSpent)
    }

    @Test
    fun stopping_the_session_cancels_its_reminder() = runTest {
        // Arrange
        val viewModel = viewModel()
        collectStates(viewModel)
        viewModel.onStartSession()

        // Act
        viewModel.onStopSession(register = false)

        // Assert
        assertNull(reminders.scheduled)
    }

    @Test
    fun stopping_and_discarding_the_session_adds_no_time() = runTest {
        // Arrange
        val sessionStart = testStart - 3.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onStopSession(register = false)

        // Assert
        assertNull(viewModel.displayed.sessionElapsedTime)
        assertEquals(PlayTime(10, 0), viewModel.displayed.entry.playStatus.timeSpent)
    }

    @Test
    fun stopping_a_session_starting_after_the_clock_adds_no_time() = runTest {
        // Arrange - e.g. the device's clock was set back after the session started
        val sessionStart = testStart + 10.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onStopSession(register = true)

        // Assert
        assertNull(viewModel.displayed.sessionElapsedTime)
        assertEquals(PlayTime(10, 0), viewModel.displayed.entry.playStatus.timeSpent)
    }

    @Test
    fun stopping_the_session_to_leave_closes_the_screen() = runTest {
        // Arrange
        val sessionStart = testStart - 72.minutes
        val repository = FakeGameCollectionRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onStopSession(register = true, leave = true)

        // Assert
        assertEquals(Closed(Closed.Reason.LEFT), viewModel.state.value)
        assertEquals(PlayTime(11, 12), repository.observeEntry(game.id).first()?.playStatus?.timeSpent)
    }

    @Test
    fun stopping_without_an_active_session_is_ignored() = runTest {
        // Arrange
        val viewModel = viewModel()
        collectStates(viewModel)

        // Act
        viewModel.onStopSession(register = true, leave = true)

        // Assert
        assertIs<Displaying>(viewModel.state.value)
    }

    @Test
    fun failure_to_stop_the_session_is_published_and_the_screen_is_not_left() = runTest {
        // Arrange
        val sessionStart = testStart - 72.minutes
        val repository = ControlledRepository(listOf(entry(sessionStartTime = sessionStart)))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        repository.failsChanges = true

        // Act
        viewModel.onStopSession(register = true, leave = true)

        // Assert
        val failure = assertNotNull(viewModel.actionFailure.value)
        assertEquals(GameActionFailure.Action.STOP_SESSION, failure.action)
        assertEquals(repository.failure.message, failure.error.message)
        assertEquals(sessionStart, viewModel.displayed.entry.sessionStartTime)
    }

    @Test
    fun the_action_failure_is_cleared_once_shown() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        repository.failsChanges = true
        viewModel.onStartSession()

        // Act
        viewModel.onActionFailureShown()

        // Assert
        assertNull(viewModel.actionFailure.value)
    }

    // Observation lifecycle

    @Test
    fun last_state_is_kept_while_observation_restarts() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        val collector = collectStates(viewModel)
        collector.cancel()
        advanceTimeBy(STOP_TIMEOUT + 1.milliseconds)
        repository.isReadPending = true
        val published = mutableListOf<GameDetailsScreenState>()

        // Act
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { published += it }
        }

        // Assert - The restart is not taken for a retry, so the screen does not move to Loading
        assertEquals(2, repository.entryObservations)
        assertTrue(published.none { it is GameDetailsScreenState.Loading })
        assertEquals(entry(), viewModel.displayed.entry)
    }

    // Errors and retries

    @Test
    fun failure_to_read_the_game_moves_to_error() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()), fails = true)
        val viewModel = viewModel(gameRepo = repository)

        // Act
        collectStates(viewModel)

        // Assert - The error may be a copy of the failure, as coroutines recover stack traces by
        // copying the exceptions that cross coroutine boundaries (e.g. in combine)
        val state = assertIs<GameDetailsScreenState.Error>(viewModel.state.value)
        val error = assertIs<RecoverablePersistenceException>(state.error)
        assertEquals(repository.failure.message, error.message)
    }

    @Test
    fun retrying_after_an_error_reads_the_game_again() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()), fails = true)
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)
        repository.fails = false

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(entry(), viewModel.displayed.entry)
    }

    @Test
    fun retrying_when_not_in_error_is_ignored() = runTest {
        // Arrange
        val repository = ControlledRepository(listOf(entry()))
        val viewModel = viewModel(gameRepo = repository)
        collectStates(viewModel)

        // Act
        viewModel.onRetry()

        // Assert
        assertEquals(1, repository.entryObservations)
    }
}

/**
 * A repository whose observation of the entries can be made to fail, or to never emit (i.e. to
 * be pending), and that counts how many times it was started. Otherwise, it behaves as a
 * [FakeGameCollectionRepository] with the given entries.
 */
private class ControlledRepository(
    entries: List<CollectionEntry>,
    var fails: Boolean = false,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository(entries)
) : GameCollectionRepository by delegate {

    val failure = RecoverablePersistenceException("Simulated failure")

    /**
     * Whether the entries are being read, i.e. never emitted.
     */
    var isReadPending = false

    var entryObservations = 0
        private set

    /**
     * Whether changing the entries fails.
     */
    var failsChanges = false

    override suspend fun updateEntry(
        gameId: Long,
        change: (CollectionEntry) -> CollectionEntry
    ): CollectionEntry? =
        if (failsChanges) throw failure
        else delegate.updateEntry(gameId, change)

    override suspend fun startSession(gameId: Long, now: Instant): StartSessionResult =
        if (failsChanges) throw failure
        else delegate.startSession(gameId, now)

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> = flow {
        entryObservations++
        when {
            fails -> throw failure
            isReadPending -> awaitCancellation()
            else -> emitAll(delegate.observeEntry(gameId))
        }
    }
}
