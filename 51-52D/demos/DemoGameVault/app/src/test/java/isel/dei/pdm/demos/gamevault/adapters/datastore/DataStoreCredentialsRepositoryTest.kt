package isel.dei.pdm.demos.gamevault.adapters.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class DataStoreCredentialsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    /**
     * Creates a data store backed by a new file in the temporary folder.
     */
    private fun newDataStore(scope: CoroutineScope, fileName: String): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            corruptionHandler = DataStoreCredentialsRepository.corruptionHandler,
            scope = scope,
            produceFile = { tmpFolder.newFile(fileName) }
        )

    /**
     * Creates a file whose contents cannot be parsed as preferences.
     */
    private fun newCorruptedFile(name: String): File =
        tmpFolder.newFile(name).apply { writeText("not a preferences file") }

    // Reading and writing

    @Test
    fun retrieve_secrets_when_empty_returns_null() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(
            newDataStore(
                backgroundScope,
                "secrets_initial.preferences_pb"
            )
        )

        // Act
        val secrets = sut.secrets.first()

        // Assert
        assertNull(secrets)
    }

    @Test
    fun retrieve_access_token_when_empty_returns_null() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(
            newDataStore(
                backgroundScope,
                "token_initial.preferences_pb"
            )
        )

        // Act
        val accessToken = sut.accessToken.first()

        // Assert
        assertNull(accessToken)
    }

    @Test
    fun save_secrets_persists_them() = runTest {
        // Arrange
        val sut =
            DataStoreCredentialsRepository(newDataStore(backgroundScope, "secrets.preferences_pb"))
        val secrets = Secrets("client-123", "secret-456")

        // Act
        sut.saveSecrets(secrets)

        // Assert
        assertEquals(secrets, sut.secrets.first())
    }

    @Test
    fun save_access_token_persists_it() = runTest {
        // Arrange
        val sut =
            DataStoreCredentialsRepository(newDataStore(backgroundScope, "token.preferences_pb"))
        val token = "token-xyz"

        // Act
        sut.saveAccessToken(token)

        // Assert
        assertEquals(token, sut.accessToken.first())
    }

    @Test
    fun is_logged_in_returns_false_when_there_is_no_access_token() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(
            newDataStore(
                backgroundScope,
                "logged_out.preferences_pb"
            )
        )

        // Act
        val isLoggedIn = sut.isLoggedIn()

        // Assert
        assertFalse(isLoggedIn)
    }

    @Test
    fun is_logged_in_returns_true_when_there_is_an_access_token() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(
            newDataStore(
                backgroundScope,
                "logged_in.preferences_pb"
            )
        )
        sut.saveAccessToken("token-xyz")

        // Act
        val isLoggedIn = sut.isLoggedIn()

        // Assert
        assertTrue(isLoggedIn)
    }

    // Storage failures are reported as persistence exceptions

    @Test
    fun retrieve_secrets_when_storage_fails_throws_persistence_exception() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(FailingDataStore())

        // Act & Assert
        val error = assertFailsWith<UnrecoverablePersistenceException> { sut.secrets.first() }
        assertIs<IOException>(error.cause)
    }

    @Test
    fun retrieve_access_token_when_storage_fails_throws_persistence_exception() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(FailingDataStore())

        // Act & Assert
        val error = assertFailsWith<UnrecoverablePersistenceException> { sut.accessToken.first() }
        assertIs<IOException>(error.cause)
    }

    @Test
    fun save_secrets_when_storage_fails_throws_persistence_exception() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(FailingDataStore())

        // Act & Assert
        val error = assertFailsWith<UnrecoverablePersistenceException> {
            sut.saveSecrets(Secrets("client-123", "secret-456"))
        }
        assertIs<IOException>(error.cause)
    }

    @Test
    fun save_access_token_when_storage_fails_throws_persistence_exception() = runTest {
        // Arrange
        val sut = DataStoreCredentialsRepository(FailingDataStore())

        // Act & Assert
        val error = assertFailsWith<UnrecoverablePersistenceException> {
            sut.saveAccessToken("token-xyz")
        }
        assertIs<IOException>(error.cause)
    }

    // Corrupted storage

    @Test
    fun retrieve_secrets_from_corrupted_file_without_corruption_handler_throws_persistence_exception() =
        runTest {
            // Arrange
            val corruptedFile = newCorruptedFile("corrupted_no_handler.preferences_pb")
            val dataStore = PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { corruptedFile }
            )
            val sut = DataStoreCredentialsRepository(dataStore)

            // Act & Assert
            val error = assertFailsWith<UnrecoverablePersistenceException> { sut.secrets.first() }
            assertIs<CorruptionException>(error.cause)
        }

    @Test
    fun retrieve_secrets_from_corrupted_file_with_corruption_handler_returns_null() = runTest {
        // Arrange
        val corruptedFile = newCorruptedFile("corrupted_read.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = DataStoreCredentialsRepository.corruptionHandler,
            scope = backgroundScope,
            produceFile = { corruptedFile }
        )
        val sut = DataStoreCredentialsRepository(dataStore)

        // Act
        val secrets = sut.secrets.first()

        // Assert
        assertNull(secrets)
    }

    @Test
    fun save_secrets_to_corrupted_file_with_corruption_handler_persists_them() = runTest {
        // Arrange
        val corruptedFile = newCorruptedFile("corrupted_write.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = DataStoreCredentialsRepository.corruptionHandler,
            scope = backgroundScope,
            produceFile = { corruptedFile }
        )
        val sut = DataStoreCredentialsRepository(dataStore)
        val secrets = Secrets("client-123", "secret-456")

        // Act
        sut.saveSecrets(secrets)

        // Assert
        assertEquals(secrets, sut.secrets.first())
    }
}

/**
 * A data store whose reads and writes always fail with an I/O error, as when the underlying
 * storage is not accessible.
 */
private class FailingDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw IOException("Simulated storage failure") }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        throw IOException("Simulated storage failure")
}
