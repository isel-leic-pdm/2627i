package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

//Service Locator
interface DependencyContainer {
    val nasaApodService: NasaApodService
}

class NasaApodApplication : Application(), DependencyContainer {

    //DO NOT DO
    var stateThatINeverShouldUse by mutableStateOf(0)

    override fun onCreate() {
        Log.d("NasaApodApplication", "onCreate")
        super.onCreate()

    }

    override val nasaApodService: NasaApodService by lazy {
        MockNasaApodService()
    }
}