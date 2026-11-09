package isel.dei.pdm.demos.gamevault.home

/**
 * The summary of the user's accomplishments displayed on the home screen. Time spent playing is
 * deliberately not included: it is only displayed per game. All figures count games (not runs).
 *
 * DRAFT: The values will be computed from the collection, once the view model exists.
 *
 * @property finishedThisYear The number of games finished for the first time during the current
 * year.
 * @property replayedThisYear The number of games already finished before, that were finished
 * again during the current year.
 * @property platinumThisYear The number of games whose platinum (all achievements, e.g. trophies)
 * was obtained during the current year.
 * @property owned The number of games in the collection.
 * @property played The number of games that were started at some point, whatever their state now.
 * @property finished The number of games finished at least once.
 * @property platinum The number of games whose platinum was obtained.
 */
data class HomeStats(
    val finishedThisYear: Int,
    val replayedThisYear: Int,
    val platinumThisYear: Int,
    val owned: Int,
    val played: Int,
    val finished: Int,
    val platinum: Int
)
