package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo

/**
 * Contract for services that can be used for searching games.
 *
 * All operations report failures uniformly, by throwing a [SearchServiceException].
 * Implementations must translate the errors of the underlying technology (e.g. network or HTTP
 * errors) into [SearchServiceException]s.
 */
interface GameSearchService {
    /**
     * Searches for games that match the given search criteria.
     * @param name The partial (or full) name of the game to search for.
     * @param platform The platform to search for.
     * @param category The category to search for, or null for any category.
     * @return The list of games that match the query, which is empty if none match.
     * @throws SearchServiceException If the search cannot be performed.
     */
    suspend fun searchGames(
        name: SearchString,
        platform: PlatformInfo,
        category: GameInfo.Category?
    ): List<GameInfo>

    /**
     * Fetches the details for the game with the given [gameId], as returned by the search method.
     * @param gameId The ID of the game to fetch details for.
     * @return The game details, or null if not found.
     * @throws SearchServiceException If the details cannot be fetched.
     */
    suspend fun fetchGameDetails(gameId: Long): GameDetails?
}
