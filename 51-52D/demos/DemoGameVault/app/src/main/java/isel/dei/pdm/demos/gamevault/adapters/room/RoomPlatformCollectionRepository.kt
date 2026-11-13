package isel.dei.pdm.demos.gamevault.adapters.room

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.NewPlatform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * A [PlatformCollectionRepository] that stores the user's collection in a Room database.
 *
 * Platforms are identified by their abbreviation, name and version, ignoring case. Adding a
 * platform that is already in the collection has no effect: the existing one's identifier is
 * returned.
 *
 * As required by the contract, errors are translated into [PersistenceException]s (see
 * [translate]).
 */
class RoomPlatformCollectionRepository(
    private val dao: PlatformDao
) : PlatformCollectionRepository {

    override suspend fun add(platform: NewPlatform): Long =
        translatingErrors(errorMessage = "Failed to add platform") {
            dao.insert(platform.toEntity())
        }

    override suspend fun getById(id: Long): Platform? =
        translatingErrors(errorMessage = "Failed to read platform") {
            dao.getById(id)?.toPlatform()
        }

    override suspend fun getAll(): List<Platform> =
        translatingErrors(errorMessage = "Failed to read platforms") {
            dao.getAll().map { it.toPlatform() }
        }

    override val platforms: Flow<List<Platform>> = dao.observeAll()
        .map { entities -> entities.map { it.toPlatform() } }
        .catch { error -> throw translate(error, errorMessage = "Failed to read platforms") }
}
