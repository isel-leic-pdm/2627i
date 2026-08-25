package isel.dei.pdm.mygamevault

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.adapters.DataStoreSecretsRepository
import isel.dei.pdm.mygamevault.adapters.IgdbSearchService
import isel.dei.pdm.mygamevault.adapters.InMemoryCollectionRepository
import kotlinx.serialization.json.Json

private const val PREFERENCES_DATA_STORE_NAME = "preferences"

class MyGameVaultApplication : Application(), DependenciesContainer {

    private val dataStore by preferencesDataStore(name = PREFERENCES_DATA_STORE_NAME)

    override val searchService: SearchService by lazy {
        IgdbSearchService(
            httpClient = createIgdbHttpClient(),
            clientId = BuildConfig.IGDB_CLIENT_ID,
            accessToken = BuildConfig.IGDB_ACCESS_TOKEN
        )
    }

    override val secretsRepository: SecretsRepository by lazy {
        DataStoreSecretsRepository(dataStore)
    }

    override val collectionRepository: CollectionRepository by lazy {
        InMemoryCollectionRepository()
    }

    companion object {
        const val APP_TAG = "MyGameVault"

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
