package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.ui.ViewModelBase

//changed, original viewmodel commented below
sealed interface DetailScreenViewState {
    val image: ApodImage
    val isBookmarked: Boolean

    class Favorite(override val image: ApodImage, override val isBookmarked: Boolean) :
        DetailScreenViewState

    class NotFavorite(override val image: ApodImage, override val isBookmarked: Boolean) :
        DetailScreenViewState
}

class DetailsViewModel(
    val image: ApodImage,
    val favoriteRepository: FavoriteRepository,
    val bookmarksRepository: BookmarksRepository
) : ViewModelBase() {

    val viewState: StateFlow<DetailScreenViewState> =
        combine(
            favoriteRepository.favorite,
            bookmarksRepository.isBookmarked(image)
        ) { date, isBookmarked ->
            if (date == image.date)
                DetailScreenViewState.Favorite(image, isBookmarked)
            else
                DetailScreenViewState.NotFavorite(image, isBookmarked)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            DetailScreenViewState.NotFavorite(image, false)
        )

    fun toggleFavorite() {
        viewModelScope.launch {
            if (viewState.value is DetailScreenViewState.Favorite)
                favoriteRepository.clearFavorite()
            else
                favoriteRepository.setFavorite(image.date)
        }
    }

    fun toggleBookmark(){
        viewModelScope.launch {
            if (viewState.value.isBookmarked)
                bookmarksRepository.remove(image)
            else
                bookmarksRepository.add(image)
        }
    }

}
/*
class DetailsViewModel(
    val image: ApodImage,
    val repo: FavoriteRepository
) : ViewModelBase() {

    val isFavorite: StateFlow<Boolean> =
        repo.favorite.map { date ->
            image.date == date
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    fun toggleFavorite() {
        viewModelScope.launch {
            if (isFavorite.value)
                repo.clearFavorite()
            else
                repo.setFavorite(image.date)
        }
    }
}*/