package isel.dei.pdm.mygamevault.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SearchServiceException
import isel.dei.pdm.mygamevault.domain.toNonBlankStringOrNull
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _selectedPlatform = MutableStateFlow(Platforms.PS5)
    val selectedPlatform: StateFlow<Platform> = _selectedPlatform.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Game.Category?>(null)
    val selectedCategory: StateFlow<Game.Category?> = _selectedCategory.asStateFlow()

    private val _state = MutableStateFlow<AddGameScreenState>(AddGameScreenState.Idle())
    val state: StateFlow<AddGameScreenState> = _state.asStateFlow()

    init {
        // Observe changes with a debounce timeout
        viewModelScope.launch {
            combine(_query, _selectedPlatform, _selectedCategory) { q, p, c ->
                Triple(q, p, c)
            }
                .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
                .collectLatest { (q, p, c) ->
                    val nonBlankQuery = q.toNonBlankStringOrNull()
                    if (nonBlankQuery == null) {
                        _state.value = AddGameScreenState.Idle(null, emptyList(), p, c)
                    } else {
                        performSearch(nonBlankQuery, p, c)
                    }
                }
        }
    }

    /**
     * Updates the search query and transitions to the Typing state.
     */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _state.value = AddGameScreenState.Typing(
            results = _state.value.results,
            selectedPlatform = _selectedPlatform.value,
            selectedCategory = _selectedCategory.value
        )
    }

    /**
     * Updates the selected platform and transitions to the Searching state if query is not blank.
     */
    fun onPlatformChange(newPlatform: Platform) {
        _selectedPlatform.value = newPlatform
        if (_query.value.isNotBlank()) {
            _state.value = AddGameScreenState.Searching(
                results = _state.value.results,
                selectedPlatform = newPlatform,
                selectedCategory = _selectedCategory.value
            )
        } else {
            _state.value = AddGameScreenState.Idle(
                sourceQuery = null,
                results = emptyList(),
                selectedPlatform = newPlatform,
                selectedCategory = _selectedCategory.value
            )
        }
    }

    /**
     * Updates the selected category and transitions to the Searching state if query is not blank.
     */
    fun onCategoryChange(newCategory: Game.Category?) {
        _selectedCategory.value = newCategory
        if (_query.value.isNotBlank()) {
            _state.value = AddGameScreenState.Searching(
                results = _state.value.results,
                selectedPlatform = _selectedPlatform.value,
                selectedCategory = newCategory
            )
        } else {
            _state.value = AddGameScreenState.Idle(
                sourceQuery = null,
                results = emptyList(),
                selectedPlatform = _selectedPlatform.value,
                selectedCategory = newCategory
            )
        }
    }

    private suspend fun performSearch(q: NonBlankString, p: Platform, c: Game.Category?) {
        _state.value = AddGameScreenState.Searching(_state.value.results, p, c)
        searchService
            .search(q, p, c)
            .fold(
                onSuccess = { newResults ->
                    _state.value = AddGameScreenState.Idle(
                        sourceQuery = q.value,
                        results = newResults,
                        selectedPlatform = p,
                        selectedCategory = c
                    )
                },
                onFailure = { error ->
                    _state.value = AddGameScreenState.Error(
                        error = error as SearchServiceException,
                        previousResults = _state.value.results,
                        selectedPlatform = p,
                        selectedCategory = c
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
