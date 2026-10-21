package isel.dei.pdm.demos.gamevault.domain.search.ports

import isel.dei.pdm.demos.gamevault.domain.search.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.GameDetails
import isel.dei.pdm.demos.gamevault.domain.search.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.SearchString

/**
 * Contract for services that can be used for searching games.
 */
interface GameSearchService {
    /**
     * Searches for games that match the given search criteria.
     * @param name The partial (or full) name of the game to search for.
     * @param platform The platform to search for.
     * @param category The category to search for, or null for all.
     * @return A result containing the list of games that match the query.
     */
    suspend fun searchGames(
        name: SearchString,
        platform: PlatformInfo,
        category: GameInfo.Category?
    ): Result<List<GameInfo>>

    /**
     * Fetches the details for the game with the given [gameId], as returned by the search method.
     * @param gameId The ID of the game to fetch details for.
     * @return A result containing the game details, or null if not found.
     */
    suspend fun fetchGameDetails(gameId: Long): Result<GameDetails?>
}
