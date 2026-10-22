package pt.isel.lei.pdm.nasaapod.ui.bookmarks

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.BookmarksRepository

sealed interface BookmarksScreenState {
    object Idle : BookmarksScreenState
    class Content(val images: StateFlow<List<ApodImage>>) : BookmarksScreenState
}

class BookmarksViewModel(
    val bookmarksRepository: BookmarksRepository
) : ViewModel() {


    val viewState: StateFlow<BookmarksScreenState>
        field = MutableStateFlow<BookmarksScreenState>(BookmarksScreenState.Idle)

    init {
        refresh()

    }

    private fun refresh() {
        viewModelScope.launch {
            val stateFlow: StateFlow<List<ApodImage>> = bookmarksRepository.bookmarks
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    emptyList()
                )

            viewState.emit(BookmarksScreenState.Content(stateFlow))
        }
    }

    fun removeBookmark(image: ApodImage) {
        viewModelScope.launch {
            bookmarksRepository.remove(image)
        }
    }


}