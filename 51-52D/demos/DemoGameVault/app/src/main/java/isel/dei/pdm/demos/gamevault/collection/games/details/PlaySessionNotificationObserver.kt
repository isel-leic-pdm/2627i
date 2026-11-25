package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier

/**
 * Observer of the lifecycle of the game details screen that shows its active play session, if
 * any, in a notification while the screen is not visible (e.g. after the user pressed the home
 * button, or with the device's screen off), and hides it while the screen is visible, as the
 * screen displays the session itself.
 *
 * Sessions are bound to the screen (see [GameDetailsScreen]): they are started and stopped there,
 * and the screen cannot be left while one is active. So, they can only change while the screen is
 * visible, and the notification, posted once the screen stops being visible, is never stale.
 *
 * @param notifier The notifier through which the session is shown.
 * @param activeSession Provides the entry with the screen's active session, or null if there is
 * none (e.g. no session is active, or the game is not displayed yet).
 * @param isChangingConfigurations Provides whether the screen stops being visible because of a
 * configuration change (e.g. a rotation), in which case the session is not shown, as the screen
 * is immediately visible again.
 */
class PlaySessionNotificationObserver(
    private val notifier: PlaySessionNotifier,
    private val activeSession: () -> CollectionEntry?,
    private val isChangingConfigurations: () -> Boolean
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        notifier.hideSession()
    }

    override fun onStop(owner: LifecycleOwner) {
        if (isChangingConfigurations())
            return
        activeSession()
            ?.takeIf { it.isSessionActive }
            ?.let(notifier::showSession)
    }
}
