package isel.dei.pdm.demos.gamevault.collection.platforms

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.Platform

/**
 * Represents the possible states of the platform collection screen.
 */
sealed class PlatformCollectionScreenState {

    /**
     * When the platforms in the collection are being loaded.
     */
    data object Loading : PlatformCollectionScreenState()

    /**
     * When the platforms in the collection are available.
     * @property platforms The platforms in the user's collection.
     */
    data class Results(val platforms: List<Platform>) : PlatformCollectionScreenState()

    /**
     * When the platforms in the collection could not be loaded.
     * @property error The error that occurred.
     */
    data class Error(val error: PersistenceException) : PlatformCollectionScreenState()
}
