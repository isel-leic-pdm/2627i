package isel.dei.pdm.demos.gamevault.domain.search.platforms

import isel.dei.pdm.demos.gamevault.domain.search.SearchString

/**
 * Contract to be supported by services that can search for platforms.
 */
interface PlatformSearchService {
    /**
     * Searches platforms matching.
     * @param name The partial name of the platform to search for.
     * @param abbreviation The partial abbreviation of the platform to search for.
     * @return A result containing the list of platforms that match the search query.
     */
    suspend fun search(name: SearchString?, abbreviation: SearchString?): Result<List<PlatformInfo>>
}