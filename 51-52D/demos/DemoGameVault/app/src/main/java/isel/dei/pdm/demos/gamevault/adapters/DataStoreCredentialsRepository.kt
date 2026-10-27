package isel.dei.pdm.demos.gamevault.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * A [CredentialsRepository] that stores credentials in a data store.
 */
class DataStoreCredentialsRepository(
    private val dataStore: DataStore<Preferences>
) : CredentialsRepository {

    private companion object {
        val CLIENT_ID_KEY = stringPreferencesKey("client_id")
        val CLIENT_SECRET_KEY = stringPreferencesKey("client_secret")
        val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    }

    override val secrets: Flow<Secrets?>
        get() = dataStore.data.map { preferences ->
            val clientId = preferences[CLIENT_ID_KEY]
            val clientSecret = preferences[CLIENT_SECRET_KEY]
            if (!clientId.isNullOrEmpty() && !clientSecret.isNullOrEmpty()) {
                Secrets(clientId, clientSecret)
            } else {
                null
            }
        }

    override val accessToken: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }

    override suspend fun saveSecrets(secrets: Secrets): Result<Unit> =
        try {
            dataStore.edit { preferences ->
                preferences[CLIENT_ID_KEY] = secrets.clientId
                preferences[CLIENT_SECRET_KEY] = secrets.clientSecret
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(UnrecoverablePersistenceException("Failed to save secrets", e))
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Unit> =
        try {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(UnrecoverablePersistenceException("Failed to save access token", e))
        }
}
