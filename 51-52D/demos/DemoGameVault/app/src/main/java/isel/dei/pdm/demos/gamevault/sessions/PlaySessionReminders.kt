package isel.dei.pdm.demos.gamevault.sessions

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Instant

/**
 * Contract to be supported by the schedulers of the reminders of play sessions, which remind the
 * user of the active session once it becomes long (see [CollectionEntry.isLongSession]), as it may
 * have been forgotten (e.g. the user went to sleep without stopping it). The reminders are shown
 * through the [PlaySessionNotifier] (see [remindOfActiveSession]).
 *
 * Reminders are scheduled when sessions start, and cancelled when they stop, or when the user
 * dismisses the session's notification, as they do not want to be reminded of it. They must be
 * delivered even if the app is not running, or the device is restarted meanwhile, although they
 * may be late (e.g. while the device is idle), as the time elapsed is shown along with them.
 *
 * The contract does not depend on Android types, so that it can be moved to a shared module if
 * the app ever becomes multiplatform.
 */
interface PlaySessionReminders {
    /**
     * Schedules the reminder of the session of the given entry, for when it becomes long,
     * replacing the one scheduled, if any (there is at most one active session). If the session
     * is long already, the reminder is delivered as soon as possible.
     * @param entry The entry with the active session. If it has none, no action is taken.
     */
    fun schedule(entry: CollectionEntry)

    /**
     * Cancels the reminder of the active session, including the one already delivered, if shown
     * (e.g. the session was stopped, or the user dismissed its notification). Has no effect if
     * there is none.
     */
    fun cancel()
}

/**
 * Reminds the user of the given session, if it is still active, by showing it through the given
 * [notifier] again, even if no longer shown (e.g. after the device restarted). Meant to be run
 * when the reminder is due: the session is read again, so that sessions stopped meanwhile, or
 * replaced by others, are not reminded of, even if their reminders were not cancelled.
 * @param gameRepo The repository where the user's collection is stored.
 * @param notifier The notifier through which the user is reminded.
 * @param gameId The identifier of the game of the session.
 * @param sessionStart The start time of the session, which identifies it.
 * @return Whether the user was reminded.
 * @throws PersistenceException If the session cannot be read.
 */
suspend fun remindOfActiveSession(
    gameRepo: GameCollectionRepository,
    notifier: PlaySessionNotifier,
    gameId: Long,
    sessionStart: Instant
): Boolean {
    val entry = gameRepo.observeEntry(gameId).first()
    if (entry == null || entry.sessionStartTime != sessionStart)
        return false
    notifier.showSession(entry)
    return true
}
