package isel.dei.pdm.mygamevault.add.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import isel.dei.pdm.mygamevault.ports.SearchService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Game Details screen.
 */
class GameDetailsViewModel(
    private val gameId: Long,
    private val searchService: SearchService
) : ViewModel() {

    private val _state = MutableStateFlow<GameDetailsScreenState>(GameDetailsScreenState.Loading)
    val state = _state.asStateFlow()

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            _state.value = GameDetailsScreenState.Loading
            searchService.fetchGameDetails(gameId).fold(
                onSuccess = { details ->
                    if (details != null) {
                        _state.value = GameDetailsScreenState.Loaded(details)
                    } else {
                        _state.value = GameDetailsScreenState.Error(Exception("Game not found"))
                    }
                },
                onFailure = { error ->
                    _state.value = GameDetailsScreenState.Error(error)
                }
            )
        }
    }

    companion object {
        fun factory(gameId: Long, searchService: SearchService) = viewModelFactory {
            initializer {
                GameDetailsViewModel(gameId, searchService)
            }
        }
    }
}
