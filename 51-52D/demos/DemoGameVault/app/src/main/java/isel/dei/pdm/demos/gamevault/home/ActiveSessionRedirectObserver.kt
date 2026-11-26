package isel.dei.pdm.demos.gamevault.home

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Observer of the lifecycle of the home screen that redirects the user to the active play
 * session, if any, each time the screen becomes visible.
 *
 * Sessions are bound to the game details screen, which cannot be left while one is active. Even
 * so, the home screen may be displayed during a session when the app's task is not fully restored,
 * namely after the device restarts (e.g. it shut down due to low battery), after the app is
 * removed from the recent apps, force stopped or updated, or after the details screen crashes.
 * In these cases, the user is silently taken back to the session.
 *
 * @param repository The repository from which the active session is obtained.
 * @param onActiveSession Called with the entry with the active session, to redirect the user to it.
 */
class ActiveSessionRedirectObserver(
    private val repository: GameCollectionRepository,
    private val onActiveSession: (CollectionEntry) -> Unit
) : DefaultLifecycleObserver {

    private var redirect: Job? = null

    override fun onStart(owner: LifecycleOwner) {
        redirect = owner.lifecycleScope.launch {
            val activeSession = try {
                repository.observeActiveSession().first()
            } catch (_: PersistenceException) {
                // The home screen is displayed instead, which reports the failures itself
                null
            }
            activeSession?.let(onActiveSession)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        redirect?.cancel()
        redirect = null
    }
}
