package isel.dei.pdm.mygamevault.collection

import isel.dei.pdm.mygamevault.domain.CollectionEntry

/**
 * Represents the possible filters for the My Collection screen.
 */
enum class CollectionFilter {
    LATEST,
    PLAYING,
    FINISHED,
    PLATINUM,
    BACKLOG
}

/**
 * Represents the possible states of the My Collection screen.
 * @property entries The list of games in the collection.
 * @property filter The current filter applied to the collection.
 * @property hasMore Whether there are more items to load.
 * @property isLoadingMore Whether a next page is currently being loaded.
 * @property error The exception that caused the failure, or null if no error occurred.
 */
sealed class MyCollectionScreenState(
    val entries: List<CollectionEntry> = emptyList(),
    val filter: CollectionFilter = CollectionFilter.LATEST,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: Throwable? = null
) {
    /**
     * The state when no data fetching is in progress.
     */
    class Idle(
        entries: List<CollectionEntry> = emptyList(),
        filter: CollectionFilter = CollectionFilter.LATEST,
        hasMore: Boolean = false,
        error: Throwable? = null
    ) : MyCollectionScreenState(entries, filter, hasMore, false, error)

    /**
     * The state when data is being fetched.
     */
    class Loading(
        entries: List<CollectionEntry> = emptyList(),
        filter: CollectionFilter = CollectionFilter.LATEST,
        hasMore: Boolean = false,
        isLoadingMore: Boolean = false
    ) : MyCollectionScreenState(entries, filter, hasMore, isLoadingMore)
}
