package isel.dei.pdm.demos.gamevault.adapters

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreCredentialsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    @Test
    fun retrieve_secrets_when_empty_returns_null() = runTest {
        // Arrange
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tmpFolder.newFile("test_preferences_secrets_initial.preferences_pb") }
        )
        val sut = DataStoreCredentialsRepository(dataStore)

        // Act
        val secrets = sut.secrets.first()

        // Assert
        assertNull(secrets)
    }

    @Test
    fun retrieve_access_token_when_empty_returns_null() = runTest {
        // Arrange
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tmpFolder.newFile("test_preferences_token_initial.preferences_pb") }
        )
        val sut = DataStoreCredentialsRepository(dataStore)

        // Act
        val accessToken = sut.accessToken.first()

        // Assert
        assertNull(accessToken)
    }

    @Test
    fun save_secrets_when_valid_persists_and_returns_success() = runTest {
        // Arrange
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tmpFolder.newFile("test_preferences_secrets.preferences_pb") }
        )
        val sut = DataStoreCredentialsRepository(dataStore)
        val secrets = Secrets("client-123", "secret-456")

        // Act
        val result = sut.saveSecrets(secrets)

        // Assert
        assertEquals(Result.success(Unit), result)
        assertEquals(secrets, sut.secrets.first())
    }

    @Test
    fun save_access_token_when_valid_persists_and_returns_success() = runTest {
        // Arrange
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tmpFolder.newFile("test_preferences_token.preferences_pb") }
        )
        val sut = DataStoreCredentialsRepository(dataStore)
        val token = "token-xyz"

        // Act
        val result = sut.saveAccessToken(token)

        // Assert
        assertEquals(Result.success(Unit), result)
        assertEquals(token, sut.accessToken.first())
    }
}
