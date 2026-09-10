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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the My Collection screen.
 * It manages the screen's state machine and provides access to the collection data.
 *
 * NOTE: This implementation uses an "Expanding Window" pagination strategy. While this
 * ensures reactivity and simplicity, it keeps all loaded items in memory. For very large
 * collections, this should be migrated to a more memory-efficient solution like Paging 3.
 * https://developer.android.com/topic/libraries/architecture/paging/v3-overview
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MyCollectionViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(CollectionFilter.LATEST)
    private val _requestedTop = MutableStateFlow(PAGE_SIZE)
    private var lastHandledNavigationId: String? = null

    /**
     * The current state of the screen, derived declaratively from the filter.
     */
    val state: StateFlow<MyCollectionScreenState> = combine(_filter, _requestedTop) { filter, top ->
        filter to top
    }
        .flatMapLatest { (filter, top) ->
            val repositoryFlow = when (filter) {
                CollectionFilter.LATEST -> repository.getLatest(skip = 0, top = top)
                CollectionFilter.PLAYING -> repository.searchByStates(states = setOf(PlayStatus.State.PLAYING), skip = 0, top = top)
                CollectionFilter.FINISHED -> repository.searchByStates(
                    states = setOf(PlayStatus.State.FINISHED, PlayStatus.State.PLATINUM),
                    skip = 0,
                    top = top
                )
                CollectionFilter.PLATINUM -> repository.searchByStates(states = setOf(PlayStatus.State.PLATINUM), skip = 0, top = top)
                CollectionFilter.BACKLOG -> repository.searchByStates(states = setOf(PlayStatus.State.BACKLOG), skip = 0, top = top)
            }

            repositoryFlow
                .map<List<CollectionEntry>, MyCollectionScreenState> { entries ->
                    Log.d(TAG, "fetchData: successfully collected ${entries.size} entries for filter $filter")
                    MyCollectionScreenState.Idle(
                        entries = entries,
                        filter = filter,
                        hasMore = entries.size >= top
                    )
                }
                .onStart {
                    Log.d(TAG, "fetchData: started for filter $filter")
                    emit(MyCollectionScreenState.Loading(filter = filter))
                }
                .catch { error ->
                    Log.e(TAG, "fetchData: error occurred", error)
                    emit(MyCollectionScreenState.Idle(filter = filter, error = error))
                }
        }
        .scan(MyCollectionScreenState.Idle() as MyCollectionScreenState) { prevState, newState ->
            when (newState) {
                is MyCollectionScreenState.Loading -> {
                    val isLoadingMore = _requestedTop.value > PAGE_SIZE && newState.filter == prevState.filter
                    MyCollectionScreenState.Loading(
                        entries = prevState.entries,
                        filter = newState.filter,
                        hasMore = prevState.hasMore,
                        isLoadingMore = isLoadingMore
                    )
                }
                is MyCollectionScreenState.Idle -> if (newState.error != null) {
                    MyCollectionScreenState.Idle(
                        entries = prevState.entries,
                        filter = newState.filter,
                        hasMore = prevState.hasMore,
                        error = newState.error
                    )
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
        _requestedTop.value = PAGE_SIZE
        _filter.value = newFilter
    }

    /**
     * Resets the filter to LATEST if the given [navigationId] has not been handled yet.
     */
    fun onFilterResetRequested(navigationId: String) {
        if (navigationId != lastHandledNavigationId) {
            Log.d(TAG, "onFilterResetRequested: resetting filter for id $navigationId")
            lastHandledNavigationId = navigationId
            _requestedTop.value = PAGE_SIZE
            onFilterChange(CollectionFilter.LATEST)
        }
    }

    /**
     * Requests the next page of results if more items are available.
     */
    fun onLoadNextPage() {
        val currentState = state.value
        if (currentState.hasMore && currentState !is MyCollectionScreenState.Loading) {
            Log.d(TAG, "onLoadNextPage: requesting more items")
            _requestedTop.value += PAGE_SIZE
        }
    }

    companion object {
        val TAG = MyGameVaultApplication.buildTag("MyCollectionViewModel")
        private const val PAGE_SIZE = 20

        fun factory(repository: CollectionRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MyCollectionViewModel(repository) as T
            }
        }
    }
}
