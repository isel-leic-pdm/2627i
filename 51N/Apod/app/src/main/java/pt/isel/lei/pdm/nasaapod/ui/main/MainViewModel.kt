package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.mock.MockApodImages

class MainViewModel : ViewModel() {
    val todayImage: ApodImage
        get() = image
    private var image: ApodImage by mutableStateOf(MockApodImages.Images[1])
}