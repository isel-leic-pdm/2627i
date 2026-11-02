package isel.dei.pdm.demos.gamevault.settings

import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    private companion object {
        const val INITIAL_CLIENT_ID = "client-123"
        const val INITIAL_CLIENT_SECRET = "secret-456"
        const val NEW_CLIENT_ID = "new-client-id"
        const val NEW_CLIENT_SECRET = "new-secret"
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun viewmodel_initializes_and_loads_secrets_into_original_state() = runTest {
        // Arrange
        val initialSecrets = Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET)
        val repository = FakeCredentialsRepository(initialSecrets)

        // Act
        val viewModel = SettingsViewModel(repository)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Original)
        assertEquals(initialSecrets.clientId, currentState.clientId)
        assertEquals(initialSecrets.clientSecret, currentState.clientSecret)
    }

    @Test
    fun changing_client_id_transitions_state_to_modified() = runTest {
        // Arrange
        val initialSecrets = Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET)
        val repository = FakeCredentialsRepository(initialSecrets)
        val viewModel = SettingsViewModel(repository)

        // Act
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Modified)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertTrue(currentState.isSaveEnabled)
    }

    @Test
    fun changing_client_secret_transitions_state_to_modified() = runTest {
        // Arrange
        val initialSecrets = Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET)
        val repository = FakeCredentialsRepository(initialSecrets)
        val viewModel = SettingsViewModel(repository)

        // Act
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Modified)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
        assertTrue(currentState.isSaveEnabled)
    }

    @Test
    fun saving_secrets_transitions_state_to_saving() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)

        // Act
        viewModel.onSave()
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Saving)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
    }

    @Test
    fun saving_secrets_transitions_state_to_original_on_success() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()

        // Act
        repository.completeSave(Result.success(Unit))
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Original)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
    }

    @Test
    fun saving_secrets_transitions_state_to_error_on_failure() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()

        // Act
        repository.completeSave(Result.failure(UnrecoverablePersistenceException("Persistence error")))
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Error)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
    }
}

/**
 * A [CredentialsRepository] implementation whose behavior can be controlled from tests.
 */
class ControlledCredentialsRepository : CredentialsRepository {
    private var saveContinuation: CancellableContinuation<Result<Unit>>? = null

    override val secrets = flowOf<Secrets?>(null)
    override val accessToken = flowOf<String?>(null)

    override suspend fun saveSecrets(secrets: Secrets): Result<Unit> =
        suspendCancellableCoroutine { cont ->
            saveContinuation = cont
        }

    override suspend fun saveAccessToken(accessToken: String): Result<Unit> {
        return Result.success(Unit)
    }

    fun completeSave(result: Result<Unit>) {
        saveContinuation?.resumeWith(Result.success(result))
    }
}
