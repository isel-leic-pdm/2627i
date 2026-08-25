package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class PlayStatusTests {

    @Test
    fun `default values are correct`() {
        val status = PlayStatus()
        assertEquals(Duration.ZERO, status.timeSpent)
        assertEquals(PlayStatus.State.NONE, status.state)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative time spent is not allowed`() {
        PlayStatus(timeSpent = Duration.ofMinutes(-1), state = PlayStatus.State.PLAYING)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `non zero time spent is not allowed for NONE state`() {
        PlayStatus(timeSpent = Duration.ofMinutes(1), state = PlayStatus.State.NONE)
    }
}
