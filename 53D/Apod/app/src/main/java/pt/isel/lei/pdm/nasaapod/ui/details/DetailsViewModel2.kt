package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.BookmarksRepository
import pt.isel.lei.pdm.nasaapod.services.FavoriteRepository

sealed interface DetailScreenViewState {
    val image: ApodImage
    val isBookmarked: Boolean

    class Favorite(override val image: ApodImage, override val isBookmarked: Boolean) :
        DetailScreenViewState

    class NotFavorite(override val image: ApodImage, override val isBookmarked: Boolean) :
        DetailScreenViewState
}

class DetailsViewModel2(
    val image: ApodImage,
    val favoriteRepository: FavoriteRepository,
    val bookmarksRepository: BookmarksRepository
) : ViewModel() {

    val viewState: StateFlow<DetailScreenViewState> =
        combine(
            bookmarksRepository.isBookmarked(image),
            favoriteRepository.favorite
        ) { isBookmarked, favoriteDate ->
            if (favoriteDate == image.date)
                DetailScreenViewState.Favorite(image, isBookmarked)
            else
                DetailScreenViewState.NotFavorite(image, isBookmarked)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            DetailScreenViewState.NotFavorite(image, false)
        )

    /*
        favoriteRepository.favorite.map
        {
            if (it == image.date)
                DetailScreenViewState.Favorite(image, false)
            else
                DetailScreenViewState.NotFavorite(image, false)
        }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        DetailScreenViewState.NotFavorite(image, false)
        )
    */
    fun toggleFavorite() {
        viewModelScope.launch {
            if (viewState.value is DetailScreenViewState.Favorite)
                favoriteRepository.clearFavorite()
            else
                favoriteRepository.setFavorite(image.date)
        }
    }

    fun toggleBookmark() {
        viewModelScope.launch {
            if (viewState.value.isBookmarked)
                bookmarksRepository.remove(image)
            else
                bookmarksRepository.add(image)
        }

    }

}