package isel.dei.pdm.demos.gamevault.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.LoadError
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Loading
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Modified
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Original
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.SaveError
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Saving
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * The view model for the settings screen. The view model is responsible for managing the state
 * of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports setting the secrets required to access the game search web API.
 *
 * @param credentialsRepository The repository used to store the secrets.
 * @param minSavingDuration The minimum time the screen remains in the [Saving] state, so that
 * the user can perceive that the values are being saved, even when saving is almost instantaneous.
 * @param loadingIndicatorDelay How long loading must take before the loading indicator is
 * displayed, so that it does not flash on screen when loading is almost instantaneous.
 */
class SettingsViewModel(
    private val credentialsRepository: CredentialsRepository,
    private val minSavingDuration: Duration = DEFAULT_MIN_SAVING_DURATION,
    private val loadingIndicatorDelay: Duration = DEFAULT_LOADING_INDICATOR_DELAY
) : ViewModel() {

    private val _currentState = MutableStateFlow<SettingsScreenState>(Loading())
    val state: StateFlow<SettingsScreenState> = _currentState.asStateFlow()

    /**
     * The secrets currently stored in the repository, i.e. the initial values of the fields.
     */
    private var persistedSecrets = Secrets(clientId = "", clientSecret = "")

    init {
        viewModelScope.launch {
            // The loading indicator is only displayed if loading takes longer than the delay
            val indicatorTimer = launch {
                delay(loadingIndicatorDelay)
                _currentState.value = Loading(isIndicatorVisible = true)
            }
            val loadedState = try {
                val original = credentialsRepository.secrets.first()
                persistedSecrets = Secrets(
                    clientId = original?.clientId ?: "",
                    clientSecret = original?.clientSecret ?: ""
                )
                Original(
                    clientId = persistedSecrets.clientId,
                    clientSecret = persistedSecrets.clientSecret
                )
            } catch (e: PersistenceException) {
                LoadError(error = e)
            }
            // The timer is cancelled before publishing the outcome, so it cannot overwrite it
            indicatorTimer.cancel()
            _currentState.value = loadedState
        }
    }

    /**
     * Implementation of the state transitions that result from the user changing the client ID.
     * If the current state is neither [Original] nor [Modified], no action is taken.
     * @param clientId The new client ID.
     */
    fun onClientIdChange(clientId: String) {
        val state = _currentState.value
        if (state !is Original && state !is Modified)
            return

        onFieldsChange(clientId = clientId, clientSecret = state.clientSecret)
    }

    /**
     * Implementation of the state transitions that result from the user changing the client secret.
     * If the current state is neither [Original] nor [Modified], no action is taken.
     * @param clientSecret The new client secret.
     */
    fun onClientSecretChange(clientSecret: String) {
        val state = _currentState.value
        if (state !is Original && state !is Modified)
            return

        onFieldsChange(clientId = state.clientId, clientSecret = clientSecret)
    }

    /**
     * Computes the next state after a field change. The screen moves to [Modified], unless the
     * fields are valid and hold their initial values, in which case it moves back to [Original].
     */
    private fun onFieldsChange(clientId: String, clientSecret: String) {
        val modified = Modified(clientId = clientId, clientSecret = clientSecret)
        val isValid = modified.isClientIdValid && modified.isClientSecretValid
        val hasInitialValues = Secrets(clientId, clientSecret) == persistedSecrets

        _currentState.value =
            if (isValid && hasInitialValues) Original(clientId = clientId, clientSecret = clientSecret)
            else modified
    }

    /**
     * Implementation of the state transitions that result from the user pressing the save button.
     * The screen moves to [Saving] and then, depending on the outcome, to [Original] (with a save
     * confirmation) or to [SaveError]. The [Saving] state lasts at least [minSavingDuration].
     * If the current state is not [Modified] or saving is not enabled, no action is taken.
     */
    fun onSave() {
        val state = _currentState.value
        if (state is Modified && state.isSaveEnabled) {

            val secrets = Secrets(clientId = state.clientId, clientSecret = state.clientSecret)
            _currentState.value = Saving(
                clientId = secrets.clientId,
                clientSecret = secrets.clientSecret
            )

            viewModelScope.launch {
                // The save must last at least minSavingDuration to prevent flickering
                val minDuration = async { delay(minSavingDuration) }
                val savedState = try {
                    credentialsRepository.saveSecrets(secrets)
                    persistedSecrets = secrets
                    Original(
                        clientId = secrets.clientId,
                        clientSecret = secrets.clientSecret,
                        showSaveConfirmation = true
                    )
                } catch (e: PersistenceException) {
                    SaveError(
                        error = e,
                        clientId = secrets.clientId,
                        clientSecret = secrets.clientSecret
                    )
                }
                minDuration.await()
                _currentState.value = savedState
            }
        }
    }

    /**
     * Implementation of the state transition that results from the user dismissing a save error.
     * The screen moves to [Original] with the values currently stored in the repository.
     * If the current state is not [SaveError], no action is taken.
     */
    fun onSaveErrorDismissed() {
        if (_currentState.value !is SaveError)
            return

        _currentState.value = Original(
            clientId = persistedSecrets.clientId,
            clientSecret = persistedSecrets.clientSecret
        )
    }

    /**
     * Implementation of the state transition that results from the save confirmation having been
     * shown to the user. The screen remains in [Original], without the confirmation.
     * If the current state is not [Original] with a pending confirmation, no action is taken.
     */
    fun onSaveConfirmationShown() {
        val state = _currentState.value
        if (state !is Original || !state.showSaveConfirmation)
            return

        _currentState.value = Original(clientId = state.clientId, clientSecret = state.clientSecret)
    }

    companion object {
        /**
         * The default minimum time the screen remains in the [Saving] state.
         */
        val DEFAULT_MIN_SAVING_DURATION: Duration = 500.milliseconds

        /**
         * The default time loading must take before the loading indicator is displayed.
         */
        val DEFAULT_LOADING_INDICATOR_DELAY: Duration = 300.milliseconds

        /**
         * Factory for creating instances of [SettingsViewModel] with the given dependencies.
         */
        fun factory(credentialsRepository: CredentialsRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { SettingsViewModel(credentialsRepository) }
            }
    }
}
