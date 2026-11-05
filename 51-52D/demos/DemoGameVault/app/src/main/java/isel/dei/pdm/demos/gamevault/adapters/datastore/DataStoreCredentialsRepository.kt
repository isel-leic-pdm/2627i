package isel.dei.pdm.demos.gamevault.adapters.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * A [CredentialsRepository] that stores credentials in a data store.
 *
 * As required by the contract, the data store's I/O errors are translated into
 * [UnrecoverablePersistenceException]s, both when reading and when writing.
 */
class DataStoreCredentialsRepository(
    private val dataStore: DataStore<Preferences>
) : CredentialsRepository {

    companion object {
        private val CLIENT_ID_KEY = stringPreferencesKey("client_id")
        private val CLIENT_SECRET_KEY = stringPreferencesKey("client_secret")
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")

        /**
         * The corruption handler to be used by the data store passed to the repository. If the
         * file cannot be parsed, its contents are replaced by an empty set of preferences, which
         * means the stored credentials are lost and must be provided again by the user.
         * Without it, every read and write on a corrupted file fails until the app data is cleared.
         */
        val corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
    }

    /**
     * The data store's contents, with read errors translated into persistence exceptions.
     */
    private val preferences: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException)
            throw UnrecoverablePersistenceException("Failed to read credentials", error)
        throw error
    }

    override val secrets: Flow<Secrets?> = preferences.map { preferences ->
        val clientId = preferences[CLIENT_ID_KEY]
        val clientSecret = preferences[CLIENT_SECRET_KEY]
        if (!clientId.isNullOrEmpty() && !clientSecret.isNullOrEmpty()) {
            Secrets(clientId, clientSecret)
        } else {
            null
        }
    }

    override val accessToken: Flow<String?> = preferences.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    override suspend fun saveSecrets(secrets: Secrets) =
        edit(errorMessage = "Failed to save secrets") { preferences ->
            preferences[CLIENT_ID_KEY] = secrets.clientId
            preferences[CLIENT_SECRET_KEY] = secrets.clientSecret
        }

    override suspend fun saveAccessToken(accessToken: String) =
        edit(errorMessage = "Failed to save access token") { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
        }

    /**
     * Edits the data store's contents, translating write errors into persistence exceptions.
     */
    private suspend fun edit(errorMessage: String, transform: suspend (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(transform)
        } catch (e: IOException) {
            throw UnrecoverablePersistenceException(errorMessage, e)
        }
    }
}
