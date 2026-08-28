package isel.dei.pdm.mygamevault.adapters

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.ports.Secrets
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.ports.StorageAccessException
import isel.dei.pdm.mygamevault.ports.UnexpectedPersistenceException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

/**
 * An implementation of [SecretsRepository] that uses [DataStore] to persist values.
 * @property dataStore The DataStore instance to use.
 * @property defaultSecrets The default secrets to use if none are found in the DataStore.
 */
class DataStoreSecretsRepository(
    private val dataStore: DataStore<Preferences>,
    private val defaultSecrets: Secrets? = null
) : SecretsRepository {

    private companion object {
        val TAG = MyGameVaultApplication.buildTag("DataStoreSecretsRepository")
        val CLIENT_ID_KEY = stringPreferencesKey("client_id")
        val CLIENT_SECRET_KEY = stringPreferencesKey("client_secret")
    }

    override val secrets: Flow<Secrets?> = dataStore.data.map { preferences ->
        val id = preferences[CLIENT_ID_KEY] ?: defaultSecrets?.clientId
        val secret = preferences[CLIENT_SECRET_KEY] ?: defaultSecrets?.clientSecret
        
        if (id != null && secret != null && id.isNotBlank() && secret.isNotBlank()) {
            Secrets(id, secret)
        } else {
            null
        }
    }

    override suspend fun saveSecrets(secrets: Secrets): Result<Unit> = try {
        Log.d(TAG, "saveSecrets: started")
        dataStore.edit { preferences ->
            preferences[CLIENT_ID_KEY] = secrets.clientId
            preferences[CLIENT_SECRET_KEY] = secrets.clientSecret
        }
        Log.d(TAG, "saveSecrets: completed successfully")
        Result.success(Unit)
    } catch (e: IOException) {
        Log.e(TAG, "saveSecrets: Storage access error occurred", e)
        Result.failure(StorageAccessException("Failed to access local storage", e))
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        Log.wtf(TAG, "saveSecrets: Unexpected error occurred", e)
        Result.failure(UnexpectedPersistenceException("Unexpected error during save", e))
    }
}
