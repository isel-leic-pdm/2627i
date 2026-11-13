package isel.dei.pdm.demos.gamevault.adapters.igdb

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Limits the rate of requests to at most [maxRequests] in any period of length [window] (a
 * sliding window): a request may only start once the one [maxRequests] places before it started
 * at least [window] ago. The requests that have to wait are queued, and start in the order they
 * arrived.
 *
 * Waiting is a suspension, and is therefore cancellable: a request that is canceled while waiting
 * (e.g. a search that became obsolete because the user kept typing) is never sent, and is not
 * counted.
 *
 * @param maxRequests The maximum number of requests started in any period of length [window].
 * @param window The length of the period.
 * @param timeSource The source of the time. Meant to be replaced in tests (e.g. by the virtual
 * time of the test scheduler).
 */
class RequestThrottle(
    private val maxRequests: Int,
    private val window: Duration,
    private val timeSource: TimeSource = TimeSource.Monotonic
) {
    init {
        require(maxRequests > 0) { "The maximum number of requests must be positive" }
        require(window.isPositive()) { "The window must be positive" }
    }

    // Guards the start times. Its queue is fair, so the waiting requests start in arrival order.
    private val mutex = Mutex()

    /**
     * When the most recent requests started, the oldest first (at most [maxRequests]).
     */
    private val startTimes = ArrayDeque<TimeMark>()

    /**
     * Suspends until a request may start, according to the rate limit, and records its start.
     * Meant to be called immediately before sending the request.
     */
    suspend fun acquire() {
        mutex.withLock {
            if (startTimes.size == maxRequests) {
                // Waits, if needed, for the oldest one to leave the window (delay ignores
                // non-positive durations)
                delay(window - startTimes.first().elapsedNow())
                startTimes.removeFirst()
            }
            startTimes.addLast(timeSource.markNow())
        }
    }
}
