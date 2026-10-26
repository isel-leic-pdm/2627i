package isel.dei.pdm.demos.gamevault.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _currentState: MutableStateFlow<SettingsScreenState>
    val state: StateFlow<SettingsScreenState>
        get() = _currentState.asStateFlow()

    init {
        _currentState = MutableStateFlow(SettingsScreenState.Loading)
        viewModelScope.launch {
            TODO("Not yet implemented")
        }
    }

    // TODO: Implement

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