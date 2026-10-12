package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import pt.isel.lei.pdm.nasaapod.ui.ViewModelBase

class MainViewModel(
    val nasaApodService: NasaApodService
) : ViewModelBase() {
    var todayImage: ApodImage? by mutableStateOf(null)
        private set



    init {
        refresh()
    }

    fun refresh() = safeCall{
        todayImage = nasaApodService.getTodaysImage()
    }


}

class MainViewModelFactory(
    val nasaApodService: NasaApodService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(nasaApodService) as T
    }
}