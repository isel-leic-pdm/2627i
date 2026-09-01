package isel.dei.pdm.mygamevault.collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the My Collection screen.
 * It manages the screen's state machine and provides access to the collection data.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyCollectionViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(CollectionFilter.LATEST)

    /**
     * The current state of the screen, derived declaratively from the filter.
     */
    val state: StateFlow<MyCollectionScreenState> = _filter
        .flatMapLatest { filter ->
            val repositoryFlow = when (filter) {
                CollectionFilter.LATEST -> repository.getLatest()
                CollectionFilter.PLAYING -> repository.searchByStates(setOf(PlayStatus.State.PLAYING))
                CollectionFilter.FINISHED -> repository.searchByStates(
                    setOf(PlayStatus.State.FINISHED, PlayStatus.State.PLATINUM)
                )
                CollectionFilter.PLATINUM -> repository.searchByStates(setOf(PlayStatus.State.PLATINUM))
                CollectionFilter.BACKLOG -> repository.searchByStates(setOf(PlayStatus.State.BACKLOG))
            }

            repositoryFlow
                .map<List<CollectionEntry>, MyCollectionScreenState> { entries ->
                    Log.d(TAG, "fetchData: successfully collected ${entries.size} entries for filter $filter")
                    MyCollectionScreenState.Idle(entries, filter)
                }
                .onStart {
                    Log.d(TAG, "fetchData: started for filter $filter")
                    emit(MyCollectionScreenState.Loading(emptyList(), filter))
                }
                .catch { error ->
                    Log.e(TAG, "fetchData: error occurred", error)
                    emit(MyCollectionScreenState.Idle(emptyList(), filter, error))
                }
        }
        .scan(MyCollectionScreenState.Idle() as MyCollectionScreenState) { prevState, newState ->
            when (newState) {
                is MyCollectionScreenState.Loading -> MyCollectionScreenState.Loading(prevState.entries, newState.filter)
                is MyCollectionScreenState.Idle -> if (newState.error != null) {
                    MyCollectionScreenState.Idle(prevState.entries, newState.filter, newState.error)
                } else {
                    newState
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MyCollectionScreenState.Idle()
        )


    /**
     * Updates the current filter and triggers a new data fetch.
     */
    fun onFilterChange(newFilter: CollectionFilter) {
        Log.d(TAG, "onFilterChange: newFilter = $newFilter")
        _filter.value = newFilter
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
