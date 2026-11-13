package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Represents an entry in the user's game collection.
 * An entry consists of a game, the platforms it's owned on, its playing status,
 * and the date it was added to the collection.
 *
 * The collection has at most one entry per game, and at most one active play session overall
 * (the user does not play two games at the same time). Both rules involve several entries, so
 * they are enforced by the collection's repository, not by this class.
 *
 * The user may own the game on several platforms (e.g. bought for the PS5 and for the PC). The
 * play status is the game's, regardless of the platforms it is played on. Games are oblivious of
 * the platforms' versions (see [Platform.isSameGamePlatformAs]): a game owned on the PS5 Slim is
 * owned on the PS5, whatever its version.
 *
 * Play time is logged through play sessions: starting a session registers its start time, and
 * ending it adds the elapsed time to the time spent playing. Only the total is kept. Play time
 * may also be added directly (e.g. if the user forgot to start a session).
 *
 * @property game The catalog game entry.
 * @property platforms The platforms the game is owned on, in no particular order. There is
 * at least one, and no two of them are versions of the same platform (see [isOwnedOn]). Each one
 * is the version of the collection through which the game was registered as owned on the
 * platform, which is merely a reference: its version is irrelevant.
 * @property playStatus The user's playing status for this game.
 * @property addedAt The date the game was added to the collection.
 * @property sessionStartTime The time when the current play session started, or null if no
 * session is active. While a session is active, the game is in the PLAYING state.
 */
data class CollectionEntry(
    val game: Game,
    val platforms: Set<Platform>,
    val playStatus: PlayStatus = PlayStatus(),
    val addedAt: Instant = Clock.System.now(),
    val sessionStartTime: Instant? = null
) {
    init {
        require(platforms.isNotEmpty()) { "The game must be owned on at least one platform" }
        require(platforms.toList().distinctGamePlatforms().size == platforms.size) {
            "The platforms cannot include versions of the same platform"
        }
        require(sessionStartTime == null || playStatus.state == PlayStatus.State.PLAYING) {
            "Games with an active play session must be in the PLAYING state"
        }
    }

    /**
     * Whether a play session is active.
     */
    val isSessionActive: Boolean
        get() = sessionStartTime != null

    /**
     * Checks whether the game is owned on the given platform, regardless of its version (see
     * [Platform.isSameGamePlatformAs]). For example, a game owned on the PS5 is owned on the PS5
     * Slim.
     * @param platform The platform to check.
     */
    fun isOwnedOn(platform: Platform): Boolean =
        platforms.any { it.isSameGamePlatformAs(platform) }

    /**
     * Registers that the game is also owned on the given platform. The play status is kept, as
     * it is the game's, regardless of the platform. Has no effect if the game is already owned on
     * the platform (see [isOwnedOn]).
     * @param platform The platform the game is also owned on.
     * @return A new [CollectionEntry] owned on the platform, or this entry if it already was.
     */
    fun addPlatform(platform: Platform): CollectionEntry =
        if (isOwnedOn(platform)) this
        else copy(platforms = platforms + platform)

    /**
     * Updates the state of the entry's play status. See [PlayStatus.updateState].
     * @param newState The new state to set.
     * @return A new [CollectionEntry] with the updated status.
     * @throws IllegalArgumentException If the new state is not PLAYING and a session is active
     * (it must be ended first), or if the new state does not meet the play status requirements.
     */
    fun updateStatus(newState: PlayStatus.State): CollectionEntry =
        copy(playStatus = playStatus.updateState(newState))

    /**
     * Adds a completed run to the play status. See [PlayStatus.addRun].
     * @param completedOn The date the run was completed, or null if unknown.
     * @return A new [CollectionEntry] with the updated status.
     * @throws IllegalArgumentException If this is the first completed run and a session is
     * active (it must be ended first, as the game becomes FINISHED).
     */
    fun addRun(completedOn: LocalDate?): CollectionEntry =
        copy(playStatus = playStatus.addRun(completedOn))

    /**
     * Registers the platinum in the play status. See [PlayStatus.addPlatinum].
     * @param obtainedOn The date the platinum was obtained, or null if unknown.
     * @return A new [CollectionEntry] with the updated status.
     */
    fun addPlatinum(obtainedOn: LocalDate?): CollectionEntry =
        copy(playStatus = playStatus.addPlatinum(obtainedOn))

    /**
     * Adds the given [duration] to the total time spent playing.
     * @param duration The duration to add.
     * @return A new [CollectionEntry] with the updated status.
     */
    fun addPlayTime(duration: Duration): CollectionEntry =
        copy(playStatus = playStatus.addPlayTime(duration))

    /**
     * Starts a play session at the given time. The game becomes PLAYING, whatever its state
     * (e.g. it may be resumed, or replayed).
     * @param now The current time.
     * @return A new [CollectionEntry] with the active session.
     * @throws IllegalStateException If a session is already active.
     */
    fun startSession(now: Instant): CollectionEntry {
        check(sessionStartTime == null) { "A play session is already active" }
        return copy(
            playStatus = playStatus.updateState(PlayStatus.State.PLAYING),
            sessionStartTime = now
        )
    }

    /**
     * The time elapsed since the active session started. Meant, for example, to be displayed
     * while playing, or to be suggested to the user when ending the session.
     * @param now The current time.
     * @throws IllegalStateException If no session is active.
     * @throws IllegalArgumentException If [now] is before the session's start.
     */
    fun sessionElapsedTime(now: Instant): Duration {
        val start = checkNotNull(sessionStartTime) { "No play session is active" }
        require(now >= start) { "The current time cannot be before the session's start" }
        return now - start
    }

    // Ending and cancelling sessions are tolerant: if no session is active (e.g. it was already
    // ended), they have no effect, instead of failing. Repeating them is therefore harmless, and
    // the elapsed time is never added twice.

    /**
     * Ends the active session at the given time, adding the elapsed time to the time spent
     * playing. The game remains PLAYING. Has no effect if no session is active.
     * @param now The current time.
     * @return A new [CollectionEntry] without active session, or this entry if no session is
     * active.
     * @throws IllegalArgumentException If [now] is before the session's start.
     */
    fun endSession(now: Instant): CollectionEntry =
        if (!isSessionActive) this
        else endSession(playedFor = sessionElapsedTime(now))

    /**
     * Ends the active session, adding the given duration to the time spent playing, instead of
     * the elapsed time (e.g. if the user corrected it, after forgetting to end the session).
     * The game remains PLAYING. Has no effect if no session is active.
     * @param playedFor The duration of the session.
     * @return A new [CollectionEntry] without active session, or this entry if no session is
     * active.
     * @throws IllegalArgumentException If [playedFor] is negative.
     */
    fun endSession(playedFor: Duration): CollectionEntry {
        require(!playedFor.isNegative()) { "The session's duration cannot be negative" }
        if (!isSessionActive) return this
        return copy(playStatus = playStatus.addPlayTime(playedFor), sessionStartTime = null)
    }

    /**
     * Discards the active session, without adding play time (e.g. if started by mistake).
     * The game remains PLAYING. Has no effect if no session is active.
     * @return A new [CollectionEntry] without active session, or this entry if no session is
     * active.
     */
    fun cancelSession(): CollectionEntry =
        if (!isSessionActive) this
        else copy(sessionStartTime = null)
}
