package isel.dei.pdm.mygamevault.domain

import java.time.Duration

/**
 * Represents the playing status of a game in the collection.
 * @property timeSpent The total time spent playing the game.
 * @property state The current state of the game in the user's collection.
 */
data class PlayStatus(
    val timeSpent: Duration = Duration.ZERO,
    val state: State = State.NONE
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
        require(!timeSpent.isNegative) { "Time spent cannot be negative" }
        require(!(state == State.NONE && !timeSpent.isZero)) {
            "Time spent must be zero for games in the NONE state"
        }
    }
}
