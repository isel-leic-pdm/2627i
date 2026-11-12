package isel.dei.pdm.demos.gamevault.adapters.igdb

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.RefreshTokensParams
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

internal const val IGDB_HOST = "api.igdb.com"
internal const val IGDB_BASE_URL = "https://$IGDB_HOST/v4"
internal const val TWITCH_TOKEN_URL = "https://id.twitch.tv/oauth2/token"

/**
 * The JSON configuration used to parse the responses of IGDB and of the Twitch token endpoint.
 * Only the fields used by the app are declared in the DTOs, so the remaining ones are ignored.
 */
internal val igdbJson = Json { ignoreUnknownKeys = true }

/**
 * The maximum number of retries of a request that failed with a transient error.
 */
internal const val MAX_RETRIES = 3

/**
 * How long establishing a connection may take, in each attempt, before it is considered failed.
 */
internal const val CONNECT_TIMEOUT_MILLIS = 10_000L

/**
 * How long a request may take, in each attempt, from sending it to receiving the whole response,
 * before it is considered failed.
 */
internal const val REQUEST_TIMEOUT_MILLIS = 15_000L

/**
 * Creates the HTTP client used to access IGDB. It is meant to be shared by all IGDB services, so
 * that they also share the access token.
 *
 * The client includes the following resilience measures, through Ktor plugins:
 * - Retries ([HttpRequestRetry]): requests that fail with a transient error (I/O errors, including
 * timeouts, 5xx and 429 responses) are retried up to [MAX_RETRIES] times, with an exponential
 * delay between attempts.
 * - Access token renewal ([Auth]): the access token is read from the [credentials] repository. If
 * IGDB rejects it (401), or none is stored, a new one is obtained from Twitch using the stored
 * secrets, saved in the repository, and the request is repeated. Concurrent rejections trigger a
 * single renewal.
 *
 * The order in which the plugins are installed matters: the first one wraps the following ones.
 * Retries wrap token renewal, so each attempt uses the most recent access token.
 *
 * @param engine The HTTP engine (e.g. OkHttp in production, MockEngine in tests).
 * @param credentials The repository where the secrets and the access token are stored.
 * @param retryDelay The function used to wait between retries. Meant to be replaced in tests.
 */
fun createIgdbHttpClient(
    engine: HttpClientEngine,
    credentials: CredentialsRepository,
    retryDelay: suspend (millis: Long) -> Unit = { delay(it.milliseconds) }
): HttpClient = HttpClient(engine) {
    // Responses are not turned into exceptions: their status is translated by IgdbApi
    expectSuccess = false

    // Installing resilience measure: retries
    install(HttpRequestRetry) {
        retryIf(maxRetries = MAX_RETRIES) { _, response ->
            response.status.value in 500..599 || response.status == HttpStatusCode.TooManyRequests
        }
        retryOnExceptionIf(maxRetries = MAX_RETRIES) { _, cause -> cause.isNetworkError() }
        exponentialDelay(baseDelayMs = 500, maxDelayMs = 4_000, randomizationMs = 250)
        delay(retryDelay)
    }

    // Installed after HttpRequestRetry, so that each attempt has its own timeout
    install(HttpTimeout) {
        connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
        requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }

    // Finally, we install the authentication plugin, that automatically refreshes de AT
    install(Auth) {
        bearer {

            // The repository is the source of truth, so the token is read from it on every request
            // (DataStore keeps it in memory). Changes made elsewhere (e.g. in the settings) are thus
            // picked up without having to clear Ktor's cache.
            cacheTokens = false

            // The renewal completes even if the request that triggered it is canceled (e.g. the
            // user keeps typing), so that the new token is not lost
            nonCancellableRefresh = true

            // The token is only sent to IGDB, never to other hosts
            sendWithoutRequest { request -> request.url.host == IGDB_HOST }

            // We used cacheTokens = false, so we must inform the plugin on how to get the token
            // from the application's local storage
            loadTokens {
                credentials.accessToken.first()?.let { BearerTokens(accessToken = it, refreshToken = null) }
            }

            // Also called when no token is stored yet: the request is sent without one, and IGDB
            // rejects it. Returning null means that the token cannot be renewed, in which case the
            // 401 response is returned to the caller.
            refreshTokens {
                val secrets = credentials.secrets.first() ?: return@refreshTokens null
                val accessToken = requestAccessToken(secrets) ?: return@refreshTokens null
                // Saved before returning, because the repeated request reads it from the repository
                credentials.saveAccessToken(accessToken)
                BearerTokens(accessToken = accessToken, refreshToken = null)
            }
        }
    }
}

/**
 * Requests a new access token from Twitch, using the client credentials flow.
 * @return The access token, or null if Twitch rejects the secrets.
 * @throws ServiceUnavailableException If Twitch is unavailable.
 * @throws UnexpectedServiceException If the response cannot be parsed.
 */
private suspend fun RefreshTokensParams.requestAccessToken(secrets: Secrets): String? {
    val response = client.post(TWITCH_TOKEN_URL) {
        // The request is not to be handled by the Auth plugin itself
        markAsRefreshTokenRequest()
        parameter("client_id", secrets.clientId)
        parameter("client_secret", secrets.clientSecret)
        parameter("grant_type", "client_credentials")
    }
    return when {
        response.status.isSuccess() -> try {
            igdbJson.decodeFromString<TwitchToken>(response.bodyAsText()).accessToken
        } catch (e: SerializationException) {
            throw UnexpectedServiceException("Unexpected response from the token endpoint", e)
        }
        response.status.value in 500..599 ->
            throw ServiceUnavailableException("Token endpoint unavailable (${response.status})")
        // 400 (invalid client id) and 403 (invalid client secret)
        else -> null
    }
}

/**
 * Whether this exception, thrown while sending a request, is a network error (including
 * timeouts), which is transient. The [HttpTimeout] plugin reports a timeout by cancelling the
 * request, so the timeout reaches the [HttpRequestRetry] plugin as the cause of a
 * [CancellationException], which is unwrapped. Other cancellations (e.g. the user leaving the
 * screen) are not network errors.
 */
private fun Throwable.isNetworkError(): Boolean {
    var exception: Throwable = this
    while (exception is CancellationException) {
        exception = exception.cause ?: return false
    }
    return exception is IOException
}

/**
 * The response of the Twitch token endpoint. The token's expiration is not used: the token is
 * renewed when IGDB rejects it.
 */
@Serializable
private data class TwitchToken(
    @SerialName("access_token") val accessToken: String
)
