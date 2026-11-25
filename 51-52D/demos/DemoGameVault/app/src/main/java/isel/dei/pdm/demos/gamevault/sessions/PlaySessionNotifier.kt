package isel.dei.pdm.demos.gamevault.sessions

import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry

/**
 * Contract to be supported by the notifiers of the active play session, which let the user know
 * that a session is active, and for how long, while not looking at the app (e.g. after pressing
 * the home button, or with the screen off).
 *
 * Sessions are bound to the game details screen: they are started and stopped there, and the
 * screen cannot be left while one is active. So, the session is shown while the screen is not
 * visible, and hidden while it is, as the screen displays it itself (see
 * [isel.dei.pdm.demos.gamevault.collection.games.details.GameDetailsActivity]).
 *
 * The contract does not depend on Android types, so that it can be moved to a shared module if
 * the app ever becomes multiplatform.
 */
interface PlaySessionNotifier {
    /**
     * Shows that the session of the given entry is active, with the time elapsed since it started
     * kept up to date by the notifier itself, so that the app does not have to be running. If
     * shown already (e.g. for another game, or before the entry changed), it is replaced. If the
     * entry has no active session, it is hidden instead.
     * @param entry The entry with the active session.
     */
    fun showSession(entry: CollectionEntry)

    /**
     * Stops showing the active session. Has no effect if none is shown.
     */
    fun hideSession()
}
