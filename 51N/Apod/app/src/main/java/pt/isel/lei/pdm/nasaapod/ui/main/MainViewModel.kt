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
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class MainViewModel(
    val nasaApodService: NasaApodService
) : ViewModel() {

    val todayImage: ApodImage
        get() = image
    private var image: ApodImage by mutableStateOf(???)

    fun refresh(){
        viewModelScope.launch {
            image = nasaApodService.getTodaysImage()
        }
    }
}

class MainViewModelFactory(
    val nasaApodService: NasaApodService
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(nasaApodService) as T
    }
}