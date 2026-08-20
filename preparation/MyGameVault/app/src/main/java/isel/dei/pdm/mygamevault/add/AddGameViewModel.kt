package isel.dei.pdm.mygamevault.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.core.SearchServiceException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Add Game screen, managing its state machine.
 */
@OptIn(FlowPreview::class)
class AddGameViewModel(
    private val searchService: SearchService,
) : ViewModel() {

    /**
     * Represents the sequence of search query values entered by the user (note that only the
     * latest value is kept)
     */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /**
     * Represents the sequence of states the screen is in (note that only the latest value is kept)
     */
    private val _state = MutableStateFlow<AddGameScreenState>(AddGameScreenState.Idle())
    val state: StateFlow<AddGameScreenState> = _state.asStateFlow()

    init {
        // Observe query changes with a debounce timeout
        viewModelScope.launch {
            _query
                .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
                .collectLatest { q ->
                    if (q.isBlank()) {
                        _state.value = AddGameScreenState.Idle(sourceQuery = null, results = emptyList())
                    } else {
                        performSearch(q)
                    }
                }
        }
    }

    /**
     * Updates the search query and transitions to the Typing state.
     */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _state.value = AddGameScreenState.Typing(_state.value.results)
    }

    private suspend fun performSearch(q: String) {
        _state.value = AddGameScreenState.Searching(_state.value.results)
        searchService
            .search(q)
            .fold(
                onSuccess = { newResults -> 
                    _state.value = AddGameScreenState.Idle(sourceQuery = q, results = newResults) 
                },
                onFailure = { error ->
                    _state.value = AddGameScreenState.Error(
                        error = error as SearchServiceException,
                        previousResults = _state.value.results
                    )
                }
            )
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 2000L

        fun factory(searchService: SearchService) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddGameViewModel(searchService) as T
            }
        }
    }
}
