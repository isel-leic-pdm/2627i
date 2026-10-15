package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.service.DataStoreFavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.NasaApodService

interface DependencyContainer {
    val nasaService: NasaApodService
    val favoriteRepository: FavoriteRepository
}

class NasaApodApplication : Application(), DependencyContainer {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate() {
        super.onCreate()
        Log.d("NasaApodApplication", "onCreate()")
    }

    override val nasaService: NasaApodService by lazy {
        MockNasaApodService()
    }
    override val favoriteRepository: FavoriteRepository by lazy {
        DataStoreFavoriteRepository(dataStore)
    }
}
