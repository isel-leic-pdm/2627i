package isel.dei.pdm.mygamevault.ports

/**
 * Base class for exceptions related to data persistence.
 */
sealed class PersistenceException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when an error occurs while accessing the local storage.
 */
class StorageAccessException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)

/**
 * Thrown when an unexpected error occurs during a persistence operation.
 */
class UnexpectedPersistenceException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)
