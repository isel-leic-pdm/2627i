package isel.dei.pdm.demos.gamevault.adapters.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsActivity
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.home.HomeActivity
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Implementation of the [PlaySessionNotifier] interface that shows the active session as an
 * ongoing notification, with the game's name and a running timer since the session started.
 * The timer is a chronometer kept up to date by the system, so the app does not have to be
 * running (e.g. with the screen off). Tapping the notification opens the game's screen. Long
 * sessions are shown with a text that asks whether the user is still playing.
 *
 * Since Android 16, the notification is a Live Update (i.e. a promoted ongoing notification), so
 * the timer is also shown in a chip in the status bar. Otherwise, the status bar shows its icon.
 *
 * The notification is posted in its own channel ([CHANNEL_ID]), of low importance, as it is
 * meant to be glanced at: it neither makes sound nor pops up. The channel is created along with
 * the notifier (creating an existing channel has no effect).
 *
 * Since Android 13, notifications require the user's permission (POST_NOTIFICATIONS), which is up
 * to the UI to ask for. Without it, the session is simply not shown.
 *
 * Since Android 14, the user may dismiss ongoing notifications. Dismissing the notification
 * cancels the reminder of the session (see [PlaySessionDismissedReceiver]), so that the app does
 * not post it again by itself, as the guidelines of Live Updates require.
 *
 * @param context The context used to post the notifications (e.g. the application).
 * @param clock The clock used to determine whether the sessions are long.
 */
class AndroidPlaySessionNotifier(
    private val context: Context,
    private val clock: Clock = Clock.System
) : PlaySessionNotifier {

    private val manager = NotificationManagerCompat.from(context)

    init {
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.play_session_channel_name))
            .setDescription(context.getString(R.string.play_session_channel_description))
            .setShowBadge(false)
            .build()
        manager.createNotificationChannel(channel)
    }

    override fun showSession(entry: CollectionEntry) {
        val start = entry.sessionStartTime
        if (start == null) {
            hideSession()
            return
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_session)
            .setContentTitle(entry.game.name.value)
            .setContentText(contentText(entry, start))
            // The chronometer counts the time elapsed since the notification's time, both in the
            // notification and in the status bar's chip
            .setWhen(start.toEpochMilliseconds())
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openGameIntent(entry.game.id))
            .setDeleteIntent(PlaySessionDismissedReceiver.intent(context))
            .build()

        // Since Android 13, notifications can only be posted if the user granted the permission
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && (
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
            )
        ) {
            return
        }
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun hideSession() {
        manager.cancel(NOTIFICATION_ID)
    }

    /**
     * The text of the notification of the given entry's session, which started at [start]: whether
     * it is in progress or, if long, whether the user is still playing.
     */
    private fun contentText(entry: CollectionEntry, start: Instant): String =
        // If the device's clock was set back, the session is taken as just started
        if (entry.isLongSession(maxOf(clock.now(), start))) {
            context.getString(
                R.string.play_session_notification_long_text,
                CollectionEntry.LONG_SESSION.inWholeHours
            )
        } else {
            context.getString(R.string.play_session_notification_text)
        }

    /**
     * The intent that opens the given game's screen, above the home screen, so that going back
     * from it leads to the app, not out of it. The app's task, if any, is replaced, as the screen
     * of the game being played is the one the user was in (see GameDetailsScreen).
     */
    private fun openGameIntent(gameId: Long): PendingIntent =
        PendingIntent.getActivities(
            context,
            0,
            arrayOf(
                Intent(context, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                GameDetailsActivity.intent(context, gameId)
            ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    companion object {
        /**
         * The identifier of the channel of the play session's notification.
         */
        const val CHANNEL_ID = "play_session"

        /**
         * The identifier of the play session's notification. There is a single one, as there is
         * at most one active session.
         */
        const val NOTIFICATION_ID = 1
    }
}
