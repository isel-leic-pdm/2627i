package isel.dei.pdm.demos.gamevault.domain.search

import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Represents the application's secrets (API keys).
 */
data class Secrets(val clientId: String, val clientSecret: String)

/**
 * Contract to be supported by the repository that stores the client's secrets.
 * The application uses a BYOK approach, meaning the user must provide his own client credentials
 * to use when accessing the game search web API. They are the credentials of the app's single
 * search provider (see the search services), so there is a single set of them.
 *
 * All operations report failures uniformly, by throwing a [PersistenceException]: the flows when
 * collected, and the suspending functions when called. Implementations must translate the errors
 * of the underlying storage technology into [PersistenceException]s.
 */
interface CredentialsRepository {

    /**
     * The application's secrets, or null if none are stored.
     * Collecting the flow throws a [PersistenceException] if the secrets cannot be read.
     */
    val secrets: Flow<Secrets?>

    /**
     * The access token to be used when accessing the game search web API, or null if none is stored.
     * Collecting the flow throws a [PersistenceException] if the access token cannot be read.
     */
    val accessToken: Flow<String?>

    /**
     * Saves the application's secrets.
     * @param secrets The secrets to save.
     * @throws PersistenceException If the secrets cannot be saved.
     */
    suspend fun saveSecrets(secrets: Secrets)

    /**
     * Saves the access token to the repository.
     * @param accessToken The access token to save.
     * @throws PersistenceException If the access token cannot be saved.
     */
    suspend fun saveAccessToken(accessToken: String)

    /**
     * Checks if the application is logged in (i.e. if there is a valid access token).
     * @throws PersistenceException If the access token cannot be read.
     */
    suspend fun isLoggedIn(): Boolean = accessToken.first() != null
}
