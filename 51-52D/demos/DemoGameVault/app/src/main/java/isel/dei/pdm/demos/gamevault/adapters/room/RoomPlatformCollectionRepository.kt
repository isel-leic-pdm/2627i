package isel.dei.pdm.demos.gamevault.adapters.room

import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
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
 * As required by the contract, errors are translated into [PersistenceException]s: the database
 * being locked is considered recoverable, while any other database error, or stored data that
 * does not meet the domain requirements, is considered unrecoverable.
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

    /**
     * Executes the given block, translating its errors into persistence exceptions.
     */
    private suspend fun <T> translatingErrors(errorMessage: String, block: suspend () -> T): T =
        try {
            block()
        } catch (e: Exception) {
            throw translate(e, errorMessage)
        }
}

/**
 * Translates the given error into the corresponding persistence exception. Errors that are not
 * related to persistence are returned as is, so that they are rethrown.
 */
private fun translate(error: Throwable, errorMessage: String): Throwable = when (error) {
    is SQLiteDatabaseLockedException -> RecoverablePersistenceException(errorMessage, error)
    is SQLiteException -> UnrecoverablePersistenceException(errorMessage, error)
    // Thrown when the stored data does not meet the domain requirements (i.e. it is corrupted)
    is IllegalArgumentException -> UnrecoverablePersistenceException(errorMessage, error)
    else -> error
}
