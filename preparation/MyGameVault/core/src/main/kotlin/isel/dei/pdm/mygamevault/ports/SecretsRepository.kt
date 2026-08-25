package isel.dei.pdm.mygamevault.ports

import kotlinx.coroutines.flow.Flow

/**
 * Represents the application's secrets (API keys).
 */
data class Secrets(val clientId: String, val clientSecret: String)

/**
 * Represents the repository for the application's secrets.
 */
interface SecretsRepository {
    /**
     * The application's secrets.
     */
    val secrets: Flow<Secrets?>

    /**
     * Saves the application's secrets.
     * @param secrets The secrets to save.
     * @return A result indicating success or failure.
     */
    suspend fun saveSecrets(secrets: Secrets): Result<Unit>
}
