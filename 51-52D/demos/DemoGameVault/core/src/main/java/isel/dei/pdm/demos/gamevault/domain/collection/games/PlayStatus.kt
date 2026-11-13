package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.Year
import kotlinx.datetime.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Represents the play time of a game in hours and minutes.
 * @property hours The number of hours spent playing.
 * @property minutes The number of minutes spent playing.
 */
data class PlayTime(val hours: Int = 0, val minutes: Int = 0) {
    init {
        require(hours >= 0) { "Hours cannot be negative" }
        require(minutes in 0..59) { "Minutes must be between 0 and 59" }
    }

    /**
     * Converts this play time to a [Duration].
     */
    fun toDuration(): Duration = hours.hours + minutes.minutes
}

/**
 * Converts a [Duration] to a [PlayTime].
 */
fun Duration.toPlayTime(): PlayTime = PlayTime(
    hours = inWholeHours.toInt(),
    minutes = (inWholeMinutes % 60).toInt()
)

/**
 * Represents something the user accomplished in a game, such as completing a run or obtaining
 * the platinum.
 *
 * Accomplishments may be registered after the fact (e.g. the runs completed before the user
 * adopted the app), when their date may no longer be known. Undated accomplishments are assumed
 * to have happened before any dated one, as they typically predate the app: they count as
 * accomplishments, but they are in no year.
 *
 * @property achievedOn The date of the accomplishment, or null if unknown. When registered while
 * playing, it is the current date; otherwise, it is provided by the user, if known.
 */
data class Accomplishment(val achievedOn: LocalDate?) {
    /**
     * Whether the accomplishment happened in the given [year]. Undated accomplishments are in no
     * year.
     */
    fun isIn(year: Year): Boolean = achievedOn != null && achievedOn in year

    companion object {
        /**
         * Orders accomplishments chronologically, the undated ones first (see [Accomplishment]).
         */
        val chronologically: Comparator<Accomplishment> =
            compareBy(nullsFirst()) { it.achievedOn }
    }
}

/**
 * Represents the playing status of a game in the collection: what the user is currently doing
 * with it ([state]) and what was accomplished in it so far ([completedRuns] and [platinum]).
 *
 * Accomplishments are kept regardless of the state: a game that was finished, or whose platinum
 * was obtained, may be played again (i.e. be in the [State.PLAYING] state) without losing them.
 *
 * @property timeSpent The total time spent playing the game.
 * @property state The current state of the game in the user's collection.
 * @property completedRuns The runs completed by the user, in no particular order (runs may be
 * registered after the fact, e.g. when importing a collection). If the user is playing the game
 * and there are no completed runs, they are in their first run.
 * @property platinum The platinum, if obtained: all the game's achievements (e.g. trophies) were
 * obtained. Not to be confused with completing 100% of the game, which is game specific (e.g.
 * visiting all areas or getting all collectibles) and may or may not be expressed in achievements.
 */
data class PlayStatus(
    val timeSpent: PlayTime = PlayTime(),
    val state: State = State.NONE,
    val completedRuns: List<Accomplishment> = emptyList(),
    val platinum: Accomplishment? = null
) {
    /**
     * Represents the possible states of a game in the user's collection, i.e. what the user is
     * currently doing with it.
     */
    enum class State {
        /** The game was added to the collection, but not categorized yet. */
        NONE,

        /** The user intends to play the game. */
        BACKLOG,

        /** The user is currently playing the game. */
        PLAYING,

        /** The user finished the game (at least one run was completed) and is not playing it. */
        FINISHED,

        /** The user stopped playing the game, intending to resume it. */
        PAUSED,

        /** The user stopped playing the game, not intending to resume it. */
        DROPPED
    }

    init {
        require(!(state == State.NONE && (timeSpent.hours != 0 || timeSpent.minutes != 0))) {
            "Time spent must be zero for games in the NONE state"
        }
        require(!(state == State.NONE && completedRuns.isNotEmpty())) {
            "Completed runs must be empty for games in the NONE state"
        }
        require(!(state == State.NONE && platinum != null)) {
            "Games in the NONE state cannot have the platinum"
        }
        require(!(state == State.FINISHED && completedRuns.isEmpty())) {
            "Games in the FINISHED state must have at least one completed run"
        }
    }

    /**
     * Whether the game was finished at least once, whatever its current state.
     */
    val isFinished: Boolean
        get() = completedRuns.isNotEmpty()

    // Yearly accomplishments. The first completed run is the earliest one, whatever the order the
    // runs were registered in. Undated runs are the earliest (see Accomplishment): if there is
    // one, the game was first finished in no known year, and all the dated runs are replays.

    /**
     * Whether the game was finished for the first time in the given [year], i.e. its first
     * completed run (the earliest one) is from that year.
     */
    fun wasFirstFinishedIn(year: Year): Boolean =
        completedRuns.minWithOrNull(Accomplishment.chronologically)?.isIn(year) == true

    /**
     * Whether the game was replayed in the given [year], i.e. a run other than the first one (the
     * earliest one) was completed in that year. A game finished for the first time and replayed
     * in the same year counts as both, as does a game finished twice on the same date.
     */
    fun wasReplayedIn(year: Year): Boolean =
        completedRuns.sortedWith(Accomplishment.chronologically).drop(1).any { it.isIn(year) }

    /**
     * Whether the platinum was obtained in the given [year].
     */
    fun obtainedPlatinumIn(year: Year): Boolean =
        platinum?.isIn(year) == true

    /**
     * Adds the given [duration] to the total time spent playing.
     * @param duration The duration to add.
     * @return A new [PlayStatus] with the updated time spent.
     */
    fun addPlayTime(duration: Duration): PlayStatus {
        val newTotalDuration = timeSpent.toDuration() + duration
        return copy(timeSpent = newTotalDuration.toPlayTime())
    }

    /**
     * Updates the state of the playing status, keeping its accomplishments.
     * A game is finished for the first time by adding a run (see [addRun]), which registers the
     * completion date. Hence, setting the state to FINISHED requires a completed run.
     * @param newState The new state to set.
     * @return A new [PlayStatus] with the updated state.
     * @throws IllegalArgumentException If the new state is FINISHED and there are no completed
     * runs, or if the new state is NONE and there is time spent or accomplishments.
     */
    fun updateState(newState: State): PlayStatus = copy(state = newState)

    /**
     * Adds a completed run, finished on the given date.
     * If this is the first completed run, the state is set to FINISHED.
     * @param completedOn The date the run was completed, or null if unknown (e.g. a run
     * registered after the fact).
     * @return A new [PlayStatus] with the added run.
     */
    fun addRun(completedOn: LocalDate?): PlayStatus {
        val nextState = if (completedRuns.isEmpty()) State.FINISHED else state
        return copy(completedRuns = completedRuns + Accomplishment(completedOn), state = nextState)
    }

    /**
     * Registers the platinum, obtained on the given date. If the platinum was already registered,
     * its date is replaced (e.g. to correct it). The state is not changed.
     * @param obtainedOn The date the platinum was obtained, or null if unknown.
     * @return A new [PlayStatus] with the platinum.
     * @throws IllegalArgumentException If the game is in the NONE state.
     */
    fun addPlatinum(obtainedOn: LocalDate?): PlayStatus =
        copy(platinum = Accomplishment(obtainedOn))
}
