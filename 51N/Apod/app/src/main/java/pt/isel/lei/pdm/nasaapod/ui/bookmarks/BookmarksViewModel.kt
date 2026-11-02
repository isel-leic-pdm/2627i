package pt.isel.lei.pdm.nasaapod.ui.bookmarks

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository
import pt.isel.lei.pdm.nasaapod.service.ImageDownloadService
import pt.isel.lei.pdm.nasaapod.ui.ViewModelBase

sealed interface BookmarksScreenState {
    object Idle : BookmarksScreenState
    data class Content(val images: StateFlow<List<ApodImage>>) : BookmarksScreenState
}

class BookmarksViewModel(
    private val bookmarksRepository: BookmarksRepository,
    private val downloader: ImageDownloadService
) : ViewModelBase() {

    val viewState: StateFlow<BookmarksScreenState>
        field = MutableStateFlow<BookmarksScreenState>(BookmarksScreenState.Idle)

    init {
        refresh()
    }

    fun downloadBookmarks(all: Boolean = false) {
        viewModelScope.launch {

            if(all)
            {
                downloader.start(*bookmarksRepository.bookmarks.first().toTypedArray())
            }
            else
                downloader.start()

        }
    }

    fun refresh() {
        viewModelScope.launch {
            val stateFlow = bookmarksRepository.bookmarks.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                emptyList()
            )
            viewState.emit(BookmarksScreenState.Content(stateFlow))
        }
    }

    fun deleteBookmark(it: ApodImage) {
        viewModelScope.launch {
            bookmarksRepository.remove(it)
        }
    }
}