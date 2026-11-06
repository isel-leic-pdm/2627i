package isel.dei.pdm.demos.gamevault.adapters.igdb

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.RateLimitExceededException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnauthenticatedException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import kotlinx.coroutines.flow.first
import java.io.IOException

/**
 * Executes queries on IGDB, translating every failure into a [SearchServiceException], as
 * required by the search services contracts. Meant to be shared by all IGDB services.
 *
 * The failures are only translated after the resilience measures of the [client] have run (see
 * [createIgdbHttpClient]), that is, once the retries are exhausted or the token cannot be renewed.
 *
 * @param client The HTTP client, created with [createIgdbHttpClient].
 * @param credentials The repository where the secrets are stored.
 */
class IgdbApi(
    private val client: HttpClient,
    private val credentials: CredentialsRepository
) {

    /**
     * Executes the given query on the given IGDB endpoint.
     * @param endpoint The endpoint (e.g. "platforms").
     * @param query The query, in the Apicalypse language.
     * @return The response's body (a JSON array).
     * @throws SearchServiceException If the query cannot be executed.
     */
    suspend fun query(endpoint: String, query: String): String {
        try {
            val secrets = credentials.secrets.first()
                ?: throw UnauthenticatedException("The client credentials are not defined")
            val response = client.post("$IGDB_BASE_URL/$endpoint") {
                header("Client-ID", secrets.clientId)
                contentType(ContentType.Text.Plain)
                setBody(query)
            }
            if (!response.status.isSuccess())
                throw response.toSearchServiceException()
            return response.bodyAsText()
        } catch (e: IOException) {
            throw NoConnectivityException("IGDB could not be reached", e)
        } catch (e: PersistenceException) {
            throw UnexpectedServiceException("The credentials could not be accessed", e)
        }
    }
}

/**
 * Translates this unsuccessful response into the corresponding search service exception.
 */
private fun HttpResponse.toSearchServiceException(): SearchServiceException {
    val message = "IGDB request failed ($status)"
    return when {
        status == HttpStatusCode.Unauthorized -> UnauthenticatedException(message)
        status == HttpStatusCode.TooManyRequests -> RateLimitExceededException(message)
        status.value in 500..599 -> ServiceUnavailableException(message)
        else -> UnexpectedServiceException(message)
    }
}
