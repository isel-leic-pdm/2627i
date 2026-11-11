package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class CollectionEntryTests {

    private val game = Game(1L, "Elden Ring")
    private val platform = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")
    private val addedAt = Instant.parse("2026-01-15T10:00:00Z")
    private val sessionStart = Instant.parse("2026-11-08T20:00:00Z")
    private val completedOn = LocalDate.of(2026, 11, 8)

    private fun entry(
        state: State = State.PLAYING,
        timeSpent: PlayTime = PlayTime(10, 0),
        completedRuns: List<Accomplishment> = emptyList(),
        sessionStartTime: Instant? = null
    ) = CollectionEntry(
        game = game,
        platforms = setOf(platform),
        playStatus = PlayStatus(timeSpent, state, completedRuns),
        addedAt = addedAt,
        sessionStartTime = sessionStartTime
    )

    private fun entryInSession() = entry(sessionStartTime = sessionStart)

    // Construction

    @Test
    fun `CollectionEntry has default play status and no active session by default`() {
        // Act
        val entry = CollectionEntry(game, setOf(platform))

        // Assert
        assertEquals(PlayStatus(), entry.playStatus)
        assertNull(entry.sessionStartTime)
        assertFalse(entry.isSessionActive)
    }

    @Test
    fun `CollectionEntry is added now by default`() {
        // Arrange
        val before = Clock.System.now()

        // Act
        val entry = CollectionEntry(game, setOf(platform))

        // Assert
        val after = Clock.System.now()
        assertTrue(entry.addedAt in before..after)
    }

    @Test
    fun `CollectionEntry must be owned on at least one platform`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionEntry(game, emptySet())
        }
    }

    @Test
    fun `CollectionEntry platforms cannot include versions of the same platform`() {
        // Games are oblivious of versions: owned on the PS5 Slim is owned on the PS5
        val slim = Platform(3L, "PS5", "PlayStation 5", version = "Slim")

        assertFailsWith<IllegalArgumentException> {
            CollectionEntry(game, setOf(platform, slim))
        }
    }

    @Test
    fun `CollectionEntry with an active session must be PLAYING`() {
        assertTrue(entry(state = State.PLAYING, sessionStartTime = sessionStart).isSessionActive)
        assertFailsWith<IllegalArgumentException> {
            entry(state = State.PAUSED, sessionStartTime = sessionStart)
        }
    }

    // Platforms

    @Test
    fun `isOwnedOn recognizes the platforms regardless of their versions`() {
        val entry = entry()

        assertTrue(entry.isOwnedOn(platform))
        assertTrue(entry.isOwnedOn(Platform(3L, "PS5", "PlayStation 5", version = "Slim")))
        assertFalse(entry.isOwnedOn(switch))
    }

    @Test
    fun `addPlatform adds the platform after the existing ones and keeps the rest of the entry`() {
        // Arrange
        val entry = entry(state = State.FINISHED, completedRuns = listOf(Accomplishment(completedOn)))

        // Act
        val updated = entry.addPlatform(switch)

        // Assert
        assertEquals(listOf(platform, switch), updated.platforms.toList())
        assertEquals(entry.playStatus, updated.playStatus)
        assertEquals(entry.game, updated.game)
        assertEquals(entry.addedAt, updated.addedAt)
    }

    @Test
    fun `addPlatform has no effect if the game is already owned on the platform`() {
        // Arrange
        val entry = entry()

        // Act - Even if given another version of the platform
        val updated = entry.addPlatform(Platform(3L, "PS5", "PlayStation 5", version = "Pro"))

        // Assert
        assertEquals(entry, updated)
    }

    // Play status operations: each one updates the play status only

    @Test
    fun `updateStatus updates the play status state`() {
        // Act
        val updated = entry().updateStatus(State.PAUSED)

        // Assert
        assertEquals(State.PAUSED, updated.playStatus.state)
        assertEquals(PlayTime(10, 0), updated.playStatus.timeSpent)
    }

    @Test
    fun `addRun adds a run completed on the given date to the play status`() {
        val updated = entry().addRun(completedOn)

        assertEquals(listOf(Accomplishment(completedOn)), updated.playStatus.completedRuns)
        assertEquals(State.FINISHED, updated.playStatus.state)
    }

    @Test
    fun `addPlatinum registers the platinum with the given date in the play status`() {
        val updated = entry().addPlatinum(completedOn)

        assertEquals(Accomplishment(completedOn), updated.playStatus.platinum)
    }

    @Test
    fun `addPlayTime adds the duration to the play status time spent`() {
        val updated = entry().addPlayTime(90.minutes)

        assertEquals(PlayTime(11, 30), updated.playStatus.timeSpent)
    }

    @Test
    fun `play status operations keep the remaining properties of the entry`() {
        val entry = entry()

        listOf(
            entry.updateStatus(State.PAUSED),
            entry.addRun(completedOn),
            entry.addPlatinum(completedOn),
            entry.addPlayTime(30.minutes)
        ).forEach { updated ->
            assertEquals(game, updated.game)
            assertEquals(setOf(platform), updated.platforms)
            assertEquals(addedAt, updated.addedAt)
            assertNull(updated.sessionStartTime)
        }
    }

    @Test
    fun `operations do not change the original entry`() {
        // Arrange
        val entry = entry()

        // Act
        entry.updateStatus(State.DROPPED)
        entry.addRun(completedOn)
        entry.addPlatinum(completedOn)
        entry.addPlayTime(30.minutes)
        entry.startSession(sessionStart)

        // Assert
        assertEquals(entry(), entry)
    }

    // Play status operations during a session

    @Test
    fun `leaving the PLAYING state during a session is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entryInSession().updateStatus(State.PAUSED)
        }
    }

    @Test
    fun `finishing the game for the first time during a session is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entryInSession().addRun(completedOn)
        }
    }

    @Test
    fun `completing a later run during a session keeps it active`() {
        // Arrange
        val replaying = entry(
            completedRuns = listOf(Accomplishment(LocalDate.of(2025, 1, 1))),
            sessionStartTime = sessionStart
        )

        // Act
        val updated = replaying.addRun(completedOn)

        // Assert
        assertEquals(2, updated.playStatus.completedRuns.size)
        assertEquals(sessionStart, updated.sessionStartTime)
    }

    @Test
    fun `adding play time and the platinum during a session keep it active`() {
        assertTrue(entryInSession().addPlayTime(10.minutes).isSessionActive)
        assertTrue(entryInSession().addPlatinum(completedOn).isSessionActive)
    }

    // Starting sessions

    @Test
    fun `startSession registers its start time`() {
        val updated = entry().startSession(sessionStart)

        assertEquals(sessionStart, updated.sessionStartTime)
        assertTrue(updated.isSessionActive)
    }

    @Test
    fun `startSession sets the state to PLAYING from any state`() {
        val finished = listOf(Accomplishment(completedOn))
        listOf(
            entry(state = State.NONE, timeSpent = PlayTime()),
            entry(state = State.BACKLOG),
            entry(state = State.PAUSED),
            entry(state = State.DROPPED),
            entry(state = State.FINISHED, completedRuns = finished)
        ).forEach { entry ->
            val updated = entry.startSession(sessionStart)

            assertEquals(State.PLAYING, updated.playStatus.state)
            assertEquals(entry.playStatus.completedRuns, updated.playStatus.completedRuns)
        }
    }

    @Test
    fun `startSession when a session is already active is rejected`() {
        assertFailsWith<IllegalStateException> {
            entryInSession().startSession(sessionStart + 1.hours)
        }
    }

    // Elapsed time

    @Test
    fun `sessionElapsedTime is the time since the session started`() {
        assertEquals(95.minutes, entryInSession().sessionElapsedTime(sessionStart + 95.minutes))
    }

    @Test
    fun `sessionElapsedTime without active session is rejected`() {
        assertFailsWith<IllegalStateException> {
            entry().sessionElapsedTime(sessionStart)
        }
    }

    @Test
    fun `sessionElapsedTime before the session started is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entryInSession().sessionElapsedTime(sessionStart - 1.minutes)
        }
    }

    // Ending sessions

    @Test
    fun `endSession at a given time adds the elapsed time and ends the session`() {
        // Act
        val updated = entryInSession().endSession(now = sessionStart + 95.minutes)

        // Assert
        assertEquals(PlayTime(11, 35), updated.playStatus.timeSpent)
        assertFalse(updated.isSessionActive)
        assertEquals(State.PLAYING, updated.playStatus.state)
    }

    @Test
    fun `endSession with a given duration adds it instead of the elapsed time`() {
        // Act
        val updated = entryInSession().endSession(playedFor = 2.hours)

        // Assert
        assertEquals(PlayTime(12, 0), updated.playStatus.timeSpent)
        assertFalse(updated.isSessionActive)
    }

    @Test
    fun `endSession with a zero duration ends the session without adding time`() {
        val updated = entryInSession().endSession(playedFor = Duration.ZERO)

        assertEquals(PlayTime(10, 0), updated.playStatus.timeSpent)
        assertFalse(updated.isSessionActive)
    }

    @Test
    fun `endSession with a negative duration is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entryInSession().endSession(playedFor = (-1).minutes)
        }
    }

    @Test
    fun `endSession before the session started is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            entryInSession().endSession(now = sessionStart - 1.minutes)
        }
    }

    @Test
    fun `endSession without active session has no effect`() {
        val entry = entry()

        assertEquals(entry, entry.endSession(now = sessionStart))
        assertEquals(entry, entry.endSession(playedFor = 1.hours))
    }

    @Test
    fun `ending a session twice adds the elapsed time only once`() {
        // Act
        val updated = entryInSession()
            .endSession(now = sessionStart + 1.hours)
            .endSession(now = sessionStart + 2.hours)

        // Assert
        assertEquals(PlayTime(11, 0), updated.playStatus.timeSpent)
        assertFalse(updated.isSessionActive)
    }

    @Test
    fun `endSession with a negative duration is rejected even without active session`() {
        assertFailsWith<IllegalArgumentException> {
            entry().endSession(playedFor = (-1).minutes)
        }
    }

    @Test
    fun `ending the session allows finishing the game`() {
        val updated = entryInSession()
            .endSession(now = sessionStart + 1.hours)
            .addRun(completedOn)

        assertEquals(State.FINISHED, updated.playStatus.state)
        assertEquals(PlayTime(11, 0), updated.playStatus.timeSpent)
    }

    // Cancelling sessions

    @Test
    fun `cancelSession ends the session without adding time`() {
        val updated = entryInSession().cancelSession()

        assertFalse(updated.isSessionActive)
        assertEquals(PlayTime(10, 0), updated.playStatus.timeSpent)
        assertEquals(State.PLAYING, updated.playStatus.state)
    }

    @Test
    fun `cancelSession without active session has no effect`() {
        val entry = entry()

        assertEquals(entry, entry.cancelSession())
    }
}
