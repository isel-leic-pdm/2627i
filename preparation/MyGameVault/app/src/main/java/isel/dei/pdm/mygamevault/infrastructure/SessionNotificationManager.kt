package isel.dei.pdm.mygamevault.infrastructure

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import isel.dei.pdm.mygamevault.MainActivity
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry

/**
 * Manages the notifications for the play sessions.
 */
class SessionNotificationManager(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private val TAG = MyGameVaultApplication.buildTag("SessionNotificationManager")
        private const val CHANNEL_ID = "play_session_channel"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_GAME_ID = "game_id"
        const val EXTRA_PLATFORM_ID = "platform_id"
    }

    /**
     * Creates the notification channel for play sessions.
     * Should be called when the application starts.
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel: Creating channel $CHANNEL_ID")
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a notification indicating that a play session is in progress.
     * @param entry The collection entry being played.
     */
    fun showSessionNotification(entry: CollectionEntry) {
        Log.d(TAG, "showSessionNotification: entry=${entry.game.name()}, startTime=${entry.sessionStartTime}")
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_GAME_ID, entry.game.id)
            putExtra(EXTRA_PLATFORM_ID, entry.platform.id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_session_title))
            .setContentText(context.getString(R.string.notification_session_content, entry.game.name()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        // Show a live timer if we have a start time
        entry.sessionStartTime?.let { startTime ->
            val epoch = startTime.toEpochMilliseconds()
            Log.d(TAG, "showSessionNotification: Setting chronometer base to $epoch")
            builder.setWhen(epoch)
            builder.setUsesChronometer(true)
            builder.setShowWhen(true)
        } ?: Log.w(TAG, "showSessionNotification: No sessionStartTime found, chronometer will not be shown")

        notificationManager.notify(NOTIFICATION_ID, builder.build())
        Log.d(TAG, "showSessionNotification: Notification posted")
    }

    /**
     * Cancels the play session notification.
     */
    fun cancelSessionNotification() {
        Log.d(TAG, "cancelSessionNotification: Cancelling notification $NOTIFICATION_ID")
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
