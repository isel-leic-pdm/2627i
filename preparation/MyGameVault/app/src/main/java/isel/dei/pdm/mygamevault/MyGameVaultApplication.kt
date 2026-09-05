package isel.dei.pdm.mygamevault

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.Secrets
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.adapters.DataStoreSecretsRepository
import isel.dei.pdm.mygamevault.adapters.IgdbSearchService
import isel.dei.pdm.mygamevault.adapters.RoomCollectionRepository
import isel.dei.pdm.mygamevault.adapters.createFileCachedImageLoader
import isel.dei.pdm.mygamevault.adapters.createIgdbImageLoader
import isel.dei.pdm.mygamevault.adapters.db.GameDatabase
import isel.dei.pdm.mygamevault.infrastructure.SessionNotificationManager
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val PREFERENCES_DATA_STORE_NAME = "preferences"
private const val DATABASE_NAME = "game-vault-db"

class MyGameVaultApplication : Application(), DependenciesContainer {

    private val dataStore by preferencesDataStore(name = PREFERENCES_DATA_STORE_NAME)
    private val applicationScope = MainScope()

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            GameDatabase::class.java,
            DATABASE_NAME
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    override val searchService: SearchService by lazy {
        IgdbSearchService(
            httpClient = createIgdbHttpClient(),
            secretsRepository = secretsRepository
        )
    }

    override val secretsRepository: SecretsRepository by lazy {
        val buildTimeSecrets = if (BuildConfig.IGDB_CLIENT_ID.isNotBlank() && BuildConfig.IGDB_ACCESS_TOKEN.isNotBlank()) {
            Secrets(BuildConfig.IGDB_CLIENT_ID, BuildConfig.IGDB_ACCESS_TOKEN)
        } else {
            null
        }
        DataStoreSecretsRepository(dataStore, buildTimeSecrets)
    }

    override val collectionRepository: CollectionRepository by lazy {
        RoomCollectionRepository(database)
    }

    private val sessionNotificationManager by lazy {
        SessionNotificationManager(this)
    }

    override fun onCreate() {
        super.onCreate()
        sessionNotificationManager.createNotificationChannel()
        applicationScope.launch {
            collectionRepository.getActiveSession().collect { entry ->
                Log.d(APP_TAG, "Active session collected: game=${entry?.game?.name()}, startTime=${entry?.sessionStartTime}")
                if (entry != null) {
                    sessionNotificationManager.showSessionNotification(entry)
                } else {
                    sessionNotificationManager.cancelSessionNotification()
                }
            }
        }
    }

    override val imageLoader: ImageLoader by lazy {
        createFileCachedImageLoader(
            context = applicationContext,
            decorated = createIgdbImageLoader(createIgdbHttpClient(), secretsRepository),
            maxEntries = 100
        )
    }

    companion object {
        const val APP_TAG = "MyGameVaultApp"

        /**
         * Builds a log tag for the given component name following the project convention.
         * @param componentName The name of the component.
         * @return The log tag.
         */
        fun buildTag(componentName: String) = "$APP_TAG.$componentName"
    }
}

/**
 * Creates and configures an [HttpClient] for IGDB.
 */
private fun createIgdbHttpClient(): HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            isLenient = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        })
    }
    install(Logging) {
        level = LogLevel.INFO
    }
}
