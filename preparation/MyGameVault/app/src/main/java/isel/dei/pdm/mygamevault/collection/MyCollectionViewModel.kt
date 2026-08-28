package isel.dei.pdm.mygamevault.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the My Collection screen.
 * It manages the screen's state machine and provides access to the collection data.
 */
class MyCollectionViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MyCollectionScreenState>(MyCollectionScreenState.Idle())

    /**
     * The current state of the screen.
     */
    val state: StateFlow<MyCollectionScreenState> = _state.asStateFlow()

    private var fetchJob: Job? = null

    init {
        fetchData()
    }

    /**
     * Fetches the latest data from the collection.
     * Transitions from Idle to Loading, and back to Idle upon completion or update.
     */
    fun fetchData() {
        Log.d(TAG, "fetchData: started")
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _state.value = MyCollectionScreenState.Loading(_state.value.entries)
            repository.getLatest()
                .catch { error ->
                    Log.e(TAG, "fetchData: error occurred", error)
                    _state.value = MyCollectionScreenState.Idle(_state.value.entries, error)
                }
                .collect { entries ->
                    Log.d(TAG, "fetchData: successfully collected ${entries.size} entries")
                    _state.value = MyCollectionScreenState.Idle(entries)
                }
        }
    }

    companion object {
        val TAG = MyGameVaultApplication.buildTag("MyCollectionViewModel")

        fun factory(repository: CollectionRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyCollectionViewModel(repository) as T
            }
        }
    }
}
