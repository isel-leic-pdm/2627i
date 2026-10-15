package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.FavoriteRepository

sealed interface DetailScreenViewState {
    val image: ApodImage

    class Favorite(override val image: ApodImage) : DetailScreenViewState
    class NotFavorite(override val image: ApodImage) : DetailScreenViewState
}

class DetailsViewModel2(
    val image: ApodImage,
    val favoriteRepository: FavoriteRepository
) : ViewModel() {

    val viewState: StateFlow<DetailScreenViewState> =
        favoriteRepository.favorite.map {
            if (it == image.date)
                DetailScreenViewState.Favorite(image)
            else
                DetailScreenViewState.NotFavorite(image)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            DetailScreenViewState.NotFavorite(image)
        )

    fun toggleFavorite() {
        viewModelScope.launch {
            if (viewState.value is DetailScreenViewState.Favorite)
                favoriteRepository.clearFavorite()
            else
                favoriteRepository.setFavorite(image.date)
        }
    }

}