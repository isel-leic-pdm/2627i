package isel.dei.pdm.demos.gamevault.adapters.room

import android.database.sqlite.SQLiteDatabaseLockedException
import android.database.sqlite.SQLiteException
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException

// The translation of the errors of the Room repositories into persistence exceptions, as required
// by the repositories' contracts: the database being locked is considered recoverable, while any
// other database error, or stored data that does not meet the domain requirements, is considered
// unrecoverable.

/**
 * Translates the given error into the corresponding persistence exception. Errors that are not
 * related to persistence are returned as is, so that they are rethrown.
 */
internal fun translate(error: Throwable, errorMessage: String): Throwable = when (error) {
    is PersistenceException -> error
    is SQLiteDatabaseLockedException -> RecoverablePersistenceException(errorMessage, error)
    is SQLiteException -> UnrecoverablePersistenceException(errorMessage, error)
    // Thrown when the stored data does not meet the domain requirements (i.e. it is corrupted)
    is IllegalArgumentException -> UnrecoverablePersistenceException(errorMessage, error)
    else -> error
}

/**
 * Executes the given block, translating its errors into persistence exceptions (see [translate]).
 */
internal suspend fun <T> translatingErrors(errorMessage: String, block: suspend () -> T): T =
    try {
        block()
    } catch (e: Exception) {
        throw translate(e, errorMessage)
    }
