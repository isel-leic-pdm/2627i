package isel.dei.pdm.mygamevault.adapters

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.ports.NoConnectivityException
import isel.dei.pdm.mygamevault.ports.RateLimitExceededException
import isel.dei.pdm.mygamevault.ports.SearchService
import isel.dei.pdm.mygamevault.ports.ServiceUnavailableException
import isel.dei.pdm.mygamevault.ports.UnauthenticatedException
import isel.dei.pdm.mygamevault.ports.UnexpectedServiceException
import kotlinx.coroutines.CancellationException
import java.io.IOException

/**
 * Real implementation of [SearchService] using IGDB API v4 and Ktor.
 */
class IgdbSearchService(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val accessToken: String
) : SearchService {

    companion object {
        private val TAG = MyGameVaultApplication.buildTag("IgdbSearchService")
    }

    override suspend fun search(
        partialName: NonBlankString,
        platform: Platform,
        category: Game.Category?
    ): Result<List<Game>> = try {
        Log.d(TAG, "search: started with partialName = \"$partialName\", platform = $platform, category = $category")
        
        val whereClause = buildString {
            append("where platforms = (${platform.toIgdbId()})")
            if (category != null) {
                append(" & category = (${category.toIgdbId()})")
            }
            append(" & name ~ \"$partialName\"*")
        }

        val apicalypseQuery = """
            fields name, first_release_date, cover.url;
            $whereClause;
            sort name asc;
            limit 20;
        """.trimIndent()

        val response = httpClient.post("https://api.igdb.com/v4/games") {
            header("Client-ID", clientId)
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(apicalypseQuery)
        }

        Log.d(TAG, "search: API returned status ${response.status}")

        when (response.status) {
            HttpStatusCode.OK -> {
                val gamesDto: List<IgdbGame> = response.body()
                Result.success(gamesDto.map { it.toGame() })
            }
            HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> 
                Result.failure(UnauthenticatedException("Invalid credentials"))
            HttpStatusCode.TooManyRequests -> 
                Result.failure(RateLimitExceededException("Rate limit reached"))
            else -> 
                Result.failure(ServiceUnavailableException("API returned ${response.status}"))
        }
    } catch (e: IOException) {
        Log.e(TAG, "search: Network error occurred", e)
        Result.failure(NoConnectivityException("Network error", e))
    } catch (e: Exception) {
        // Defensive catch block to capture unanticipated errors (e.g. serialization issues), 
        // while ensuring that cancellation is propagated.
        if (e is CancellationException) throw e
        Log.wtf(TAG, "search: Unexpected error occurred", e)
        Result.failure(UnexpectedServiceException("Unexpected error", e))
    }
}

/**
 * Maps the domain [Platform] to the IGDB API ID.
 */
private fun Platform.toIgdbId(): Int = when (this.abbreviation()) {
    "PS5" -> 167
    "XBOX" -> 169
    "SWITCH" -> 130
    "PC" -> 6
    "PS4" -> 48
    else -> 0 // Handle other platforms if needed
}

/**
 * Maps the domain [Game.Category] to the IGDB API ID.
 */
private fun Game.Category.toIgdbId(): Int = when (this) {
    Game.Category.MAIN_GAME -> 0
    Game.Category.DLC -> 1
    Game.Category.BUNDLE -> 3
    Game.Category.REMAKE -> 8
    Game.Category.REMASTER -> 9
}
