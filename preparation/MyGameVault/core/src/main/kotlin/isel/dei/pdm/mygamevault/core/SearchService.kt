package isel.dei.pdm.mygamevault.core

/**
 * Contract for services that can be used for searching games.
 */
fun interface SearchService {
    /**
     * Searches for games that match the given [query].
     * @param query The search query.
     * @return A list of games that match the query.
     */
    suspend fun search(query: String): List<Game>
}
