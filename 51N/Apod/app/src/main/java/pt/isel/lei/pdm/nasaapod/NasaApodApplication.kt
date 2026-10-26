package pt.isel.lei.pdm.nasaapod

import android.app.Application
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import pt.isel.lei.pdm.nasaapod.domain.database.AppDatabase
import pt.isel.lei.pdm.nasaapod.service.BookmarksRepository
import pt.isel.lei.pdm.nasaapod.service.DataStoreFavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.FavoriteRepository
import pt.isel.lei.pdm.nasaapod.service.NasaApodService
import pt.isel.lei.pdm.nasaapod.service.RemoteNasaApodService
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
            .fallbackToDestructiveMigration(true)
            .build()
    }



    val httpClient by lazy {
        HttpClient {
            install(plugin = ContentNegotiation) { // handles serialization/deserialization
                json(
                    json = Json {
                        prettyPrint = true //written json is idented
                        isLenient = true // more easy on the "correctness" of the files
                        ignoreUnknownKeys = true //ignore fields not in your data class
                    }
                )
            }
            install(Logging) {
                level = LogLevel.ALL
                //logger = io.ktor.client.plugins.logging.Logger.ANDROID
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorClient", message)
                    }

                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("NasaApodApplication", "onCreate()")
    }

    override val nasaService: NasaApodService by lazy {
        RemoteNasaApodService(httpClient)
    }
    override val favoriteRepository: FavoriteRepository by lazy {
        DataStoreFavoriteRepository(dataStore)
    }
    override val bookmarksRepository: BookmarksRepository by lazy {
        RoomBookmarksRepository(db.bookmarkDao())
    }
}
