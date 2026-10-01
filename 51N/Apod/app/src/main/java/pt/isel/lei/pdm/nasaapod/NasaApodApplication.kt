package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.service.NasaApodService

interface DependencyContainer {
    val nasaService: NasaApodService
}

class NasaApodApplication : Application(), DependencyContainer {
    override fun onCreate() {
        super.onCreate()
        Log.d("NasaApodApplication", "onCreate()")
    }

    override val nasaService: NasaApodService by lazy {
        MockNasaApodService()
    }
}