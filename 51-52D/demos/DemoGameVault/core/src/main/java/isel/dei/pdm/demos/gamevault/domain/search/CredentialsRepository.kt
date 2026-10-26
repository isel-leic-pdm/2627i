package isel.dei.pdm.demos.gamevault.domain.search

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.last

/**
 * Represents the application's secrets (API keys).
 */
data class Secrets(val clientId: String, val clientSecret: String)

/**
 * Contract to be supported by the repository that stores the client's secrets.
 * The application uses a BYOK approach, meaning the user must provide his own client credentials
 * to use when accessing the game search web API.
 */
interface CredentialsRepository {

    /**
     * The application's secrets.
     */
    val secrets: Flow<Secrets?>

    /**
     * The access token to be used when accessing the game search web API.
     */
    val accessToken: Flow<String?>

    /**
     * Saves the application's secrets.
     * @param secrets The secrets to save.
     * @return A result indicating success or failure.
     */
    suspend fun saveSecrets(secrets: Secrets): Result<Unit>

    /**
     * Saves the access token to the repository.
     * @param accessToken The access token to save.
     * @return A result indicating success or failure.
     */
    suspend fun saveAccessToken(accessToken: String): Result<Unit>

    /**
     * Checks if the application is logged in (i.e. if there is a valid access token).
     */
    suspend fun isLoggedIn(): Boolean = accessToken.last() != null
}
