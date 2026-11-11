package isel.dei.pdm.demos.gamevault

import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
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
}