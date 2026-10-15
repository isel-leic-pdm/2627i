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


class DetailsViewModel(
    val image: ApodImage,
    val favoriteRepository: FavoriteRepository
) : ViewModel() {
    val isFavorite: StateFlow<Boolean> =
        favoriteRepository.favorite
            .map { date ->
                date == image.date
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                false
            )

    fun toggleFavorite() {
        viewModelScope.launch {
            if (isFavorite.value)
                favoriteRepository.clearFavorite()
            else
                favoriteRepository.setFavorite(image.date)
        }
    }
}
