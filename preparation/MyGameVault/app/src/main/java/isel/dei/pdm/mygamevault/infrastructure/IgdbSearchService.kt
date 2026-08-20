package isel.dei.pdm.mygamevault.infrastructure

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.NoConnectivityException
import isel.dei.pdm.mygamevault.core.RateLimitExceededException
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.core.ServiceUnavailableException
import isel.dei.pdm.mygamevault.core.UnauthenticatedException
import isel.dei.pdm.mygamevault.core.UnexpectedServiceException
import isel.dei.pdm.mygamevault.MyGameVaultApplication
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

    override suspend fun search(query: String): Result<List<Game>> = try {
        Log.d(TAG, "search: started with query = \"$query\"")
        val apicalypseQuery = """
            fields name, first_release_date, cover.url;
            where name ~ "$query"*;
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
