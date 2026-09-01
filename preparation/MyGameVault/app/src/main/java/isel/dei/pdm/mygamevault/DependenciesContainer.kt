package isel.dei.pdm.mygamevault

import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.ui.common.ImageLoader

/**
 * Interface that represents the container for the application's dependencies.
 */
interface DependenciesContainer {
    val searchService: SearchService
    val secretsRepository: SecretsRepository
    val collectionRepository: CollectionRepository
    val imageLoader: ImageLoader
}
