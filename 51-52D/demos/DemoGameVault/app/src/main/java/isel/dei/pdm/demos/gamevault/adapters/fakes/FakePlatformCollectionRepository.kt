package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * A fake implementation of the [PlatformCollectionRepository] interface.
 * We use it to have fine control on the repository behavior while developing the app.
 */
class FakePlatformCollectionRepository : PlatformCollectionRepository {

    private val _platforms = MutableStateFlow<List<Platform>>(emptyList())

    override suspend fun add(platform: NewPlatform): Long {
        val current = _platforms.value
        val newId = (current.maxOfOrNull { it.id } ?: 0L) + 1L
        val created = Platform(
            id = newId,
            abbreviation = platform.abbreviation,
            name = platform.name,
            logoUri = platform.logoUri,
            ownedSince = platform.ownedSince,
            version = platform.version,
            source = platform.source
        )
        _platforms.emit(_platforms.value + created)
        return newId
    }

    override suspend fun getById(id: Long): Platform? =
        platforms.first().find { it.id == id }

    override suspend fun getAll(): List<Platform> =
        platforms.first()

    override val platforms: Flow<List<Platform>> = _platforms.asStateFlow()
}
