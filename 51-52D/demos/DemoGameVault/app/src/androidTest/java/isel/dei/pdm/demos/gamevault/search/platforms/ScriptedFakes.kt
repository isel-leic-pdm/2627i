package isel.dei.pdm.demos.gamevault.search.platforms

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.PlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import java.util.Collections
import java.util.concurrent.atomic.AtomicInteger

/**
 * A [PlatformSearchService] whose searches complete immediately, following a script: the n-th
 * search produces the n-th response (the last one is repeated once the script is exhausted).
 * Each response is either a list of results or the error to be thrown.
 */
class ScriptedPlatformSearchService(
    private val responses: List<Result<List<PlatformInfo>>>
) : PlatformSearchService {

    init {
        require(responses.isNotEmpty()) { "At least one response must be scripted" }
    }

    /**
     * The queries searched for, in the order they were requested. Searches run on the main
     * thread, while tests read this list from the instrumentation thread.
     */
    val requestedQueries: MutableList<SearchString> = Collections.synchronizedList(mutableListOf())

    override suspend fun search(query: SearchString): List<PlatformInfo> {
        val response = responses[minOf(requestedQueries.size, responses.lastIndex)]
        requestedQueries += query
        return response.getOrThrow()
    }
}

/**
 * A [PlatformCollectionRepository] whose operations complete immediately.
 * @param addError If not null, the error thrown by every addition. Otherwise, additions succeed.
 * @param readErrors The errors thrown when collecting [platforms]: the n-th collection throws the
 * n-th error. Once the list is exhausted, collections succeed.
 * @param initialPlatforms The platforms initially in the collection.
 */
class ScriptedPlatformCollectionRepository(
    private val addError: PersistenceException? = null,
    private val readErrors: List<PersistenceException> = emptyList(),
    initialPlatforms: List<Platform> = emptyList()
) : PlatformCollectionRepository {

    private val _platforms = MutableStateFlow(initialPlatforms)

    /**
     * The number of times [platforms] was collected. Collections run on the main thread, while
     * tests read this value from the instrumentation thread.
     */
    private val _readCount = AtomicInteger(0)

    val readCount: Int
        get() = _readCount.get()

    override val platforms: Flow<List<Platform>> = flow {
        readErrors.getOrNull(_readCount.getAndIncrement())?.let { throw it }
        emitAll(_platforms)
    }

    override suspend fun add(platform: NewPlatform): Long {
        addError?.let { throw it }
        val id = (_platforms.value.maxOfOrNull { it.id } ?: 0L) + 1L
        _platforms.update {
            it + Platform(
                id = id,
                abbreviation = platform.abbreviation,
                name = platform.name,
                logoUri = platform.logoUri,
                ownedSince = platform.ownedSince,
                version = platform.version
            )
        }
        return id
    }

    override suspend fun getById(id: Long): Platform? = _platforms.value.find { it.id == id }

    override suspend fun getAll(): List<Platform> = _platforms.value
}
