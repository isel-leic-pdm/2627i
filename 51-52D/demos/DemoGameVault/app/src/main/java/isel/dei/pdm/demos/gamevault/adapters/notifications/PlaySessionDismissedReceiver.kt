package isel.dei.pdm.demos.gamevault.adapters.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import isel.dei.pdm.demos.gamevault.DependenciesContainer

/**
 * Receives the dismissal of the play session's notification by the user (i.e. its delete intent),
 * which means they do not want to be reminded of the active session, so its reminder is cancelled
 * (see [isel.dei.pdm.demos.gamevault.sessions.PlaySessionReminders]). As there is at most one
 * active session, the dismissal is always about it.
 *
 * Nothing else needs to be kept: the reminder's cancellation is kept by the scheduler itself
 * (e.g. across process deaths and device restarts), and the next session's reminder is scheduled
 * when it starts.
 *
 * It is not exported, as it is only meant to be sent by the system on behalf of the app, through
 * the notification.
 */
class PlaySessionDismissedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        (context.applicationContext as DependenciesContainer).playSessionReminders.cancel()
    }

    companion object {
        /**
         * The intent sent when the play session's notification is dismissed by the user.
         */
        fun intent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, PlaySessionDismissedReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
    }
}
