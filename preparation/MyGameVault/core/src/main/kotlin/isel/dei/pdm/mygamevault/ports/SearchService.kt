package isel.dei.pdm.mygamevault.ports

import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform

/**
 * Contract for services that can be used for searching games.
 */
fun interface SearchService {
    /**
     * Searches for games that match the given [partialName].
     * @param partialName The partial name of the game to search for.
     * @param platform The platform to search for.
     * @param category The category to search for, or null for all.
     * @return A result containing the list of games that match the query.
     */
    suspend fun search(
        partialName: NonBlankString,
        platform: Platform,
        category: Game.Category?
    ): Result<List<Game>>
}
