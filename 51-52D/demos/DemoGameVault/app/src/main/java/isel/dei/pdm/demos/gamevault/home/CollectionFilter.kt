package isel.dei.pdm.demos.gamevault.home

import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus

/**
 * The filters with which the collection can be browsed, offered as shortcuts on the home screen.
 *
 * DRAFT: To be moved next to the collection browsing screen, once it exists.
 */
sealed interface CollectionFilter {

    /**
     * The entries in the given play [state].
     */
    data class InState(val state: PlayStatus.State) : CollectionFilter

    /**
     * The entries whose platinum was obtained, whatever their current state.
     */
    data object WithPlatinum : CollectionFilter

    companion object {
        /**
         * The filters offered as shortcuts on the home screen, in the order they are displayed:
         * those of the user's main path (from wanting to play a game to mastering it). Filters of
         * secondary relevance (e.g. paused or dropped games) are only offered when browsing the
         * collection, and entries in the NONE state are not offered, as they were not
         * categorized yet.
         */
        val shortcuts: List<CollectionFilter> =
            listOf(
                InState(PlayStatus.State.PLAYING),
                InState(PlayStatus.State.BACKLOG),
                InState(PlayStatus.State.FINISHED),
                WithPlatinum
            )
    }
}
