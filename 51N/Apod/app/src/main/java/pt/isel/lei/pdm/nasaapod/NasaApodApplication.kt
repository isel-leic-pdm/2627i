package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import pt.isel.lei.pdm.nasaapod.domain.database.AppDatabase
import pt.isel.lei.pdm.nasaapod.mock.MockBookmarksRepository
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository
import pt.isel.lei.pdm.nasaapod.service.DataStoreFavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import pt.isel.lei.pdm.nasaapod.service.RoomBookmarksRepository

interface DependencyContainer {
    val nasaService: NasaApodService
    val favoriteRepository: FavoriteRepository
    val bookmarksRepository: BookmarksRepository
}

class NasaApodApplication : Application(), DependencyContainer {
    private val dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    val db by lazy {
        Room.databaseBuilder<AppDatabase>(applicationContext, "database-name")
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

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
    override val bookmarksRepository: BookmarksRepository by lazy {
        RoomBookmarksRepository(db.bookmarkDao())
    }
}
