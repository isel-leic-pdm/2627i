package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

class MainViewModel(
    val service: NasaApodService
) : ViewModel() {
    var image: ApodImage? by mutableStateOf(null)
        private set;

    var loading: Boolean by mutableStateOf(false)
        private set

    var error: Exception? by mutableStateOf(null)
        private set

    init {
        refresh()
    }


    fun refresh() = safeCall {
        image = service.getTodaysImage()
    }

    private fun safeCall(
        func: suspend () -> Unit
    ) {
        loading = true
        error = null
        viewModelScope.launch {
            try {
                func()
            } catch (e: Exception) {
                error = e
            } finally {
                loading = false
            }
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