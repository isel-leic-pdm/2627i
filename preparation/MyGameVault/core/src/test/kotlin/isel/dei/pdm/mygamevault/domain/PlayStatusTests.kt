package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PlayStatusTests {

    @Test
    fun `default values are correct`() {
        val status = PlayStatus()
        assertEquals(Duration.ZERO, status.timeSpent)
        assertEquals(PlayStatus.State.NONE, status.state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative time spent is not allowed`() {
        PlayStatus(timeSpent = (-1).minutes, state = PlayStatus.State.PLAYING)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non zero time spent is not allowed for NONE state`() {
        PlayStatus(timeSpent = 1.minutes, state = PlayStatus.State.NONE)
    }
}
