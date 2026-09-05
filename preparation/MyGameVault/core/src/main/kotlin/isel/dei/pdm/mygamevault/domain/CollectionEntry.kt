package isel.dei.pdm.mygamevault.domain

import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Represents an entry in the user's game collection.
 * An entry consists of a game, the platform it's owned on, its playing status,
 * and the date it was added to the collection.
 * @property game The catalog game entry.
 * @property platform The platform the game is owned on.
 * @property playStatus The user's playing status for this game on this platform.
 * @property addedAt The date the game was added to the collection.
 * @property sessionStartTime The time when the current play session started, or null if no session is active.
 */
data class CollectionEntry(
    val game: Game,
    val platform: Platform,
    val playStatus: PlayStatus = PlayStatus(),
    val addedAt: LocalDate = LocalDate.now(),
    val sessionStartTime: Instant? = null
) {
    /**
     * Updates the playing status of the entry.
     * If the status is set to FINISHED or PLATINUM and completed runs is 0,
     * it automatically increments the runs to 1 to satisfy domain invariants.
     * @param newState The new state to set.
     * @return A new [CollectionEntry] with the updated status.
     */
    fun updateStatus(newState: PlayStatus.State): CollectionEntry =
        copy(playStatus = playStatus.updateState(newState))

    /**
     * Adds a new completed run to the play status.
     * @return A new [CollectionEntry] with the updated status.
     */
    fun addRun(): CollectionEntry = copy(playStatus = playStatus.addRun())

    /**
     * Adds the given [duration] to the total time spent playing.
     * @param duration The duration to add.
     * @return A new [CollectionEntry] with the updated status.
     */
    fun addPlayTime(duration: Duration): CollectionEntry =
        copy(playStatus = playStatus.addPlayTime(duration))
}
