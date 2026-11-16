package isel.dei.pdm.demos.gamevault.domain.collection.games

/**
 * How far the user went in a game, regardless of what they are currently doing with it (see
 * [PlayStatus.state]): a game being replayed is still finished, and a game whose platinum was
 * obtained is also finished, as the platinum requires a completed run (see [PlayStatus]).
 *
 * Used to browse the collection by what was accomplished (see [CollectionQuery.completion]).
 */
enum class Completion {
    /** No run was completed yet. */
    NOT_FINISHED,

    /** At least one run was completed, whether the platinum was obtained or not. */
    FINISHED,

    /** The platinum was obtained (hence, the game is also finished). */
    PLATINUM
}

/**
 * Whether the play status reached the given [completion]. Reaching the platinum implies being
 * finished, so a game whose platinum was obtained meets both [Completion.FINISHED] and
 * [Completion.PLATINUM].
 */
fun PlayStatus.meets(completion: Completion): Boolean =
    when (completion) {
        Completion.NOT_FINISHED -> !isFinished
        Completion.FINISHED -> isFinished
        Completion.PLATINUM -> platinum != null
    }
