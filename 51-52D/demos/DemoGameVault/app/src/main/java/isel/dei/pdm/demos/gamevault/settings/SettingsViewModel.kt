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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The view model for the settings screen. The view model is responsible for managing the state
 * of the screen and for interacting with the remaining parts of the application.
 *
 * The screen supports setting the secrets required to access the game search web API.
 *
 * @param credentialsRepository The repository used to store the secrets.
 */
class SettingsViewModel(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _currentState = MutableStateFlow<SettingsScreenState>(SettingsScreenState.Loading)
    val state: StateFlow<SettingsScreenState>
        get() = _currentState.asStateFlow()

    init {
        viewModelScope.launch {
            val original = credentialsRepository.secrets.first()
            _currentState.value = Original(
                clientId = original?.clientId ?: "",
                clientSecret = original?.clientSecret ?: ""
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

        _currentState.value = Modified(
            clientId = clientId,
            clientSecret = state.clientSecret
        )
    }

    fun onClientSecretChange(clientSecret: String) {
        val state = _currentState.value
        if (state !is Original && state !is Modified)
            return

        _currentState.value = Modified(
            clientId = state.clientId,
            clientSecret = clientSecret
        )
    }

    fun onSave() {
        val state = _currentState.value
        if (state.isSaveEnabled) {
            viewModelScope.launch {
                _currentState.value = Saving(
                    clientId = state.clientId,
                    clientSecret = state.clientSecret
                )
                val result = credentialsRepository.saveSecrets(
                    Secrets(
                        clientId = state.clientId,
                        clientSecret = state.clientSecret
                    )
                )
                _currentState.value = result.fold(
                    onSuccess = {
                        Original(
                            clientId = state.clientId,
                            clientSecret = state.clientSecret
                        )
                    },
                    onFailure = { error ->
                        Error(
                            error = UnrecoverablePersistenceException(cause = error),
                            clientId = state.clientId,
                            clientSecret = state.clientSecret
                        )
                    }
                )
            }
        }
    }

    companion object {
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
