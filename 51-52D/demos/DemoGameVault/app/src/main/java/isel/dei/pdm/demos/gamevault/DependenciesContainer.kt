package isel.dei.pdm.demos.gamevault

import isel.dei.pdm.demos.gamevault.domain.collection.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService

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
     * The platform collection repository to be used by the app.
     */
    val platformCollectionRepository: PlatformCollectionRepository
}