package isel.dei.pdm.mygamevault.core

/**
 * Base class for exceptions thrown by [SearchService].
 */
sealed class SearchServiceException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)

/**
 * Thrown when the search service could not be authenticated.
 */
class UnauthenticatedException(message: String? = null, cause: Throwable? = null) :
    SearchServiceException(message, cause)

/**
 * Thrown when the search service is unavailable (e.g. server error).
 */
class ServiceUnavailableException(message: String? = null, cause: Throwable? = null) :
    SearchServiceException(message, cause)

/**
 * Thrown when the search service could not be reached (e.g. no internet connection).
 */
class NoConnectivityException(message: String? = null, cause: Throwable? = null) :
    SearchServiceException(message, cause)

/**
 * Thrown when the search service rate limit has been exceeded.
 */
class RateLimitExceededException(message: String? = null, cause: Throwable? = null) :
    SearchServiceException(message, cause)

/**
 * Thrown when an unexpected error occurs in the search service.
 */
class UnexpectedServiceException(message: String? = null, cause: Throwable? = null) :
    SearchServiceException(message, cause)
