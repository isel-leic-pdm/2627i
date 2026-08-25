package isel.dei.pdm.mygamevault.adapters

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.mygamevault.ports.Secrets
import isel.dei.pdm.mygamevault.ports.StorageAccessException
import isel.dei.pdm.mygamevault.ports.UnexpectedPersistenceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.IOException

class DataStoreSecretsRepositoryTests {

    @get:Rule
    internal val dataStoreRule = DataStoreRule(
        context = InstrumentationRegistry.getInstrumentation().targetContext
    )

    private val dataStore: DataStore<Preferences>
        get() = dataStoreRule.dataStore

    private companion object {
        val CLIENT_ID_KEY = stringPreferencesKey("client_id")
        val CLIENT_SECRET_KEY = stringPreferencesKey("client_secret")
    }

    @Test
    fun secrets_whenEmpty_emitsNull() = runTest {
        val sut = DataStoreSecretsRepository(dataStore)
        assertNull(sut.secrets.first())
    }

    @Test
    fun saveSecrets_whenNewValue_updatesTheFlow() = runTest {
        val sut = DataStoreSecretsRepository(dataStore)
        val expectedSecrets = Secrets("test-id", "test-secret")

        val result = sut.saveSecrets(expectedSecrets)
        
        assertTrue(result.isSuccess)
        assertEquals(expectedSecrets, sut.secrets.first())
    }

    @Test
    fun saveSecrets_whenValueSaved_persistsAcrossInstances() = runTest {
        val expectedSecrets = Secrets("persistent-id", "persistent-secret")

        val repo1 = DataStoreSecretsRepository(dataStore)
        repo1.saveSecrets(expectedSecrets)
        
        val repo2 = DataStoreSecretsRepository(dataStore)
        val savedSecrets = repo2.secrets.first()
        
        assertEquals(expectedSecrets, savedSecrets)
    }

    @Test
    fun secrets_whenOnlyClientIdExists_emitsNull() = runTest {
        dataStore.edit { it[CLIENT_ID_KEY] = "some-id" }
        val sut = DataStoreSecretsRepository(dataStore)
        
        assertNull(sut.secrets.first())
    }

    @Test
    fun secrets_whenOnlyClientSecretExists_emitsNull() = runTest {
        dataStore.edit { it[CLIENT_SECRET_KEY] = "some-secret" }
        val sut = DataStoreSecretsRepository(dataStore)
        
        assertNull(sut.secrets.first())
    }

    @Test
    fun secrets_whenDataChanges_emitsNewValue() = runTest {
        val sut = DataStoreSecretsRepository(dataStore)
        val secrets1 = Secrets("id1", "secret1")
        val secrets2 = Secrets("id2", "secret2")

        val emissions = mutableListOf<Secrets?>()
        val job = launch {
            sut.secrets.take(3).toList(emissions)
        }

        sut.saveSecrets(secrets1)
        sut.saveSecrets(secrets2)

        job.join()

        assertEquals(3, emissions.size)
        assertNull(emissions[0])
        assertEquals(secrets1, emissions[1])
        assertEquals(secrets2, emissions[2])
    }

    @Test
    fun saveSecrets_whenIOExceptionOccurs_returnsFailureWithStorageAccessException() = runTest {
        val failingDataStore = FakeDataStore(
            data = flowOf(emptyPreferences()),
            updateDataException = IOException("Disk full")
        )
        val sut = DataStoreSecretsRepository(failingDataStore)
        
        val result = sut.saveSecrets(Secrets("id", "secret"))
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is StorageAccessException)
    }

    @Test
    fun saveSecrets_whenGenericExceptionOccurs_returnsFailureWithUnexpectedPersistenceException() = runTest {
        val failingDataStore = FakeDataStore(
            data = flowOf(emptyPreferences()),
            updateDataException = RuntimeException("Unexpected error")
        )
        val sut = DataStoreSecretsRepository(failingDataStore)
        
        val result = sut.saveSecrets(Secrets("id", "secret"))
        
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnexpectedPersistenceException)
    }

    /**
     * A fake implementation of [DataStore] to simulate errors.
     */
    private class FakeDataStore(
        override val data: Flow<Preferences>,
        private val updateDataException: Throwable? = null
    ) : DataStore<Preferences> {
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            updateDataException?.let { throw it }
            return transform(emptyPreferences())
        }
    }
}

/**
 * A JUnit rule to manage the lifecycle of a DataStore instance during tests.
 */
internal class DataStoreRule(private val context: android.content.Context) : TestWatcher() {

    private lateinit var testDbName: String
    lateinit var dataStore: DataStore<Preferences>
        private set

    override fun starting(description: Description) {
        testDbName = "test_secrets_${System.currentTimeMillis()}_${(0..1000).random()}"
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(testDbName) }
        )
    }

    override fun finished(description: Description) {
        context.preferencesDataStoreFile(testDbName).delete()
    }
}
