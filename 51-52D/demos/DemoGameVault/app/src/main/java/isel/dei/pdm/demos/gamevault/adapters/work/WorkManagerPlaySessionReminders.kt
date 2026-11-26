package isel.dei.pdm.demos.gamevault.adapters.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import isel.dei.pdm.demos.gamevault.DependenciesContainer
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionReminders
import isel.dei.pdm.demos.gamevault.sessions.remindOfActiveSession
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Implementation of the [PlaySessionReminders] interface that schedules the reminders as
 * WorkManager work, delayed until the sessions become long, which runs [PlaySessionReminderWorker].
 *
 * WorkManager keeps the work across process deaths and device restarts, without the app holding a
 * wake lock: the system wakes the device to run it. The work may be delayed (e.g. by Doze, while
 * the device is idle overnight), and overdue work runs once the device is restarted (e.g. after
 * its battery ran out). Late reminders still show the time elapsed since the sessions started.
 *
 * There is at most one active session, so there is a single unique work ([WORK_NAME]), which each
 * scheduling replaces.
 *
 * @param workManager The work manager through which the reminders are scheduled.
 * @param notifier The notifier through which the reminders are shown, used to hide the one
 * shown, if any, when cancelled.
 * @param clock The clock used to compute the remaining time until the sessions become long.
 */
class WorkManagerPlaySessionReminders(
    private val workManager: WorkManager,
    private val notifier: PlaySessionNotifier,
    private val clock: Clock = Clock.System
) : PlaySessionReminders {

    override fun schedule(entry: CollectionEntry) {
        val start = entry.sessionStartTime ?: return
        val delay = (start + CollectionEntry.LONG_SESSION - clock.now())
            .coerceAtLeast(Duration.ZERO)
        val request = OneTimeWorkRequestBuilder<PlaySessionReminderWorker>()
            .setInitialDelay(delay.inWholeMilliseconds, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    PlaySessionReminderWorker.KEY_GAME_ID to entry.game.id,
                    PlaySessionReminderWorker.KEY_SESSION_START to start.toEpochMilliseconds()
                )
            )
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
        notifier.hideSession()
    }

    companion object {
        /**
         * The name of the unique work of the reminder of the active session.
         */
        const val WORK_NAME = "play_session_reminder"
    }
}

/**
 * The worker that reminds the user of a session once it becomes long, if it is still active (see
 * [remindOfActiveSession]). The session is identified in its input data, by its game's identifier
 * ([KEY_GAME_ID]) and its start time ([KEY_SESSION_START]).
 *
 * Workers are created by WorkManager, so its dependencies are obtained from the application. If
 * the session cannot be read, the work is retried later.
 */
class PlaySessionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val gameId = inputData.getLong(KEY_GAME_ID, NO_VALUE)
        val sessionStart = inputData.getLong(KEY_SESSION_START, NO_VALUE)
        if (gameId == NO_VALUE || sessionStart == NO_VALUE)
            return Result.failure()

        val dependencies = applicationContext as DependenciesContainer
        return try {
            remindOfActiveSession(
                gameRepo = dependencies.gameCollectionRepository,
                notifier = dependencies.playSessionNotifier,
                gameId = gameId,
                sessionStart = Instant.fromEpochMilliseconds(sessionStart)
            )
            Result.success()
        } catch (_: PersistenceException) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_GAME_ID = "gameId"
        const val KEY_SESSION_START = "sessionStart"
        private const val NO_VALUE = Long.MIN_VALUE
    }
}
