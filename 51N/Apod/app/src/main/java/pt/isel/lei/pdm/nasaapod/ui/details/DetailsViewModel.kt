package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.ui.ViewModelBase

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
}