package isel.dei.pdm.demos.gamevault.search.games

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo

/**
 * Represents the possible states of the search for games screen.
 *
 * Games are searched for on a platform of the user's collection, which is the one the game is
 * added on. Therefore, the search is only possible once the collection's platforms are known
 * ([Loading]), if they can be read ([PlatformsUnavailable]), and if there is at least one
 * ([NoPlatforms]). From then on, the states and their transitions are the same as in the search
 * for platforms screen (see docs/ux/search-for-platform-screen-state-machine.png), with the search
 * being repeated whenever the [GameSearchFilters] change.
 */
sealed class SearchForGamesScreenState {

    /**
     * When the platforms of the user's collection are being read.
     */
    data object Loading : SearchForGamesScreenState()

    /**
     * When the platforms of the user's collection cannot be read, so games cannot be searched for
     * (nor added). Reading them can be retried.
     * @property error The error that occurred.
     */
    data class PlatformsUnavailable(val error: PersistenceException) : SearchForGamesScreenState()

    /**
     * When the user's collection has no platforms, so games cannot be searched for (nor added).
     */
    data object NoPlatforms : SearchForGamesScreenState()

    /**
     * When there is no search query, either because none was entered yet or because it was cleared.
     * @property filters The filters to be applied once a query is entered.
     */
    data class Start(val filters: GameSearchFilters) : SearchForGamesScreenState()

    /**
     * When the search for the given query is in progress.
     * @property query The query being searched for.
     * @property filters The filters applied to the search.
     * @property isIndicatorVisible Whether the loading indicator should be displayed. It is only
     * displayed if the search takes long enough, so that it does not flash on screen when the
     * search is almost instantaneous.
     */
    data class Search(
        val query: SearchString,
        val filters: GameSearchFilters,
        val isIndicatorVisible: Boolean = false
    ) : SearchForGamesScreenState()

    /**
     * When the results of the search for the given query are available.
     * @property query The query that produced the results.
     * @property filters The filters that produced the results.
     * @property items The search results, including whether each one is in the user's collection.
     */
    data class Results(
        val query: SearchString,
        val filters: GameSearchFilters,
        val items: List<GameSearchItemUiState>
    ) : SearchForGamesScreenState()

    /**
     * When the search for the given query failed. The search can be retried.
     * @property query The query whose search failed.
     * @property filters The filters applied to the failed search.
     * @property error The error that occurred.
     */
    data class Error(
        val query: SearchString,
        val filters: GameSearchFilters,
        val error: SearchServiceException
    ) : SearchForGamesScreenState()
}

/**
 * The criteria that narrow down the search for games, besides the query.
 * @property platforms The platforms of the user's collection games can be searched for: one per
 * platform, regardless of its versions, as games are oblivious of them (see
 * [distinctGamePlatforms]). There is at least one.
 * @property selectedPlatform The platform to search games for, or null to search games for all
 * [platforms]. Either way, the results have one item per game and platform it was released for,
 * which is the platform the game is added on.
 * @property category The category of the games to search for, or null for any category.
 * @throws IllegalArgumentException If [platforms] is empty or includes versions of the same
 * platform, or if [selectedPlatform] is not one of them.
 */
data class GameSearchFilters(
    val platforms: List<Platform>,
    val selectedPlatform: Platform? = null,
    val category: GameInfo.Category? = null
) {
    init {
        require(platforms.isNotEmpty()) { "There must be at least one platform" }
        require(platforms.distinctGamePlatforms().size == platforms.size) {
            "The platforms cannot include versions of the same platform"
        }
        require(selectedPlatform == null || selectedPlatform in platforms) {
            "The selected platform must be one of the platforms"
        }
    }

    /**
     * The platforms games are searched for: the selected one, or all of them if none is.
     */
    val searchedPlatforms: List<Platform>
        get() = selectedPlatform?.let { listOf(it) } ?: platforms
}

/**
 * The outcome of adding a game to the user's collection that must be reported to the user,
 * because it cannot be perceived from the search results. Adding games is independent of the
 * screen state: an addition started in [SearchForGamesScreenState.Results] completes even if the
 * search changes meanwhile.
 */
sealed class AddGameOutcome {

    /**
     * The game being added.
     */
    abstract val game: GameInfo

    /**
     * The platform the game is being added on.
     */
    abstract val platform: Platform

    /**
     * The game was added on the platform, but it is no longer displayed in the search results
     * (i.e. the search changed while the game was being added).
     */
    data class Added(override val game: GameInfo, override val platform: Platform) : AddGameOutcome()

    /**
     * The game could not be added. Failures are always reported, regardless of the game being
     * displayed or not.
     * @property error The error that occurred.
     */
    data class Failed(
        override val game: GameInfo,
        override val platform: Platform,
        val error: PersistenceException
    ) : AddGameOutcome()
}
