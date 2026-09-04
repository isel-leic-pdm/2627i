package isel.dei.pdm.mygamevault.ports

/**
 * Base class for exceptions related to data persistence.
 */
sealed class PersistenceException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when an error occurs while accessing the local storage and it is considered fatal.
 */
class UnrecoverablePersistenceException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)

/**
 * Thrown when a persistence error occurs that might be recoverable (e.g. temporary lock).
 */
class RecoverablePersistenceException(message: String? = null, cause: Throwable? = null) :
    PersistenceException(message, cause)
