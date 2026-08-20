package isel.dei.pdm.mygamevault

import isel.dei.pdm.mygamevault.core.SearchService

/**
 * Interface that represents the container for the application's dependencies.
 */
interface DependenciesContainer {
    val searchService: SearchService
}
