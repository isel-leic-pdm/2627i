package isel.dei.pdm.demos.gamevault.settings

import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.settings.SettingsViewModel.Companion.DEFAULT_LOADING_INDICATOR_DELAY
import isel.dei.pdm.demos.gamevault.settings.SettingsViewModel.Companion.DEFAULT_MIN_SAVING_DURATION
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private companion object {
        const val INITIAL_CLIENT_ID = "client-123"
        const val INITIAL_CLIENT_SECRET = "secret-456"
        const val NEW_CLIENT_ID = "new-client-id"
        const val NEW_CLIENT_SECRET = "new-secret"
        val INITIAL_SECRETS = Secrets(INITIAL_CLIENT_ID, INITIAL_CLIENT_SECRET)
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Loading

    @Test
    fun viewmodel_initializes_and_loads_secrets_into_original_state() = runTest {
        // Arrange
        val repository = FakeCredentialsRepository(INITIAL_SECRETS)

        // Act
        val viewModel = SettingsViewModel(repository)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.showSaveConfirmation)
        assertTrue(currentState.isInputEnabled)
        assertFalse(currentState.isSaveEnabled)
    }

    @Test
    fun viewmodel_initializes_with_empty_values_and_no_validation_feedback_when_no_secrets_are_stored() =
        runTest {
            // Arrange
            val repository = FakeCredentialsRepository(initialSecrets = null)

            // Act
            val viewModel = SettingsViewModel(repository)
            val currentState = viewModel.state.value

            // Assert
            assertIs<SettingsScreenState.Original>(currentState)
            assertEquals("", currentState.clientId)
            assertEquals("", currentState.clientSecret)
            assertFalse(currentState.isValidationFeedbackVisible)
        }

    @Test
    fun loading_state_initially_does_not_display_the_indicator() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(INITIAL_SECRETS, loadDuration = 1.seconds)

        // Act
        val viewModel = SettingsViewModel(repository)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Loading>(currentState)
        assertFalse(currentState.isIndicatorVisible)
        assertFalse(currentState.isInputEnabled)
        assertFalse(currentState.isBackEnabled)
    }

    @Test
    fun loading_indicator_is_displayed_once_the_delay_elapses() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(INITIAL_SECRETS, loadDuration = 1.seconds)
        val viewModel = SettingsViewModel(repository)

        // Act & Assert
        advanceTimeBy(DEFAULT_LOADING_INDICATOR_DELAY - 1.milliseconds)
        runCurrent()
        assertEquals(SettingsScreenState.Loading(isIndicatorVisible = false), viewModel.state.value)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertEquals(SettingsScreenState.Loading(isIndicatorVisible = true), viewModel.state.value)
    }

    @Test
    fun slow_loading_transitions_state_to_original_once_completed() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(INITIAL_SECRETS, loadDuration = 1.seconds)
        val viewModel = SettingsViewModel(repository)

        // Act
        advanceUntilIdle()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
    }

    @Test
    fun loading_faster_than_the_delay_never_displays_the_indicator() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(
            initialSecrets = INITIAL_SECRETS,
            loadDuration = DEFAULT_LOADING_INDICATOR_DELAY / 2
        )
        val viewModel = SettingsViewModel(repository)
        val states = mutableListOf<SettingsScreenState>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.toList(states)
        }

        // Act
        advanceUntilIdle()

        // Assert
        assertIs<SettingsScreenState.Original>(viewModel.state.value)
        assertFalse(states.any { it is SettingsScreenState.Loading && it.isIndicatorVisible })
    }

    @Test
    fun loading_secrets_transitions_state_to_load_error_on_failure() = runTest {
        // Arrange
        val loadError = UnrecoverablePersistenceException("Storage failure")
        val repository = ControlledCredentialsRepository(loadError = loadError)

        // Act
        val viewModel = SettingsViewModel(repository)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.LoadError>(currentState)
        assertSame(loadError, currentState.error)
        assertFalse(currentState.isInputEnabled)
        assertFalse(currentState.isSaveEnabled)
        assertTrue(currentState.isBackEnabled)
    }

    @Test
    fun changing_fields_in_load_error_state_has_no_effect() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(
            loadError = UnrecoverablePersistenceException("Storage failure")
        )
        val viewModel = SettingsViewModel(repository)

        // Act
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.LoadError>(currentState)
    }

    // Editing

    @Test
    fun changing_client_id_transitions_state_to_modified() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))

        // Act
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertTrue(currentState.isSaveEnabled)
    }

    @Test
    fun changing_client_secret_transitions_state_to_modified() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))

        // Act
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
        assertTrue(currentState.isSaveEnabled)
    }

    @Test
    fun blank_field_in_modified_state_disables_save_and_shows_validation_feedback() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))

        // Act
        viewModel.onClientIdChange(" ")
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertFalse(currentState.isClientIdValid)
        assertFalse(currentState.isSaveEnabled)
        assertTrue(currentState.isValidationFeedbackVisible)
    }

    @Test
    fun restoring_initial_client_id_transitions_state_back_to_original() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.isSaveEnabled)
    }

    @Test
    fun state_remains_modified_while_only_one_field_has_its_initial_value() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)

        // Act
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
    }

    @Test
    fun restoring_invalid_initial_values_keeps_state_modified() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(initialSecrets = null))
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onClientIdChange("")
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertFalse(currentState.isSaveEnabled)
    }

    // Saving

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
        assertIs<SettingsScreenState.Saving>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.isInputEnabled)
        assertFalse(currentState.isBackEnabled)
    }

    @Test
    fun saving_is_ignored_when_save_is_not_enabled() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))

        // Act
        viewModel.onSave()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
    }

    @Test
    fun saving_state_lasts_at_least_the_minimum_duration() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()
        repository.completeSave(Result.success(Unit))

        // Act & Assert
        advanceTimeBy(DEFAULT_MIN_SAVING_DURATION - 1.milliseconds)
        runCurrent()
        assertIs<SettingsScreenState.Saving>(viewModel.state.value)

        advanceTimeBy(1.milliseconds)
        runCurrent()
        assertIs<SettingsScreenState.Original>(viewModel.state.value)
    }

    @Test
    fun saving_slower_than_the_minimum_duration_completes_as_soon_as_save_completes() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()
        advanceTimeBy(DEFAULT_MIN_SAVING_DURATION * 2)
        runCurrent()

        // Act
        repository.completeSave(Result.success(Unit))
        runCurrent()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
    }

    @Test
    fun saving_secrets_transitions_state_to_original_with_confirmation_on_success() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()

        // Act
        repository.completeSave(Result.success(Unit))
        advanceUntilIdle()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
        assertTrue(currentState.showSaveConfirmation)
    }

    @Test
    fun save_confirmation_shown_clears_the_confirmation() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        advanceUntilIdle()

        // Act
        viewModel.onSaveConfirmationShown()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertFalse(currentState.showSaveConfirmation)
    }

    @Test
    fun save_confirmation_shown_when_not_pending_has_no_effect() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onSaveConfirmationShown()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
    }

    @Test
    fun after_successful_save_restoring_saved_values_transitions_state_back_to_original() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        advanceUntilIdle()
        viewModel.onClientIdChange(INITIAL_CLIENT_ID)

        // Act
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
    }

    // Save errors

    @Test
    fun saving_secrets_transitions_state_to_save_error_on_failure() = runTest {
        // Arrange
        val saveError = UnrecoverablePersistenceException("Persistence error")
        val repository = ControlledCredentialsRepository()
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onClientSecretChange(NEW_CLIENT_SECRET)
        viewModel.onSave()

        // Act
        repository.completeSave(Result.failure(saveError))
        advanceUntilIdle()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.SaveError>(currentState)
        // Not assertSame: when an exception is thrown across a suspension point, coroutines' stack
        // trace recovery (active in tests) may replace it with a copy of the same type and message
        assertIs<UnrecoverablePersistenceException>(currentState.error)
        assertEquals(saveError.message, currentState.error.message)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
        assertEquals(NEW_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.isInputEnabled)
        assertTrue(currentState.isBackEnabled)
    }

    @Test
    fun dismissing_save_error_transitions_state_to_original_with_persisted_values() = runTest {
        // Arrange
        val repository = ControlledCredentialsRepository(INITIAL_SECRETS)
        val viewModel = SettingsViewModel(repository)
        viewModel.onClientIdChange(NEW_CLIENT_ID)
        viewModel.onSave()
        repository.completeSave(Result.failure(UnrecoverablePersistenceException("Persistence error")))
        advanceUntilIdle()

        // Act
        viewModel.onSaveErrorDismissed()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Original>(currentState)
        assertEquals(INITIAL_CLIENT_ID, currentState.clientId)
        assertEquals(INITIAL_CLIENT_SECRET, currentState.clientSecret)
        assertFalse(currentState.showSaveConfirmation)
    }

    @Test
    fun dismissing_save_error_when_not_in_save_error_state_has_no_effect() = runTest {
        // Arrange
        val viewModel = SettingsViewModel(FakeCredentialsRepository(INITIAL_SECRETS))
        viewModel.onClientIdChange(NEW_CLIENT_ID)

        // Act
        viewModel.onSaveErrorDismissed()
        val currentState = viewModel.state.value

        // Assert
        assertIs<SettingsScreenState.Modified>(currentState)
        assertEquals(NEW_CLIENT_ID, currentState.clientId)
    }
}

/**
 * A [CredentialsRepository] implementation whose behavior can be controlled from tests.
 * @param initialSecrets The secrets returned when loading succeeds.
 * @param loadError If not null, the error thrown when loading the secrets.
 * @param loadDuration How long loading the secrets takes.
 */
class ControlledCredentialsRepository(
    initialSecrets: Secrets? = null,
    loadError: PersistenceException? = null,
    loadDuration: Duration = Duration.ZERO
) : CredentialsRepository {
    private var saveContinuation: CancellableContinuation<Unit>? = null

    override val secrets: Flow<Secrets?> = flow {
        delay(loadDuration)
        if (loadError != null) throw loadError
        emit(initialSecrets)
    }
    override val accessToken = flowOf<String?>(null)

    override suspend fun saveSecrets(secrets: Secrets) =
        suspendCancellableCoroutine { cont ->
            saveContinuation = cont
        }

    override suspend fun saveAccessToken(accessToken: String) = Unit

    /**
     * Completes the pending save operation: it returns normally if [result] is a success, or
     * throws the exception held by [result] if it is a failure.
     */
    fun completeSave(result: Result<Unit>) {
        saveContinuation?.resumeWith(result)
    }
}
