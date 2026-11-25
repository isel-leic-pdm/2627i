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

/**
 * Implementation of the [PlaySessionNotifier] interface that shows the active session as an
 * ongoing notification, with the game's name and a running timer since the session started.
 * The timer is a chronometer kept up to date by the system, so the app does not have to be
 * running (e.g. with the screen off). Tapping the notification opens the game's screen.
 *
 * The notification is posted in its own channel ([CHANNEL_ID]), of low importance, as it is
 * meant to be glanced at: it neither makes sound nor pops up. The channel is created along with
 * the notifier (creating an existing channel has no effect).
 *
 * Since Android 13, notifications require the user's permission (POST_NOTIFICATIONS), which is up
 * to the UI to ask for. Without it, the session is simply not shown.
 *
 * Since Android 14, the user may dismiss ongoing notifications. A dismissed notification is only
 * shown again when the session changes (e.g. the next session).
 *
 * @param context The context used to post the notifications (e.g. the application).
 */
class AndroidPlaySessionNotifier(private val context: Context) : PlaySessionNotifier {

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
            .setContentText(context.getString(R.string.play_session_notification_text))
            // The chronometer counts the time elapsed since the notification's time
            .setWhen(start.toEpochMilliseconds())
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openGameIntent(entry.game.id))
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
