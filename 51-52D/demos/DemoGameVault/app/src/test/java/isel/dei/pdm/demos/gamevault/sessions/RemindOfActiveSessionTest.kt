package isel.dei.pdm.demos.gamevault.sessions

import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class RemindOfActiveSessionTest {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val game = Game(1L, "Elden Ring")
    private val sessionStart = Instant.parse("2026-11-25T21:00:00Z")

    private val entry = CollectionEntry(game = game, platforms = setOf(ps5))
    private val entryInSession = entry.startSession(sessionStart)

    private val notifier = RecordingNotifier()

    @Test
    fun the_active_session_is_reminded_of() = runTest {
        val repository = FakeGameCollectionRepository(listOf(entryInSession))

        val reminded = remindOfActiveSession(repository, notifier, game.id, sessionStart)

        assertTrue(reminded)
        assertEquals(entryInSession, notifier.reminded)
    }

    @Test
    fun a_stopped_session_is_not_reminded_of() = runTest {
        val repository = FakeGameCollectionRepository(listOf(entry))

        val reminded = remindOfActiveSession(repository, notifier, game.id, sessionStart)

        assertFalse(reminded)
        assertNull(notifier.reminded)
    }

    @Test
    fun another_session_of_the_game_is_not_reminded_of() = runTest {
        // e.g. the session was stopped, and another one was started, but the reminder of the
        // first one was not cancelled
        val repository = FakeGameCollectionRepository(
            listOf(entry.startSession(sessionStart + 4.hours))
        )

        val reminded = remindOfActiveSession(repository, notifier, game.id, sessionStart)

        assertFalse(reminded)
        assertNull(notifier.reminded)
    }

    @Test
    fun a_game_no_longer_in_the_collection_is_not_reminded_of() = runTest {
        val repository = FakeGameCollectionRepository()

        val reminded = remindOfActiveSession(repository, notifier, game.id, sessionStart)

        assertFalse(reminded)
        assertNull(notifier.reminded)
    }

    @Test
    fun failure_to_read_the_session_is_propagated() = runTest {
        // So that the reminder can be retried later
        val repository = FailingRepository()

        assertFailsWith<RecoverablePersistenceException> {
            remindOfActiveSession(repository, notifier, game.id, sessionStart)
        }
        assertNull(notifier.reminded)
    }
}

/**
 * A notifier that only records the session it reminds of, i.e. shows.
 */
private class RecordingNotifier : PlaySessionNotifier {

    var reminded: CollectionEntry? = null
        private set

    override fun showSession(entry: CollectionEntry) {
        reminded = entry
    }

    override fun hideSession() {}
}

/**
 * A repository whose entries cannot be read. Otherwise, it behaves as an empty
 * [FakeGameCollectionRepository].
 */
private class FailingRepository(
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository()
) : GameCollectionRepository by delegate {

    override fun observeEntry(gameId: Long): Flow<CollectionEntry?> = flow {
        throw RecoverablePersistenceException("Simulated failure")
    }
}
