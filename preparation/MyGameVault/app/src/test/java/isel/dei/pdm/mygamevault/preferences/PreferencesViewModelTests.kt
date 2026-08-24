package isel.dei.pdm.mygamevault.preferences

import isel.dei.pdm.mygamevault.MainDispatcherRule
import isel.dei.pdm.mygamevault.core.Secrets
import isel.dei.pdm.mygamevault.core.StorageAccessException
import isel.dei.pdm.mygamevault.infrastructure.FakeSecretsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for the [PreferencesViewModel] state machine.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @Test
    fun `initial state is Loading and then transitions to Original`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())

        assertTrue("Initial state should be Loading", sut.state.value is PreferencesScreenState.Loading)
        assertFalse("UI should be disabled in Loading state", sut.state.value.isInputEnabled)
        assertFalse("Save should be disabled in Loading state", sut.state.value.isSaveEnabled)

        advanceUntilIdle()

        assertTrue("Should transition to Original after load", sut.state.value is PreferencesScreenState.Original)
        assertTrue("UI should be enabled in Original state", sut.state.value.isInputEnabled)
        assertFalse("Save should be disabled in Original state", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Original to Modified Valid when user types new non-blank values`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())
        advanceUntilIdle()

        sut.onClientIdChange("new-id")
        sut.onClientSecretChange("new-secret")

        assertTrue("Should transition to Modified", sut.state.value is PreferencesScreenState.Modified)
        assertTrue("UI should be enabled in Modified state", sut.state.value.isInputEnabled)
        assertTrue("Save should be enabled for valid modified values", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Original to Modified Invalid when user types blank values`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())
        advanceUntilIdle()

        sut.onClientIdChange(" ") // Blank
        sut.onClientSecretChange("secret")

        assertTrue("Should transition to Modified", sut.state.value is PreferencesScreenState.Modified)
        assertTrue("UI should be enabled in Modified state", sut.state.value.isInputEnabled)
        assertFalse("Save should be disabled for invalid values", sut.state.value.isSaveEnabled)
        assertFalse("Client ID should be marked as invalid", sut.state.value.isClientIdValid)
    }

    @Test
    fun `transition from Modified to Original when user reverts to original values`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())
        advanceUntilIdle() // original is "", ""

        sut.onClientIdChange("new-id")
        assertTrue(sut.state.value is PreferencesScreenState.Modified)

        sut.onClientIdChange("") // Back to original
        assertTrue("Should transition back to Original when reverted", sut.state.value is PreferencesScreenState.Original)
        assertFalse("Save should be disabled in Original state", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Modified Valid to Saving when Save is clicked`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())
        advanceUntilIdle()

        sut.onClientIdChange("id")
        sut.onClientSecretChange("secret")

        sut.onSave()
        advanceTimeBy(1.milliseconds)

        assertTrue("Should transition to Saving", sut.state.value is PreferencesScreenState.Saving)
        assertFalse("UI should be disabled in Saving state", sut.state.value.isInputEnabled)
        assertFalse("Save should be disabled in Saving state", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Saving to Original on successful save`() = runTest(mainDispatcherRule.testDispatcher) {
        val sut = PreferencesViewModel(FakeSecretsRepository())
        advanceUntilIdle()

        sut.onClientIdChange("id")
        sut.onClientSecretChange("secret")

        sut.onSave()
        advanceUntilIdle()

        assertTrue("Should transition back to Original after successful save", sut.state.value is PreferencesScreenState.Original)
        assertEquals("id", sut.state.value.clientId)
        assertEquals("secret", sut.state.value.clientSecret)
        assertFalse("Save should be disabled in Original state", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Saving to Error on failed save`() = runTest(mainDispatcherRule.testDispatcher) {
        val error = StorageAccessException("Disk full")
        val repository = object : FakeSecretsRepository() {
            override suspend fun saveSecrets(secrets: Secrets): Result<Unit> {
                delay(50.milliseconds)
                return Result.failure(error)
            }
        }
        val sut = PreferencesViewModel(repository)
        advanceUntilIdle()

        sut.onClientIdChange("id")
        sut.onClientSecretChange("secret")

        sut.onSave()
        advanceTimeBy(60.milliseconds) // repo delay is 50ms

        assertTrue("Should transition to Error on failure", sut.state.value is PreferencesScreenState.Error)
        assertEquals(error, (sut.state.value as PreferencesScreenState.Error).error)
        assertFalse("UI should be disabled in Error state", sut.state.value.isInputEnabled)
        assertFalse("Save should be disabled in Error state", sut.state.value.isSaveEnabled)
    }

    @Test
    fun `transition from Error to Original after 3 seconds timeout`() = runTest(mainDispatcherRule.testDispatcher) {
        val error = StorageAccessException("Disk full")
        val repository = object : FakeSecretsRepository() {
            override suspend fun saveSecrets(secrets: Secrets): Result<Unit> {
                return Result.failure(error)
            }
        }
        val sut = PreferencesViewModel(repository)
        advanceUntilIdle()

        sut.onClientIdChange("id")
        sut.onClientSecretChange("secret")

        sut.onSave()
        
        // Advance time just enough to reach Error state but not finish the timeout
        advanceTimeBy(1.milliseconds)
        assertTrue("Should be in Error state", sut.state.value is PreferencesScreenState.Error)

        // Advance time past the 3s timeout
        advanceTimeBy(PreferencesViewModel.ERROR_TIMEOUT)
        // No advanceUntilIdle needed here if we use StandardTestDispatcher correctly, 
        // but it doesn't hurt to ensure all pending tasks at this time are run.
        advanceUntilIdle()

        assertTrue("Should revert to Original after timeout", sut.state.value is PreferencesScreenState.Original)
        assertEquals("Should revert to initial values", "", sut.state.value.clientId)
        assertEquals("Should revert to initial values", "", sut.state.value.clientSecret)
    }
}
