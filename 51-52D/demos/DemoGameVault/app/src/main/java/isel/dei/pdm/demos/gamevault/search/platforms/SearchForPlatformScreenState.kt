package isel.dei.pdm.demos.gamevault.search.platforms

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo

/**
 * Represents the possible states of the search for platform screen.
 * See docs/ux/search-for-platform-screen-state-machine.png for the allowed transitions.
 */
sealed class SearchForPlatformScreenState {

    /**
     * When there is no search query, either because none was entered yet or because it was cleared.
     */
    data object Start : SearchForPlatformScreenState()

    /**
     * When the search for the given query is in progress.
     * @property query The query being searched for.
     * @property isIndicatorVisible Whether the loading indicator should be displayed. It is only
     * displayed if the search takes long enough, so that it does not flash on screen when the
     * search is almost instantaneous.
     */
    data class Search(
        val query: SearchString,
        val isIndicatorVisible: Boolean = false
    ) : SearchForPlatformScreenState()

    /**
     * When the results of the search for the given query are available.
     * @property query The query that produced the results.
     * @property items The search results, including whether each one is in the user's collection.
     */
    data class Results(
        val query: SearchString,
        val items: List<PlatformSearchItemUiState>
    ) : SearchForPlatformScreenState()

    /**
     * When the search for the given query failed. The search can be retried.
     * @property query The query whose search failed.
     * @property error The error that occurred.
     */
    data class Error(
        val query: SearchString,
        val error: SearchServiceException
    ) : SearchForPlatformScreenState()
}

/**
 * The outcome of adding a platform to the user's collection that must be reported to the user,
 * because it cannot be perceived from the search results. Adding platforms is independent of the
 * screen state: an addition started in [SearchForPlatformScreenState.Results] completes even if
 * the search changes meanwhile.
 */
sealed class AddPlatformOutcome {

    /**
     * The platform being added.
     */
    abstract val platform: PlatformInfo

    /**
     * The platform was added, but it is no longer displayed in the search results (i.e. the
     * search changed while the platform was being added).
     */
    data class Added(override val platform: PlatformInfo) : AddPlatformOutcome()

    /**
     * The platform could not be added. Failures are always reported, regardless of the platform
     * being displayed or not.
     * @property error The error that occurred.
     */
    data class Failed(
        override val platform: PlatformInfo,
        val error: PersistenceException
    ) : AddPlatformOutcome()
}
