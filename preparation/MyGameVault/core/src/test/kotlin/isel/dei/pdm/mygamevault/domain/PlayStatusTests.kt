package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class PlayStatusTests {

    @Test
    fun `default values are correct`() {
        val status = PlayStatus()
        assertEquals(0, status.timeSpent.hours)
        assertEquals(0, status.timeSpent.minutes)
        assertEquals(PlayStatus.State.NONE, status.state)
        assertEquals(0, status.completedRuns)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative hours are not allowed in PlayTime`() {
        PlayTime(hours = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `minutes greater than 59 are not allowed in PlayTime`() {
        PlayTime(minutes = 60)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative minutes are not allowed in PlayTime`() {
        PlayTime(minutes = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative completed runs are not allowed`() {
        PlayStatus(completedRuns = -1, state = PlayStatus.State.PLAYING)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non zero time spent is not allowed for NONE state`() {
        PlayStatus(timeSpent = PlayTime(hours = 1), state = PlayStatus.State.NONE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non zero completed runs is not allowed for NONE state`() {
        PlayStatus(completedRuns = 1, state = PlayStatus.State.NONE)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `completed runs must be at least 1 for FINISHED state`() {
        PlayStatus(completedRuns = 0, state = PlayStatus.State.FINISHED)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `completed runs must be at least 1 for PLATINUM state`() {
        PlayStatus(completedRuns = 0, state = PlayStatus.State.PLATINUM)
    }

    @Test
    fun `addPlayTime correctly updates time`() {
        val status = PlayStatus(state = PlayStatus.State.PLAYING)
        val updated = status.addPlayTime(90.minutes)
        assertEquals(1, updated.timeSpent.hours)
        assertEquals(30, updated.timeSpent.minutes)
    }

    @Test
    fun `addRun correctly increments completed runs and transitions state if needed`() {
        // Case 1: First run completed from PLAYING state
        val status = PlayStatus(state = PlayStatus.State.PLAYING, completedRuns = 0)
        val updated = status.addRun()
        assertEquals(1, updated.completedRuns)
        assertEquals(PlayStatus.State.FINISHED, updated.state)

        // Case 2: Second run completed from FINISHED state (state should stay FINISHED)
        val status2 = PlayStatus(state = PlayStatus.State.FINISHED, completedRuns = 1)
        val updated2 = status2.addRun()
        assertEquals(2, updated2.completedRuns)
        assertEquals(PlayStatus.State.FINISHED, updated2.state)

        // Case 3: First run completed from PLATINUM state (state should stay PLATINUM)
        val status3 = PlayStatus(state = PlayStatus.State.PLATINUM, completedRuns = 1)
        val updated3 = status3.addRun()
        assertEquals(2, updated3.completedRuns)
        assertEquals(PlayStatus.State.PLATINUM, updated3.state)

        // Case 4: First run completed from NONE state
        val status4 = PlayStatus(state = PlayStatus.State.NONE, completedRuns = 0)
        val updated4 = status4.addRun()
        assertEquals(1, updated4.completedRuns)
        assertEquals(PlayStatus.State.FINISHED, updated4.state)
    }

    @Test
    fun `toDuration and toPlayTime are consistent`() {
        val playTime = PlayTime(hours = 2, minutes = 45)
        val duration = playTime.toDuration()
        assertEquals(165.minutes, duration)
        
        val backToPlayTime = duration.toPlayTime()
        assertEquals(playTime, backToPlayTime)
    }
}
