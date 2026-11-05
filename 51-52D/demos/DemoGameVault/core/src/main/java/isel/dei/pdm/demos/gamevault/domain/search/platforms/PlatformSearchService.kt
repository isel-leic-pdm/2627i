package isel.dei.pdm.demos.gamevault.domain.search.platforms

import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString

/**
 * Contract to be supported by services that can search for platforms.
 *
 * All operations report failures uniformly, by throwing a [SearchServiceException].
 * Implementations must translate the errors of the underlying technology (e.g. network or HTTP
 * errors) into [SearchServiceException]s.
 */
interface PlatformSearchService {
    /**
     * Searches for platforms whose name or abbreviation contains the given query
     * (case-insensitive).
     * @param query The partial (or full) name or abbreviation of the platform to search for.
     * @return The list of platforms that match the query, which is empty if none match.
     * @throws SearchServiceException If the search cannot be performed.
     */
    suspend fun search(query: SearchString): List<PlatformInfo>
}
