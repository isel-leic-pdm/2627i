package isel.dei.pdm.demos.gamevault.settings

import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private companion object {
        const val INITIAL_CLIENT_ID = "client-123"
        const val INITIAL_CLIENT_SECRET = "secret-456"
        const val NEW_CLIENT_ID = "new-client-id"
        const val NEW_CLIENT_SECRET = "new-secret"
        val ERROR_TIMEOUT = 2.seconds
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

    @Test
    fun restoring_initial_client_id_transitions_state_back_to_original() = runTest {
        // Arrange
        val repository = FakeCredentialsRepository(Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET))
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Original)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.isSaveEnabled)
    }

    @Test
    fun state_remains_modified_while_only_one_field_has_its_initial_value() = runTest {
        // Arrange
        val repository = FakeCredentialsRepository(Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET))
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)

        // Act
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Modified)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
    }

    @Test
    fun restoring_invalid_initial_values_keeps_state_modified() = runTest {
        // Arrange
        val repository = FakeCredentialsRepository(initialSecrets = null)
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onClientIdChange("")
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Modified)
        assertFalse(currentState.isSaveEnabled)
    }

    @Test
    fun after_successful_save_restoring_saved_values_transitions_state_back_to_original() = runTest {
        // Arrange
        val repository = FakeCredentialsRepository(Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET))
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)

        // Act
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Original)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
    }

    @Test
    fun error_state_remains_until_timeout_elapses() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET))
        val viewModel = SettingsViewModel(repository, errorTimeout = ERROR_TIMEOUT)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        repository.completeSave(Result.failure(UnrecoverablePersistenceException("Persistence error")))

        // Act
        advanceTimeBy(ERROR_TIMEOUT - 1.milliseconds)
        runCurrent()
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Error)
    }

    @Test
    fun error_state_transitions_to_original_with_persisted_values_after_timeout() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET))
        val viewModel = SettingsViewModel(repository, errorTimeout = ERROR_TIMEOUT)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        repository.completeSave(Result.failure(UnrecoverablePersistenceException("Persistence error")))

        // Act
        advanceTimeBy(ERROR_TIMEOUT)
        runCurrent()
        val currentState = viewModel.state.value

        // Assert
        assertTrue(currentState is SettingsScreenState.Original)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
    }
}

/**
 * A [CredentialsRepository] implementation whose behavior can be controlled from tests.
 */
class ControlledCredentialsRepository(
    initialSecrets: Secrets? = null
) : CredentialsRepository {
    private var saveContinuation: CancellableContinuation<Result<Unit>>? = null

    override val secrets = flowOf(initialSecrets)
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
