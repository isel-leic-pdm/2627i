package isel.dei.pdm.demos.gamevault.search.games

import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.AddGameResult
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfoDetails
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import java.util.Collections

/**
 * A [GameSearchService] whose searches complete immediately, following a script: the n-th search
 * produces the n-th response (the last one is repeated once the script is exhausted). Each
 * response is either a list of results or the error to be thrown.
 */
class ScriptedGameSearchService(
    private val responses: List<Result<List<GameInfo>>>
) : GameSearchService {

    init {
        require(responses.isNotEmpty()) { "At least one response must be scripted" }
    }

    /**
     * The queries searched for, in the order they were requested. Searches run on the main
     * thread, while tests read this list from the instrumentation thread.
     */
    val requestedQueries: MutableList<SearchString> = Collections.synchronizedList(mutableListOf())

    override suspend fun searchGames(
        name: SearchString,
        platforms: List<PlatformCriterion>,
        category: GameInfo.Category?
    ): List<GameInfo> {
        val response = responses[minOf(requestedQueries.size, responses.lastIndex)]
        requestedQueries += name
        return response.getOrThrow()
    }

    override suspend fun fetchGameDetails(gameId: Long): GameInfoDetails? = null
}

/**
 * A [GameCollectionRepository] whose operations complete immediately: those of a
 * [FakeGameCollectionRepository], except that additions may fail.
 * @param addError If not null, the error thrown by every addition. Otherwise, additions succeed.
 */
class ScriptedGameCollectionRepository(
    private val addError: PersistenceException? = null,
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository()
) : GameCollectionRepository by delegate {

    override suspend fun add(
        game: NewGame,
        platform: Platform,
        playStatus: PlayStatus,
        details: NewGameDetails
    ): AddGameResult {
        addError?.let { throw it }
        return delegate.add(game, platform, playStatus, details)
    }
}
