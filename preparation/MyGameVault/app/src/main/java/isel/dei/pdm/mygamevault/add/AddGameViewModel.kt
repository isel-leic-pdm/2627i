package isel.dei.pdm.mygamevault.add

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.toNonBlankStringOrNull
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SearchServiceException
import isel.dei.pdm.mygamevault.ports.UnexpectedServiceException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.milliseconds

/**
 * ViewModel for the Add Game screen, managing its state machine.
 */
@OptIn(FlowPreview::class)
class AddGameViewModel(
    private val searchService: SearchService,
    private val collectionRepository: CollectionRepository,
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
            combine(_query, _selectedPlatform, _selectedCategory) {
                query, platform, category -> Triple(query, platform, category)
            }
                .debounce(SEARCH_DEBOUNCE_MS.milliseconds)
                .collectLatest { (query, platform, category) ->
                    val nonBlankQuery = query.toNonBlankStringOrNull()
                    if (nonBlankQuery == null) {
                        _state.value = AddGameScreenState.Idle(
                            sourceQuery = null,
                            results = emptyList(),
                            selectedPlatform = platform,
                            selectedCategory = category
                        )
                    } else {
                        performSearch(nonBlankQuery, platform, category)
                    }
                }
        }
    }

    /**
     * Updates the search query and transitions to the Typing state.
     */
    fun onQueryChange(newQuery: String) {
        Log.d(TAG, "onQueryChange: newQuery = \"$newQuery\"")
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
        Log.d(TAG, "onPlatformChange: newPlatform = $newPlatform")
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
        Log.d(TAG, "onCategoryChange: newCategory = $newCategory")
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

    private suspend fun performSearch(
        partialName: NonBlankString,
        platform: Platform,
        category: Game.Category?,
    ) {
        Log.d(
            TAG,
            "performSearch: partialName = \"$partialName\", platform = $platform, category = $category"
        )
        _state.value = AddGameScreenState.Searching(_state.value.results, platform, category)
        searchService
            .search(partialName, platform, category)
            .fold(
                onSuccess = { newResults ->
                    _state.value = AddGameScreenState.Idle(
                        sourceQuery = partialName.value,
                        results = newResults,
                        selectedPlatform = platform,
                        selectedCategory = category
                    )
                },
                onFailure = { error ->
                    _state.value = AddGameScreenState.Error(
                        error = error as SearchServiceException,
                        previousResults = _state.value.results,
                        selectedPlatform = platform,
                        selectedCategory = category
                    )
                }
            )
    }

    /**
     * Adds a game to the collection on the specified platform.
     * @param game The game to add.
     * @param platform The platform to add the game on.
     */
    fun addGame(game: Game, platform: Platform) {
        Log.d(TAG, "addGame: game = ${game.id}, platform = ${platform.id}")
        viewModelScope.launch {
            try {
                collectionRepository.save(CollectionEntry(game, platform))
                Log.d(TAG, "addGame: successfully saved game ${game.id}")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "addGame: error saving game", e)
                _state.value = AddGameScreenState.Error(
                    error = UnexpectedServiceException("Could not add game to collection", e),
                    previousResults = _state.value.results,
                    selectedPlatform = _state.value.selectedPlatform,
                    selectedCategory = _state.value.selectedCategory
                )
            }
        }
    }

    companion object {
        val TAG = MyGameVaultApplication.buildTag("AddGameViewModel")
        const val SEARCH_DEBOUNCE_MS = 2000L

        fun factory(searchService: SearchService, collectionRepository: CollectionRepository) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AddGameViewModel(searchService, collectionRepository) as T
                }
            }
    }
}
