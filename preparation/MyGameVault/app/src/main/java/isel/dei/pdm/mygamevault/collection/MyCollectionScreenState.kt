package isel.dei.pdm.mygamevault.collection

import isel.dei.pdm.mygamevault.domain.CollectionEntry

/**
 * Represents the possible states of the My Collection screen.
 * @property entries The list of games in the collection.
 * @property error The exception that caused the failure, or null if no error occurred.
 */
sealed class MyCollectionScreenState(
    val entries: List<CollectionEntry> = emptyList(),
    val error: Throwable? = null
) {
    /**
     * The state when no data fetching is in progress.
     */
    class Idle(
        entries: List<CollectionEntry> = emptyList(),
        error: Throwable? = null
    ) : MyCollectionScreenState(entries, error)

    /**
     * The state when data is being fetched.
     */
    class Loading(
        entries: List<CollectionEntry> = emptyList()
    ) : MyCollectionScreenState(entries)
}
