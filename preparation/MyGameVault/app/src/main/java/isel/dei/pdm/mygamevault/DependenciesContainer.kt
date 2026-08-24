package isel.dei.pdm.mygamevault

import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.core.SecretsRepository

/**
 * Interface that represents the container for the application's dependencies.
 */
interface DependenciesContainer {
    val searchService: SearchService
    val secretsRepository: SecretsRepository
}
