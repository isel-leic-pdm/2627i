package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString

/**
 * Contract for services that can be used for searching games.
 *
 * The app uses a single search provider (currently IGDB), and implementations are specific to it.
 * Using another provider means replacing the implementation, not supporting several providers at
 * once. Therefore, the platforms of the user's collection that were obtained from a search
 * service are assumed to have been obtained from this provider (see [toSearchCriterion]).
 *
 * All operations report failures uniformly, by throwing a [SearchServiceException].
 * Implementations must translate the errors of the underlying technology (e.g. network or HTTP
 * errors) into [SearchServiceException]s.
 */
interface GameSearchService {
    /**
     * Searches for games that match the given search criteria.
     * @param name The partial (or full) name of the game to search for.
     * @param platforms The platforms to search games for. A game matches if it was released for
     * any of them. Equal criteria (e.g. of versions of the same platform) are allowed. Platforms
     * matched by name that the service does not recognize contribute no games.
     * @param category The category to search for, or null for any category.
     * @return The list of games that match the query, which is empty if none match. Each game is
     * listed once, even if it matches several platforms.
     * @throws IllegalArgumentException If [platforms] is empty.
     * @throws SearchServiceException If the search cannot be performed.
     */
    suspend fun searchGames(
        name: SearchString,
        platforms: List<PlatformCriterion>,
        category: GameInfo.Category?
    ): List<GameInfo>

    /**
     * Fetches the details for the game with the given [gameId], as returned by the search method.
     * @param gameId The ID of the game to fetch details for.
     * @return The game details, or null if not found.
     * @throws SearchServiceException If the details cannot be fetched.
     */
    suspend fun fetchGameDetails(gameId: Long): GameInfoDetails?
}
