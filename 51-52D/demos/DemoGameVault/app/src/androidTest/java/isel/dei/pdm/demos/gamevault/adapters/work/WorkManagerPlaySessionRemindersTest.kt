package isel.dei.pdm.demos.gamevault.adapters.work

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import isel.dei.pdm.demos.gamevault.adapters.work.WorkManagerPlaySessionReminders.Companion.WORK_NAME
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Tests for [WorkManagerPlaySessionReminders], with WorkManager's test implementation, which
 * keeps delayed work enqueued (i.e. the reminders are scheduled, but never run).
 */
class WorkManagerPlaySessionRemindersTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val game = Game(1L, "Elden Ring")
    private val sessionStart = Instant.parse("2026-11-25T21:00:00Z")
    private val entry = CollectionEntry(game = game, platforms = setOf(ps5))

    /**
     * The current time, according to the scheduler.
     */
    private var now = sessionStart + 10.minutes

    private var hidden = 0

    private lateinit var workManager: WorkManager
    private lateinit var reminders: WorkManagerPlaySessionReminders

    @Before
    fun setUp() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build()
        )
        workManager = WorkManager.getInstance(context)
        reminders = WorkManagerPlaySessionReminders(
            workManager = workManager,
            notifier = object : PlaySessionNotifier {
                override fun showSession(entry: CollectionEntry) {}
                override fun hideSession() {
                    hidden++
                }
            },
            clock = object : Clock {
                override fun now(): Instant = this@WorkManagerPlaySessionRemindersTest.now
            }
        )
    }

    private fun workInfos(): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWork(WORK_NAME).get()

    @Test
    fun the_reminder_is_scheduled_for_when_the_session_becomes_long() {
        reminders.schedule(entry.startSession(sessionStart))

        val work = workInfos().single()
        assertEquals(WorkInfo.State.ENQUEUED, work.state)
        assertEquals(
            (CollectionEntry.LONG_SESSION - 10.minutes).inWholeMilliseconds,
            work.initialDelayMillis
        )
    }

    @Test
    fun the_reminder_of_a_long_session_is_due_immediately() {
        now = sessionStart + 5.hours

        reminders.schedule(entry.startSession(sessionStart))

        assertEquals(0L, workInfos().single().initialDelayMillis)
    }

    @Test
    fun scheduling_another_reminder_replaces_the_scheduled_one() {
        reminders.schedule(entry.startSession(sessionStart))

        reminders.schedule(entry.startSession(sessionStart + 1.hours))

        val pending = workInfos().filter { !it.state.isFinished }
        assertEquals(1, pending.size)
    }

    @Test
    fun an_entry_without_session_is_not_scheduled() {
        reminders.schedule(entry)

        assertTrue(workInfos().isEmpty())
    }

    @Test
    fun cancelling_cancels_the_scheduled_reminder_and_hides_the_shown_one() {
        reminders.schedule(entry.startSession(sessionStart))

        reminders.cancel()

        assertEquals(WorkInfo.State.CANCELLED, workInfos().single().state)
        assertEquals(1, hidden)
    }
}
