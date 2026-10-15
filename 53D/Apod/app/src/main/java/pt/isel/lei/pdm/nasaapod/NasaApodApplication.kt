package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.services.DataStoreFavoriteRepository
import pt.isel.lei.pdm.nasaapod.services.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.services.NasaApodService

//Service Locator
interface DependencyContainer {
    val nasaApodService: NasaApodService
    val favoriteRepository: FavoriteRepository
}

class NasaApodApplication : Application(), DependencyContainer {

    //DO NOT DO
    var stateThatINeverShouldUse by mutableStateOf(0)


    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    override fun onCreate() {
        Log.d("NasaApodApplication", "onCreate")
        super.onCreate()

    }

    override val nasaApodService: NasaApodService by lazy {
        MockNasaApodService()
    }
    override val favoriteRepository: FavoriteRepository by lazy {
        DataStoreFavoriteRepository(dataStore)
    }
}