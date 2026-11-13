package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class PlayStatusTests {

    private val thisYear = Year(2026)
    private val lastYear = LocalDate(2025, 6, 1)
    private val march = LocalDate(2026, 3, 12)
    private val october = LocalDate(2026, 10, 3)

    // PlayTime

    @Test
    fun `PlayTime is zero by default`() {
        // Act
        val playTime = PlayTime()

        // Assert
        assertEquals(0, playTime.hours)
        assertEquals(0, playTime.minutes)
    }

    @Test
    fun `PlayTime accepts the limits of its valid range`() {
        // Act
        val playTime = PlayTime(hours = 1000, minutes = 59)

        // Assert
        assertEquals(1000, playTime.hours)
        assertEquals(59, playTime.minutes)
    }

    @Test
    fun `PlayTime throws IllegalArgumentException when hours are negative`() {
        assertFailsWith<IllegalArgumentException> {
            PlayTime(hours = -1)
        }
    }

    @Test
    fun `PlayTime throws IllegalArgumentException when minutes are negative`() {
        assertFailsWith<IllegalArgumentException> {
            PlayTime(minutes = -1)
        }
    }

    @Test
    fun `PlayTime throws IllegalArgumentException when minutes exceed 59`() {
        assertFailsWith<IllegalArgumentException> {
            PlayTime(minutes = 60)
        }
    }

    @Test
    fun `PlayTime converts to the corresponding duration`() {
        assertEquals(42.hours + 10.minutes, PlayTime(42, 10).toDuration())
    }

    @Test
    fun `Duration converts to PlayTime carrying minutes into hours`() {
        assertEquals(PlayTime(2, 5), 125.minutes.toPlayTime())
    }

    @Test
    fun `Duration converts to PlayTime discarding seconds`() {
        assertEquals(PlayTime(0, 1), (1.minutes + 59.seconds).toPlayTime())
    }

    @Test
    fun `Duration and PlayTime conversions are inverse for whole minutes`() {
        val playTime = PlayTime(87, 45)

        assertEquals(playTime, playTime.toDuration().toPlayTime())
    }

    // Accomplishment

    @Test
    fun `Accomplishment is in the year of its date`() {
        assertTrue(Accomplishment(march).isIn(thisYear))
        assertFalse(Accomplishment(march).isIn(Year(2025)))
    }

    @Test
    fun `undated Accomplishment is in no year`() {
        assertFalse(Accomplishment(null).isIn(thisYear))
    }

    @Test
    fun `Accomplishments are ordered chronologically with the undated ones first`() {
        val accomplishments = listOf(
            Accomplishment(october), Accomplishment(null), Accomplishment(march)
        )

        assertEquals(
            listOf(Accomplishment(null), Accomplishment(march), Accomplishment(october)),
            accomplishments.sortedWith(Accomplishment.chronologically)
        )
    }

    // PlayStatus construction

    @Test
    fun `PlayStatus has no state, no time spent and no accomplishments by default`() {
        // Act
        val status = PlayStatus()

        // Assert
        assertEquals(State.NONE, status.state)
        assertEquals(PlayTime(), status.timeSpent)
        assertTrue(status.completedRuns.isEmpty())
        assertNull(status.platinum)
        assertFalse(status.isFinished)
    }

    @Test
    fun `PlayStatus throws IllegalArgumentException when time is spent in the NONE state`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(timeSpent = PlayTime(minutes = 1), state = State.NONE)
        }
    }

    @Test
    fun `PlayStatus throws IllegalArgumentException when runs are completed in the NONE state`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(state = State.NONE, completedRuns = listOf(Accomplishment(march)))
        }
    }

    @Test
    fun `PlayStatus throws IllegalArgumentException when the platinum is obtained in the NONE state`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(state = State.NONE, platinum = Accomplishment(march))
        }
    }

    @Test
    fun `PlayStatus throws IllegalArgumentException when FINISHED without completed runs`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(state = State.FINISHED)
        }
    }

    @Test
    fun `PlayStatus keeps accomplishments in the remaining states`() {
        listOf(State.BACKLOG, State.PLAYING, State.PAUSED, State.DROPPED).forEach { state ->
            // Act
            val status = PlayStatus(
                timeSpent = PlayTime(3, 30),
                state = state,
                completedRuns = listOf(Accomplishment(march)),
                platinum = Accomplishment(october)
            )

            // Assert
            assertEquals(state, status.state)
            assertTrue(status.isFinished)
            assertEquals(Accomplishment(october), status.platinum)
        }
    }

    // Adding play time

    @Test
    fun `addPlayTime adds the duration to the time spent`() {
        // Arrange
        val status = PlayStatus(PlayTime(1, 50), State.PLAYING)

        // Act
        val updated = status.addPlayTime(25.minutes)

        // Assert
        assertEquals(PlayTime(2, 15), updated.timeSpent)
        assertEquals(State.PLAYING, updated.state)
    }

    @Test
    fun `addPlayTime is rejected in the NONE state`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus().addPlayTime(30.minutes)
        }
    }

    @Test
    fun `addPlayTime is rejected when the resulting time spent is negative`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(PlayTime(0, 10), State.PLAYING).addPlayTime((-20).minutes)
        }
    }

    // Updating the state

    @Test
    fun `updateState changes the state keeping time spent and accomplishments`() {
        // Arrange
        val status = PlayStatus(
            timeSpent = PlayTime(5, 0),
            state = State.PLAYING,
            completedRuns = listOf(Accomplishment(march)),
            platinum = Accomplishment(october)
        )

        // Act
        val updated = status.updateState(State.PAUSED)

        // Assert
        assertEquals(State.PAUSED, updated.state)
        assertEquals(PlayTime(5, 0), updated.timeSpent)
        assertEquals(status.completedRuns, updated.completedRuns)
        assertEquals(status.platinum, updated.platinum)
    }

    @Test
    fun `updateState to FINISHED without completed runs is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(PlayTime(20, 0), State.PLAYING).updateState(State.FINISHED)
        }
    }

    @Test
    fun `updateState to FINISHED with completed runs keeps them`() {
        // Arrange
        val runs = listOf(Accomplishment(march), Accomplishment(october))
        val status = PlayStatus(PlayTime(40, 0), State.PLAYING, completedRuns = runs)

        // Act
        val updated = status.updateState(State.FINISHED)

        // Assert
        assertEquals(State.FINISHED, updated.state)
        assertEquals(runs, updated.completedRuns)
    }

    @Test
    fun `updateState from NONE to BACKLOG is accepted`() {
        val updated = PlayStatus().updateState(State.BACKLOG)

        assertEquals(State.BACKLOG, updated.state)
    }

    @Test
    fun `updateState back to NONE is rejected when time was spent`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus(PlayTime(1, 0), State.PLAYING).updateState(State.NONE)
        }
    }

    @Test
    fun `replaying a game keeps its accomplishments`() {
        // Arrange
        val status = PlayStatus(
            timeSpent = PlayTime(90, 0),
            state = State.FINISHED,
            completedRuns = listOf(Accomplishment(march)),
            platinum = Accomplishment(march)
        )

        // Act
        val updated = status.updateState(State.PLAYING)

        // Assert
        assertEquals(State.PLAYING, updated.state)
        assertEquals(Accomplishment(march), updated.platinum)
        assertTrue(updated.isFinished)
    }

    // Adding runs

    @Test
    fun `addRun on the first run registers its date and sets the state to FINISHED`() {
        val updated = PlayStatus(PlayTime(30, 0), State.PLAYING).addRun(march)

        assertEquals(State.FINISHED, updated.state)
        assertEquals(listOf(Accomplishment(march)), updated.completedRuns)
        assertTrue(updated.isFinished)
    }

    @Test
    fun `addRun on the first run from the NONE state sets the state to FINISHED`() {
        val updated = PlayStatus().addRun(march)

        assertEquals(State.FINISHED, updated.state)
        assertEquals(listOf(Accomplishment(march)), updated.completedRuns)
    }

    @Test
    fun `addRun without date registers an undated run and sets the state to FINISHED`() {
        val updated = PlayStatus(PlayTime(30, 0), State.BACKLOG).addRun(null)

        assertEquals(State.FINISHED, updated.state)
        assertEquals(listOf(Accomplishment(null)), updated.completedRuns)
        assertTrue(updated.isFinished)
    }

    @Test
    fun `addRun on later runs appends it and keeps the state`() {
        // Arrange
        val status = PlayStatus(PlayTime(60, 0), State.PLAYING, listOf(Accomplishment(march)))

        // Act
        val updated = status.addRun(october)

        // Assert
        assertEquals(State.PLAYING, updated.state)
        assertEquals(listOf(Accomplishment(march), Accomplishment(october)), updated.completedRuns)
    }

    @Test
    fun `addRun keeps the time spent and the platinum`() {
        // Arrange
        val status = PlayStatus(
            timeSpent = PlayTime(12, 34),
            state = State.PLAYING,
            completedRuns = listOf(Accomplishment(march)),
            platinum = Accomplishment(march)
        )

        // Act
        val updated = status.addRun(october)

        // Assert
        assertEquals(PlayTime(12, 34), updated.timeSpent)
        assertEquals(Accomplishment(march), updated.platinum)
    }

    // Platinum

    @Test
    fun `addPlatinum registers the platinum with its date keeping the state`() {
        val updated = PlayStatus(PlayTime(80, 0), State.FINISHED, listOf(Accomplishment(march)))
            .addPlatinum(october)

        assertEquals(Accomplishment(october), updated.platinum)
        assertEquals(State.FINISHED, updated.state)
    }

    @Test
    fun `addPlatinum when already registered replaces its date`() {
        val updated = PlayStatus(PlayTime(80, 0), State.PLAYING, platinum = Accomplishment(march))
            .addPlatinum(october)

        assertEquals(Accomplishment(october), updated.platinum)
    }

    @Test
    fun `addPlatinum without date registers an undated platinum`() {
        val updated = PlayStatus(PlayTime(80, 0), State.PLAYING).addPlatinum(null)

        assertEquals(Accomplishment(null), updated.platinum)
        assertFalse(updated.obtainedPlatinumIn(thisYear))
    }

    @Test
    fun `addPlatinum is rejected in the NONE state`() {
        assertFailsWith<IllegalArgumentException> {
            PlayStatus().addPlatinum(october)
        }
    }

    // Yearly accomplishments

    private fun withRuns(vararg dates: LocalDate?) =
        PlayStatus(PlayTime(50, 0), State.FINISHED, dates.map { Accomplishment(it) })

    @Test
    fun `game first finished this year was first finished in this year and not replayed`() {
        val status = withRuns(march)

        assertTrue(status.wasFirstFinishedIn(thisYear))
        assertFalse(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game first finished last year and finished again this year was replayed this year`() {
        val status = withRuns(lastYear, march)

        assertFalse(status.wasFirstFinishedIn(thisYear))
        assertTrue(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game first finished and replayed this year counts as both`() {
        val status = withRuns(march, october)

        assertTrue(status.wasFirstFinishedIn(thisYear))
        assertTrue(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `the first finish is the earliest run, whatever the order the runs were registered in`() {
        val status = withRuns(march, lastYear)

        assertFalse(status.wasFirstFinishedIn(thisYear))
        assertTrue(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game finished twice on the same date was first finished and replayed`() {
        val status = withRuns(march, march)

        assertTrue(status.wasFirstFinishedIn(thisYear))
        assertTrue(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game with an undated run was first finished in no year and its dated runs are replays`() {
        val status = withRuns(march, null)

        assertFalse(status.wasFirstFinishedIn(thisYear))
        assertTrue(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game with undated runs only was neither first finished nor replayed in any year`() {
        val status = withRuns(null, null)

        assertFalse(status.wasFirstFinishedIn(thisYear))
        assertFalse(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `game never finished was neither first finished nor replayed`() {
        val status = PlayStatus(PlayTime(5, 0), State.PLAYING)

        assertFalse(status.wasFirstFinishedIn(thisYear))
        assertFalse(status.wasReplayedIn(thisYear))
    }

    @Test
    fun `platinum is obtained in the year of its date`() {
        val status = PlayStatus(PlayTime(80, 0), State.PLAYING, platinum = Accomplishment(october))

        assertTrue(status.obtainedPlatinumIn(thisYear))
        assertFalse(status.obtainedPlatinumIn(Year(2025)))
    }

    @Test
    fun `platinum not obtained is obtained in no year`() {
        assertFalse(PlayStatus(PlayTime(1, 0), State.PLAYING).obtainedPlatinumIn(thisYear))
    }
}
