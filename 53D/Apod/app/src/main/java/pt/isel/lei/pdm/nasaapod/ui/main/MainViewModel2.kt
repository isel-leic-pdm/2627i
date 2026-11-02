package pt.isel.lei.pdm.nasaapod.ui.main

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.services.NasaApodService


sealed interface MainScreenViewState {
    object Loading : MainScreenViewState
    object Idle : MainScreenViewState
    class Image(val img: ApodImage) : MainScreenViewState
    class Error(val error: Exception) : MainScreenViewState

}

class MainViewModel2(
    val service: NasaApodService,
    val session: SavedStateHandle
) : ViewModel() {

    companion object {
        const val SESSION_KEY = "session"
    }

    var screenState: MainScreenViewState by mutableStateOf(MainScreenViewState.Idle)
        private set

    init {

        refresh()
    }

    private fun safeCall(
        func: suspend () -> MainScreenViewState
    ) {
        screenState = MainScreenViewState.Loading
        viewModelScope.launch {
            try {
                screenState = func()
            } catch (e: Exception) {
                screenState = MainScreenViewState.Error(e)
            }
        }
    }

    fun refresh() = safeCall {

        var img = session.get<ApodImage>(SESSION_KEY)

        if (img == null)
            img = service.getTodaysImage()
        else
            Log.d("MainViewModel", "Got from the session handle")

        session[SESSION_KEY] = img

        return@safeCall MainScreenViewState.Image(img)
    }
}