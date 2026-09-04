package isel.dei.pdm.mygamevault.domain

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
 * Represents the playing status of a game in the collection.
 * @property timeSpent The total time spent playing the game.
 * @property state The current state of the game in the user's collection.
 * @property completedRuns The number of times the user has completed the game.
 * If the user is playing the game and [completedRuns] is 0, they are in their first run.
 */
data class PlayStatus(
    val timeSpent: PlayTime = PlayTime(),
    val state: State = State.NONE,
    val completedRuns: Int = 0
) {
    /**
     * Represents the possible states of a game in the user's collection.
     */
    @Suppress("unused")
    enum class State {
        NONE,
        BACKLOG,
        PLAYING,
        FINISHED,
        PLATINUM,
        PAUSED,
        DROPPED
    }

    init {
        require(completedRuns >= 0) { "Completed runs cannot be negative" }
        require(!(state == State.NONE && (timeSpent.hours != 0 || timeSpent.minutes != 0))) {
            "Time spent must be zero for games in the NONE state"
        }
        require(!(state == State.NONE && completedRuns != 0)) {
            "Completed runs must be zero for games in the NONE state"
        }
        require(!((state == State.FINISHED || state == State.PLATINUM) && completedRuns == 0)) {
            "Completed runs must be at least 1 for games in the FINISHED or PLATINUM state"
        }
    }

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
     * Updates the state of the playing status.
     * If the status is set to FINISHED or PLATINUM and completed runs is 0,
     * it automatically increments the runs to 1 to satisfy domain invariants.
     * @param newState The new state to set.
     * @return A new [PlayStatus] with the updated state.
     */
    fun updateState(newState: State): PlayStatus {
        val nextRuns = if ((newState == State.FINISHED || newState == State.PLATINUM) &&
            completedRuns == 0
        ) {
            1
        } else {
            completedRuns
        }
        return copy(state = newState, completedRuns = nextRuns)
    }

    /**
     * Adds a new completed run to the play status.
     * If the current state is not FINISHED or PLATINUM and this is the first run
     * being completed, the state is automatically set to FINISHED.
     * @return A new [PlayStatus] with the incremented completed runs.
     */
    fun addRun(): PlayStatus {
        val nextRuns = completedRuns + 1
        val nextState = if (completedRuns == 0 && state != State.FINISHED && state != State.PLATINUM) {
            State.FINISHED
        } else {
            state
        }
        return copy(completedRuns = nextRuns, state = nextState)
    }
}
