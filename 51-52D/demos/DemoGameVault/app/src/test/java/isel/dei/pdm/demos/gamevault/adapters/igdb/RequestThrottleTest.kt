package isel.dei.pdm.demos.gamevault.adapters.igdb

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [RequestThrottle], in virtual time: the test's current time is when each request
 * starts.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RequestThrottleTest {

    private val window = 1.seconds

    @Test
    fun requests_within_the_limit_start_immediately() = runTest {
        // Arrange
        val sut = RequestThrottle(maxRequests = 3, window, testScheduler.timeSource)

        // Act
        repeat(3) { sut.acquire() }

        // Assert
        assertEquals(0, currentTime)
    }

    @Test
    fun request_beyond_the_limit_waits_for_the_oldest_one_to_leave_the_window() = runTest {
        // Arrange
        val sut = RequestThrottle(maxRequests = 2, window, testScheduler.timeSource)
        sut.acquire()
        delay(300.milliseconds)
        sut.acquire()

        // Act & Assert - The window slides: each one waits for the one two places before it
        sut.acquire()
        assertEquals(1_000, currentTime)
        sut.acquire()
        assertEquals(1_300, currentTime)
    }

    @Test
    fun requests_after_a_pause_start_immediately() = runTest {
        // Arrange
        val sut = RequestThrottle(maxRequests = 2, window, testScheduler.timeSource)
        repeat(2) { sut.acquire() }
        delay(5.seconds)

        // Act
        sut.acquire()

        // Assert
        assertEquals(5_000, currentTime)
    }

    @Test
    fun waiting_requests_start_in_arrival_order() = runTest {
        // Arrange
        val sut = RequestThrottle(maxRequests = 1, window, testScheduler.timeSource)
        sut.acquire()
        val started = mutableListOf<Pair<Int, Long>>()

        // Act
        val requests = (1..3).map { request ->
            launch { sut.acquire(); started += request to currentTime }
        }
        requests.joinAll()

        // Assert
        assertEquals(listOf(1 to 1_000L, 2 to 2_000L, 3 to 3_000L), started)
    }

    @Test
    fun request_canceled_while_waiting_is_not_counted() = runTest {
        // Arrange
        val sut = RequestThrottle(maxRequests = 1, window, testScheduler.timeSource)
        sut.acquire()
        val canceled = launch { sut.acquire() }
        runCurrent()

        // Act - e.g. a search that became obsolete while waiting
        canceled.cancel()
        sut.acquire()

        // Assert - It starts when the canceled one would have, not a window later
        assertEquals(1_000, currentTime)
    }
}
