package pt.isel.lei.pdm.nasaapod.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import pt.isel.lei.pdm.nasaapod.ui.ViewModelBase


sealed interface MainScreenViewState {
    object Loading : MainScreenViewState
    object Idle : MainScreenViewState
    data class Error(val error: Exception) : MainScreenViewState
    data class Content(val image: ApodImage) : MainScreenViewState
}

class MainViewModel2(
    val nasaApodService: NasaApodService,
    val sessionState: SavedStateHandle
) : ViewModelBase() {

    companion object {
        const val IMAGE_TODAY_TEMP = "todays_image"
    }

    var screenState: MainScreenViewState by mutableStateOf(MainScreenViewState.Idle)
        private set

    init {

        refresh()
    }

    fun refresh() {
        screenState = MainScreenViewState.Loading

        viewModelScope.launch {
            try {
                var img = sessionState.get<ApodImage>(IMAGE_TODAY_TEMP)

                if (img == null)
                    img = nasaApodService.getTodaysImage()
                else
                    Log.d("MainViewModel2", "Used cached image")

                sessionState[IMAGE_TODAY_TEMP] = img

                screenState = MainScreenViewState.Content(img)
            } catch (e: Exception) {
                screenState = MainScreenViewState.Error(e)
            }
        }
    }
}