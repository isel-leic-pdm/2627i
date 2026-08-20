package isel.dei.pdm.mygamevault

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.infrastructure.IgdbSearchService
import kotlinx.serialization.json.Json

class MyGameVaultApplication : Application(), DependenciesContainer {

    override val searchService: SearchService by lazy {
        IgdbSearchService(
            httpClient = createIgdbHttpClient(),
            clientId = BuildConfig.IGDB_CLIENT_ID,
            accessToken = BuildConfig.IGDB_ACCESS_TOKEN
        )
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
