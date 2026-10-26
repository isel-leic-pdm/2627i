package isel.dei.pdm.demos.gamevault.search.platforms.adapters

import isel.dei.pdm.demos.gamevault.domain.collection.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.PlatformCollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.last

/**
 * A fake implementation of the [PlatformCollectionRepository] interface.
 * We use it to have fine control on the repository behavior while developing the app.
 */
class FakePlatformCollectionRepository : PlatformCollectionRepository {

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())

    override suspend fun add(platform: NewPlatform): Result<Long> {
        val current = _platforms.value
        val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        val created = Platform(
            id = newId,
            abbreviation = platform.abbreviation,
            name = platform.name,
            logoUri = platform.logoUri,
            ownedSince = platform.ownedSince
        )
        _platforms.emit(_platforms.value + created)
        return Result.success(newId)
    }

    override suspend fun getById(id: Long): Result<Platform?> =
        Result.success(platforms.last().find { it.id == id })

    override suspend fun getAll(): Result<List<Platform>> =
        Result.success(platforms.last())

    override val platforms: Flow<List<Platform>> = _platforms.asStateFlow()
}
