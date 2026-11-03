package isel.dei.pdm.demos.gamevault.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Error
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Modified
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Original
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenState.Saving
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The view model for the settings screen. The view model is responsible for managing the state
 * of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports setting the secrets required to access the game search web API.
 *
 * @param credentialsRepository The repository used to store the secrets.
 * @param errorTimeout How long the screen remains in the [Error] state before returning to [Original].
 */
class SettingsViewModel(
    private val credentialsRepository: CredentialsRepository,
    private val errorTimeout: Duration = DEFAULT_ERROR_TIMEOUT
) : ViewModel() {

    private val _currentState = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Loading)
    val state: StateFlow<SettingsScreenState>
        get() = _currentState.asStateFlow()

    /**
     * The secrets currently stored in the repository, i.e. the initial values of the fields.
     */
    private var persistedSecrets = Secrets(clientId = "", clientSecret = "")

    init {
        viewModelScope.launch {
            val original = credentialsRepository.secrets.first()
            persistedSecrets = Secrets(
                clientId = original?.clientId ?: "",
                clientSecret = original?.clientSecret ?: ""
            )
            _currentState.value = Original(
                clientId = persistedSecrets.clientId,
                clientSecret = persistedSecrets.clientSecret
            )
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

    fun onSave() {
        val state = _currentState.value
        if (state.isSaveEnabled) {

            _currentState.value = Saving(
                clientId = state.clientId,
                clientSecret = state.clientSecret
            )

            viewModelScope.launch {

                val result = credentialsRepository.saveSecrets(
                    Secrets(
                        clientId = state.clientId,
                        clientSecret = state.clientSecret
                    )
                )
                result.fold(
                    onSuccess = {
                        persistedSecrets = Secrets(
                            clientId = state.clientId,
                            clientSecret = state.clientSecret
                        )
                        _currentState.value = Original(
                            clientId = state.clientId,
                            clientSecret = state.clientSecret
                        )
                    },
                    onFailure = { error ->
                        _currentState.value = Error(
                            error = UnrecoverablePersistenceException(cause = error),
                            clientId = state.clientId,
                            clientSecret = state.clientSecret
                        )
                        delay(errorTimeout)
                        _currentState.value = Original(
                            clientId = persistedSecrets.clientId,
                            clientSecret = persistedSecrets.clientSecret
                        )
                    }
                )
            }

            // some instruction
        }
    }

    companion object {
        /**
         * The default time the screen remains in the [Error] state.
         */
        val DEFAULT_ERROR_TIMEOUT: Duration = 3.seconds

        /**
         * Factory for creating instances of [SettingsViewModel] with the given dependencies.
         */
        fun factory(
            credentialsRepository: CredentialsRepository
        ) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(credentialsRepository) as T
            }
        }
    }
}
