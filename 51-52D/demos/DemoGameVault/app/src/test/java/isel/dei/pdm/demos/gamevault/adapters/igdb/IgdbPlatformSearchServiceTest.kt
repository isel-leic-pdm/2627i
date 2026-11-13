package isel.dei.pdm.demos.gamevault.adapters.igdb

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeCredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.CredentialsRepository
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.RateLimitExceededException
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.Secrets
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnauthenticatedException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class IgdbPlatformSearchServiceTest {

    private val secrets = Secrets(clientId = "client-id", clientSecret = "client-secret")
    private val query = SearchString("PS")

    // Scripted replies

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun json(body: String): Reply = { respond(body, HttpStatusCode.OK, jsonHeaders) }

    private fun status(status: HttpStatusCode): Reply = { respond("", status) }

    private fun token(accessToken: String): Reply =
        json("""{"access_token":"$accessToken","expires_in":5000000,"token_type":"bearer"}""")

    private val networkError: Reply = { throw IOException("Simulated network error") }

    /**
     * A reply that only arrives after the request timeout elapsed (see [scriptedEngine]'s
     * dispatcher, which makes it elapse in virtual time).
     */
    private val tooSlowReply: Reply = {
        delay(REQUEST_TIMEOUT_MILLIS + 1_000)
        respond("[]", HttpStatusCode.OK, jsonHeaders)
    }

    private val emptyResult: Reply = json("[]")

    /**
     * The requests sent to IGDB and to Twitch. MockEngine's request history is not used because it
     * does not include the requests that fail with an exception (e.g. a network error).
     */
    private val igdbRequests = mutableListOf<HttpRequestData>()
    private val twitchRequests = mutableListOf<HttpRequestData>()

    /**
     * When the requests to IGDB and to Twitch were sent, according to [scriptedEngine]'s clock.
     */
    private val igdbRequestTimes = mutableListOf<Long>()
    private val twitchRequestTimes = mutableListOf<Long>()

    /**
     * Creates an engine that replies to the requests to IGDB and to Twitch with the given replies,
     * in order. Unexpected requests (i.e. once the replies are exhausted) fail the test.
     * @param dispatcher The engine's dispatcher, if not the default one. Timeouts are measured on
     * it, so a test dispatcher makes them elapse in virtual time, instead of real time.
     * @param clock The clock used to record when the requests are sent (e.g. the virtual time).
     */
    private fun scriptedEngine(
        igdb: List<Reply>,
        twitch: List<Reply> = emptyList(),
        dispatcher: CoroutineDispatcher? = null,
        clock: () -> Long = { 0L }
    ): MockEngine {
        val igdbReplies = ArrayDeque(igdb)
        val twitchReplies = ArrayDeque(twitch)
        val config = MockEngineConfig().apply {
            dispatcher?.let { this.dispatcher = it }
            addHandler { request ->
                val isIgdb = request.url.host == IGDB_HOST
                (if (isIgdb) igdbRequests else twitchRequests).add(request)
                (if (isIgdb) igdbRequestTimes else twitchRequestTimes).add(clock())
                val replies = if (isIgdb) igdbReplies else twitchReplies
                val reply = replies.removeFirstOrNull() ?: error("Unexpected request to ${request.url}")
                reply(request)
            }
        }
        return MockEngine(config)
    }

    private fun newSut(
        engine: MockEngine,
        credentials: CredentialsRepository = FakeCredentialsRepository(secrets, "stored-token"),
        throttle: RequestThrottle = RequestThrottle(MAX_REQUESTS_PER_WINDOW, RATE_LIMIT_WINDOW)
    ): IgdbPlatformSearchService {
        val client = createIgdbHttpClient(engine, credentials, retryDelay = { }, throttle)
        return IgdbPlatformSearchService(IgdbApi(client, credentials))
    }

    // Searching

    @Test
    fun search_sends_the_query_with_the_credentials() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = listOf(emptyResult))
        val sut = newSut(engine)

        // Act
        sut.search(query)

        // Assert
        val request = igdbRequests.single()
        assertEquals("$IGDB_BASE_URL/platforms", request.url.toString())
        assertEquals("client-id", request.headers["Client-ID"])
        assertEquals("Bearer stored-token", request.headers[HttpHeaders.Authorization])
        assertEquals(platformsQuery(query), request.body.toByteArray().decodeToString())
    }

    @Test
    fun query_searches_name_abbreviation_and_alternative_name() {
        // Act
        val query = platformsQuery(SearchString(" PS "))

        // Assert
        assertTrue(
            query.contains("""where name ~ *"PS"* | abbreviation ~ *"PS"* | alternative_name ~ *"PS"*;""")
        )
    }

    @Test
    fun query_escapes_the_search_text() {
        // Act
        val query = platformsQuery(SearchString("""a"b\c"""))

        // Assert
        assertTrue(query.contains("""name ~ *"a\"b\\c"*"""))
    }

    @Test
    fun search_returns_one_platform_per_version() = runTest {
        // Arrange
        val response = """
            [
              { "id": 167, "abbreviation": "PS5", "name": "PlayStation 5",
                "platform_logo": { "id": 892, "image_id": "plos" },
                "versions": [
                  { "id": 273, "name": "Initial version", "platform_logo": { "id": 795, "image_id": "plm3" } },
                  { "id": 724, "name": "PlayStation 5 Pro", "platform_logo": { "id": 892, "image_id": "plos" } }
                ]
              },
              { "id": 48, "abbreviation": "PS4", "name": "PlayStation 4",
                "platform_logo": { "id": 231, "image_id": "pl6f" },
                "versions": [ { "id": 178, "name": "PlayStation 4 Slim" } ]
              }
            ]
        """.trimIndent()
        val sut = newSut(scriptedEngine(igdb = listOf(json(response))))

        // Act
        val platforms = sut.search(query)

        // Assert
        val logo = { id: String -> "https://images.igdb.com/igdb/image/upload/t_logo_med/$id.png" }
        assertEquals(
            listOf(
                PlatformInfo(167, "PS5", "PlayStation 5", logo("plm3")),
                PlatformInfo(167, "PS5", "PlayStation 5", logo("plos"), "PlayStation 5 Pro"),
                PlatformInfo(48, "PS4", "PlayStation 4", logo("pl6f"), "PlayStation 4 Slim"),
            ),
            platforms
        )
    }

    @Test
    fun search_handles_missing_data() = runTest {
        // Arrange
        val response = """
            [
              { "id": 413, "name": "Leapster Explorer" },
              { "id": 46, "abbreviation": "Vita", "name": "PlayStation Vita",
                "versions": [ { "id": 60, "name": "PlayStation Vita" }, { "id": 61 } ]
              },
              { "id": 999, "abbreviation": "X", "name": " " }
            ]
        """.trimIndent()
        val sut = newSut(scriptedEngine(igdb = listOf(json(response))))

        // Act
        val platforms = sut.search(query)

        // Assert
        assertEquals(
            listOf(
                PlatformInfo(413, "Leapster Explorer", "Leapster Explorer"),
                PlatformInfo(46, "Vita", "PlayStation Vita"),
            ),
            platforms
        )
    }

    @Test
    fun malformed_response_is_reported_as_unexpected() = runTest {
        // Arrange
        val sut = newSut(scriptedEngine(igdb = listOf(json("""{ "not": "an array" }"""))))

        // Act & Assert
        assertFailsWith<UnexpectedServiceException> { sut.search(query) }
    }

    // Authentication

    @Test
    fun missing_secrets_are_reported_as_unauthenticated_without_requests() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = emptyList())
        val sut = newSut(engine, FakeCredentialsRepository(initialSecrets = null))

        // Act & Assert
        assertFailsWith<UnauthenticatedException> { sut.search(query) }
        assertTrue(igdbRequests.isEmpty() && twitchRequests.isEmpty())
    }

    @Test
    fun failure_to_read_the_secrets_is_reported_as_unexpected() = runTest {
        // Arrange
        val sut = newSut(scriptedEngine(igdb = emptyList()), FakeCredentialsRepository(loadFails = true))

        // Act & Assert
        assertFailsWith<UnexpectedServiceException> { sut.search(query) }
    }

    @Test
    fun missing_token_is_obtained_and_saved() = runTest {
        // Arrange
        val credentials = FakeCredentialsRepository(secrets, initialAccessToken = null)
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized), emptyResult),
            twitch = listOf(token("new-token"))
        )
        val sut = newSut(engine, credentials)

        // Act
        sut.search(query)

        // Assert
        val twitchRequest = twitchRequests.single()
        assertEquals("client-id", twitchRequest.url.parameters["client_id"])
        assertEquals("client-secret", twitchRequest.url.parameters["client_secret"])
        assertEquals("client_credentials", twitchRequest.url.parameters["grant_type"])
        assertEquals(null, twitchRequest.headers[HttpHeaders.Authorization])
        assertEquals("Bearer new-token", igdbRequests.last().headers[HttpHeaders.Authorization])
        assertEquals("new-token", credentials.accessToken.first())
    }

    @Test
    fun rejected_token_is_renewed_and_the_request_repeated() = runTest {
        // Arrange
        val credentials = FakeCredentialsRepository(secrets, initialAccessToken = "stale-token")
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized), emptyResult),
            twitch = listOf(token("new-token"))
        )
        val sut = newSut(engine, credentials)

        // Act
        sut.search(query)

        // Assert
        val (first, second) = igdbRequests
        assertEquals("Bearer stale-token", first.headers[HttpHeaders.Authorization])
        assertEquals("Bearer new-token", second.headers[HttpHeaders.Authorization])
        assertEquals("new-token", credentials.accessToken.first())
    }

    @Test
    fun secrets_rejected_by_twitch_are_reported_as_unauthenticated() = runTest {
        // Arrange
        val credentials = FakeCredentialsRepository(secrets, initialAccessToken = "stale-token")
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized)),
            twitch = listOf(status(HttpStatusCode.Forbidden))
        )
        val sut = newSut(engine, credentials)

        // Act & Assert
        assertFailsWith<UnauthenticatedException> { sut.search(query) }
        assertEquals("stale-token", credentials.accessToken.first())
    }

    @Test
    fun token_rejected_after_renewal_is_reported_as_unauthenticated() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized), status(HttpStatusCode.Unauthorized)),
            twitch = listOf(token("new-token"))
        )
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<UnauthenticatedException> { sut.search(query) }
        assertEquals(1, twitchRequests.size)
    }

    @Test
    fun twitch_unavailable_is_reported_as_service_unavailable() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized)),
            // The token request is retried too
            twitch = List(MAX_RETRIES + 1) { status(HttpStatusCode.ServiceUnavailable) }
        )
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<ServiceUnavailableException> { sut.search(query) }
        assertEquals(1, igdbRequests.size)
        assertEquals(MAX_RETRIES + 1, twitchRequests.size)
    }

    // Retries

    @Test
    fun transient_server_error_is_retried() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = listOf(status(HttpStatusCode.ServiceUnavailable), emptyResult))
        val sut = newSut(engine)

        // Act
        sut.search(query)

        // Assert
        assertEquals(2, igdbRequests.size)
    }

    @Test
    fun transient_network_error_is_retried() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = listOf(networkError, emptyResult))
        val sut = newSut(engine)

        // Act
        sut.search(query)

        // Assert
        assertEquals(2, igdbRequests.size)
    }

    @Test
    fun persistent_server_error_is_reported_as_service_unavailable_after_retries() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = List(MAX_RETRIES + 1) { status(HttpStatusCode.BadGateway) })
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<ServiceUnavailableException> { sut.search(query) }
        assertEquals(MAX_RETRIES + 1, igdbRequests.size)
    }

    @Test
    fun persistent_rate_limiting_is_reported_as_rate_limit_exceeded_after_retries() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = List(MAX_RETRIES + 1) { status(HttpStatusCode.TooManyRequests) })
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<RateLimitExceededException> { sut.search(query) }
        assertEquals(MAX_RETRIES + 1, igdbRequests.size)
    }

    @Test
    fun persistent_network_error_is_reported_as_no_connectivity_after_retries() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = List(MAX_RETRIES + 1) { networkError })
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<NoConnectivityException> { sut.search(query) }
        assertEquals(MAX_RETRIES + 1, igdbRequests.size)
    }

    @Test
    fun request_that_times_out_is_retried() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = listOf(tooSlowReply, emptyResult),
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        val sut = newSut(engine)

        // Act
        sut.search(query)

        // Assert - The timeout is a transient error, like any other network error
        assertEquals(2, igdbRequests.size)
    }

    @Test
    fun persistent_timeout_is_reported_as_no_connectivity_after_retries() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = List(MAX_RETRIES + 1) { tooSlowReply },
            dispatcher = StandardTestDispatcher(testScheduler)
        )
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<NoConnectivityException> { sut.search(query) }
        assertEquals(MAX_RETRIES + 1, igdbRequests.size)
    }

    @Test
    fun client_error_is_reported_as_unexpected_without_retries() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = listOf(status(HttpStatusCode.BadRequest)))
        val sut = newSut(engine)

        // Act & Assert
        assertFailsWith<UnexpectedServiceException> { sut.search(query) }
        assertEquals(1, igdbRequests.size)
    }

    // Throttling (in virtual time)

    @Test
    fun requests_beyond_the_rate_limit_wait() = runTest {
        // Arrange
        val engine = scriptedEngine(igdb = List(3) { emptyResult }, clock = { currentTime })
        val throttle = RequestThrottle(maxRequests = 2, 1.seconds, testScheduler.timeSource)
        val sut = newSut(engine, throttle = throttle)

        // Act
        repeat(3) { sut.search(query) }

        // Assert
        assertEquals(listOf(0L, 0L, 1_000L), igdbRequestTimes)
    }

    @Test
    fun retries_are_throttled() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.ServiceUnavailable), emptyResult),
            clock = { currentTime }
        )
        val throttle = RequestThrottle(maxRequests = 1, 1.seconds, testScheduler.timeSource)
        val sut = newSut(engine, throttle = throttle)

        // Act
        sut.search(query)

        // Assert
        assertEquals(listOf(0L, 1_000L), igdbRequestTimes)
    }

    @Test
    fun requests_repeated_after_token_renewal_are_throttled_but_token_requests_are_not() = runTest {
        // Arrange
        val engine = scriptedEngine(
            igdb = listOf(status(HttpStatusCode.Unauthorized), emptyResult),
            twitch = listOf(token("new-token")),
            clock = { currentTime }
        )
        val throttle = RequestThrottle(maxRequests = 1, 1.seconds, testScheduler.timeSource)
        val sut = newSut(engine, throttle = throttle)

        // Act
        sut.search(query)

        // Assert - Twitch is not subject to IGDB's rate limit
        assertEquals(listOf(0L), twitchRequestTimes)
        assertEquals(listOf(0L, 1_000L), igdbRequestTimes)
    }
}
