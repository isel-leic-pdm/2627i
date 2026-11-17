package isel.dei.pdm.demos.gamevault.collection.games

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform

/**
 * Represents the possible states of the games collection screen.
 */
sealed class GameCollectionScreenState {

    /**
     * When the query cannot be determined yet, as the platforms of the collection, which the
     * query's platform is one of, are being read.
     */
    data object Loading : GameCollectionScreenState()

    /**
     * When the collection is being browsed with the given query.
     *
     * @property query The query the user browses the collection with, i.e. the latest one, even
     * if its games are still being read.
     * @property sections The games that last met a query, grouped by initial, or null if none
     * were read yet. While the games of a new query are being read, those of the previous one
     * are kept, so that the listing does not flash.
     * @property platforms The platforms the collection can be filtered by: those of the
     * collection that are distinct as far as games are concerned (see
     * [isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms]).
     */
    data class Browsing(
        val query: CollectionQuery,
        val sections: List<CollectionSection>?,
        val platforms: List<Platform>
    ) : GameCollectionScreenState()

    /**
     * When the collection could not be read.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : GameCollectionScreenState()
}
