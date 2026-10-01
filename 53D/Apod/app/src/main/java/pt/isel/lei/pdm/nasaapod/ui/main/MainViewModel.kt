package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.mock.MockApodImages
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

class MainViewModel(
    val service: NasaApodService
) : ViewModel() {
    val todayImage: ApodImage
        get() = image

    private var image: ApodImage by mutableStateOf(???)


    fun refresh() {
        viewModelScope.launch {
            image = service.getTodaysImage()
        }
    }
}


class MainViewModelFactory(
    private val service: NasaApodService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(service) as T
    }


}