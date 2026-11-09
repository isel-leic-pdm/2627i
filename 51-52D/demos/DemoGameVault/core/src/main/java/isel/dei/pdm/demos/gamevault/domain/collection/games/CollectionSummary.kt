package isel.dei.pdm.demos.gamevault.domain.collection.games

import java.time.Year

/**
 * The summary of the accomplishments in the user's collection. All figures count games (not
 * runs), following the rules of [PlayStatus].
 *
 * DRAFT: To be refined along with [GameCollectionRepository].
 *
 * @property year The year the yearly figures refer to.
 * @property finishedInYear The number of games finished for the first time in [year] (see
 * [PlayStatus.wasFirstFinishedIn]).
 * @property replayedInYear The number of games replayed in [year] (see [PlayStatus.wasReplayedIn]).
 * @property platinumInYear The number of games whose platinum was obtained in [year] (see
 * [PlayStatus.obtainedPlatinumIn]).
 * @property owned The number of games in the collection.
 * @property played The number of games that were started at some point, i.e. that are neither
 * uncategorized (NONE) nor in the BACKLOG, or that have time spent or accomplishments.
 * @property finished The number of games finished at least once (see [PlayStatus.isFinished]).
 * @property platinum The number of games whose platinum was obtained.
 */
data class CollectionSummary(
    val year: Year,
    val finishedInYear: Int,
    val replayedInYear: Int,
    val platinumInYear: Int,
    val owned: Int,
    val played: Int,
    val finished: Int,
    val platinum: Int
)
