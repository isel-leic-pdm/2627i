package isel.dei.pdm.demos.gamevault.home

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary

/**
 * Represents the possible states of the home screen's "now playing" region. The region's state
 * evolves independently of the remaining regions of the screen.
 */
sealed interface NowPlayingState {

    /**
     * When the games being played are being loaded.
     */
    data object Loading : NowPlayingState

    /**
     * When the games being played are available.
     * @property items The items of the games being played, the one with the active session first.
     * @property hasMore Whether more games are being played than those in [items].
     */
    data class Loaded(val items: List<CollectionItem>, val hasMore: Boolean) : NowPlayingState

    /**
     * When the games being played could not be loaded.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : NowPlayingState
}

/**
 * Represents the possible states of the home screen's summary region. The region's state evolves
 * independently of the remaining regions of the screen.
 */
sealed interface SummaryState {

    /**
     * When the summary is being computed.
     */
    data object Loading : SummaryState

    /**
     * When the summary is available.
     * @property summary The summary of the user's accomplishments.
     */
    data class Loaded(val summary: CollectionSummary) : SummaryState

    /**
     * When the summary could not be computed.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : SummaryState
}
