package isel.dei.pdm.demos.gamevault

import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import isel.dei.pdm.demos.gamevault.images.ImageStore
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.ui.common.LocalImageLoader

/**
 * The application's dependencies
 */
interface DependenciesContainer {
    /**
     * The credentials repository to be used by the app.
     */
    val credentialsRepository: CredentialsRepository

    /**
     * The platform search service to be used by the app.
     */
    val platformSearchService: PlatformSearchService

    /**
     * The game search service to be used by the app.
     */
    val gameSearchService: GameSearchService

    /**
     * The platform collection repository to be used by the app.
     */
    val platformCollectionRepository: PlatformCollectionRepository

    /**
     * The game collection repository to be used by the app.
     */
    val gameCollectionRepository: GameCollectionRepository

    /**
     * The image loader to be used by the app's screens to display images (see [LocalImageLoader]).
     * It also displays the copies kept by the [imageStore].
     */
    val imageLoader: ImageLoader

    /**
     * The store of the local copies of the collection's images.
     */
    val imageStore: ImageStore
}