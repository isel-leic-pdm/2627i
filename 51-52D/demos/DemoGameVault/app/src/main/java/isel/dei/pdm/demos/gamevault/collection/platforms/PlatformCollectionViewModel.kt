package isel.dei.pdm.demos.gamevault.collection.platforms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenState.Error
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenState.Loading
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenState.Results
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.PlatformCollectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The view model for the platform collection screen. The view model is responsible for managing
 * the state of the screen and for interacting with the remaining parts of the application.
 *
 * The state is computed from the observable list of platforms in the collection, so the screen
 * stays up to date with changes made elsewhere (e.g. platforms added in the search screen).
 * See docs/ux/platform-collection-screen-state-machine.png for the allowed transitions.
 *
 * @param collectionRepo The repository where the user's collection of platforms is stored.
 */
class PlatformCollectionViewModel(
    private val collectionRepo: PlatformCollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<PlatformCollectionScreenState>(Loading)

    /**
     * The state of the screen.
     */
    val state: StateFlow<PlatformCollectionScreenState> = _state.asStateFlow()

    /**
     * The ongoing observation of the collection, if any. It ends when an error occurs.
     */
    private var observationJob: Job? = null

    init {
        startObserving()
    }

    /**
     * Implementation of the state transition that results from the user retrying after an error.
     * The screen moves to [Loading] and the collection is observed again.
     * If the current state is not [Error], no action is taken.
     */
    fun onRetry() {
        if (_state.value is Error)
            startObserving()
    }

    /**
     * Starts observing the collection, cancelling the ongoing observation, if any. The screen
     * moves to [Loading] and then to [Results], each time the collection is read, or to [Error],
     * if it cannot be read (which ends the observation).
     */
    private fun startObserving() {
        observationJob?.cancel()
        _state.value = Loading

        observationJob = viewModelScope.launch {
            try {
                collectionRepo.platforms.collect { _state.value = Results(it) }
            } catch (e: PersistenceException) {
                _state.value = Error(e)
            }
        }
    }

    companion object {
        /**
         * Factory for creating instances of [PlatformCollectionViewModel] with the given dependencies.
         */
        fun factory(repository: PlatformCollectionRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { PlatformCollectionViewModel(repository) }
            }
    }
}
