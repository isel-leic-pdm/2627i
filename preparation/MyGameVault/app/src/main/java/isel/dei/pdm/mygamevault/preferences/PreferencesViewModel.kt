package isel.dei.pdm.mygamevault.preferences

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.core.PersistenceException
import isel.dei.pdm.mygamevault.core.Secrets
import isel.dei.pdm.mygamevault.core.SecretsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Preferences screen.
 */
class PreferencesViewModel(
    private val secretsRepository: SecretsRepository
) : ViewModel() {

    private var originalSecrets = Secrets("", "")

    private val _state = MutableStateFlow<PreferencesScreenState>(
        PreferencesScreenState.Loading
    )
    val state: StateFlow<PreferencesScreenState> = _state.asStateFlow()

    companion object {
        private val TAG = MyGameVaultApplication.buildTag("PreferencesViewModel")

        /**
         * The timeout for the error state.
         */
        val ERROR_TIMEOUT = 3000L.milliseconds

        fun factory(secretsRepository: SecretsRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PreferencesViewModel(secretsRepository) as T
            }
        }
    }

    init {
        viewModelScope.launch {
            secretsRepository.secrets.collectLatest { secrets ->
                Log.d(TAG, "secrets.collectLatest: secrets flow emitted new value")
                val newSecrets = secrets ?: Secrets("", "")
                val currentState = _state.value
                
                val wasOriginalOrLoading = currentState is PreferencesScreenState.Loading || 
                                           currentState is PreferencesScreenState.Original
                
                originalSecrets = newSecrets
                
                if (wasOriginalOrLoading) {
                    _state.value = PreferencesScreenState.Original(newSecrets.clientId, newSecrets.clientSecret)
                } else {
                    updateState(currentState.clientId, currentState.clientSecret)
                }
            }
        }
    }

    /**
     * Updates the Client ID in the current state and checks if it was modified.
     */
    fun onClientIdChange(newId: String) {
        Log.d(TAG, "onClientIdChange: newId = \"$newId\"")
        if (_state.value.isInputEnabled) {
            updateState(newId, _state.value.clientSecret)
        }
    }

    /**
     * Updates the Client Secret in the current state and checks if it was modified.
     */
    fun onClientSecretChange(newSecret: String) {
        Log.d(TAG, "onClientSecretChange: newSecret length = ${newSecret.length}")
        if (_state.value.isInputEnabled) {
            updateState(_state.value.clientId, newSecret)
        }
    }

    /**
     * Triggers the saving of the preferences and updates the original values.
     */
    fun onSave() {
        val current = _state.value
        if (current.isSaveEnabled) {
            Log.d(TAG, "onSave: started")
            viewModelScope.launch {
                _state.value = PreferencesScreenState.Saving(current.clientId, current.clientSecret)
                secretsRepository
                    .saveSecrets(Secrets(current.clientId, current.clientSecret))
                    .onFailure { error ->
                        Log.e(TAG, "onSave: failed", error)
                        _state.value = PreferencesScreenState.Error(
                            error = error as PersistenceException,
                            clientId = current.clientId,
                            clientSecret = current.clientSecret
                        )
                        delay(ERROR_TIMEOUT)
                        Log.d(TAG, "onSave: error state timeout reached, reverting to original secrets")
                        _state.value = PreferencesScreenState.Original(
                            originalSecrets.clientId,
                            originalSecrets.clientSecret
                        )
                    }
                    .onSuccess {
                        Log.d(TAG, "onSave: completed successfully")
                    }
            }
        }
    }

    private fun updateState(clientId: String, clientSecret: String) {
        _state.value = if (clientId == originalSecrets.clientId && clientSecret == originalSecrets.clientSecret) {
            PreferencesScreenState.Original(clientId, clientSecret)
        } else {
            PreferencesScreenState.Modified(clientId, clientSecret)
        }
    }
}
