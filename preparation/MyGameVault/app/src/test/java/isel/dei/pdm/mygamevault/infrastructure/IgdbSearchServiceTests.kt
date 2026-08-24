package isel.dei.pdm.mygamevault.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.NoConnectivityException
import isel.dei.pdm.mygamevault.core.NonBlankString
import isel.dei.pdm.mygamevault.core.RateLimitExceededException
import isel.dei.pdm.mygamevault.core.ServiceUnavailableException
import isel.dei.pdm.mygamevault.core.UnauthenticatedException
import isel.dei.pdm.mygamevault.core.UnexpectedServiceException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IgdbSearchServiceTests {

    @Test
    fun `search sends correct headers and body`() = runTest {
        // Arrange
        val clientId = "test-client-id"
        val token = "test-token"
        val query = "Zelda"
        var capturedRequestContent = ""
        var capturedHeaders = headersOf()

        val mockEngine = MockEngine { request ->
            capturedRequestContent = request.body.toByteArray().decodeToString()
            capturedHeaders = request.headers
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = IgdbSearchService(httpClient, clientId, token)

        // Act
        val result = service.search(NonBlankString(query), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(clientId, capturedHeaders["Client-ID"])
        assertEquals("Bearer $token", capturedHeaders["Authorization"])
        assertTrue("Query should contain search term", capturedRequestContent.contains("& name ~ \"$query\"*"))
        assertTrue("Query should contain platform", capturedRequestContent.contains("where platforms = (167)"))
        assertTrue("Query should contain fields", capturedRequestContent.contains("fields name"))
    }

    @Test
    fun `search returns mapped games when API succeeds`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            respond(
                content = """
                    [
                        {
                            "id": 123,
                            "name": "Elden Ring",
                            "first_release_date": 1645747200,
                            "cover": { "id": 1, "url": "//images.igdb.com/igdb/image/upload/t_thumb/co4jni.jpg" }
                        }
                    ]
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isSuccess)
        val games = result.getOrThrow()
        assertEquals(1, games.size)
        assertEquals("Elden Ring", games[0].name.value)
        assertEquals(123L, games[0].id)
        assertEquals("https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg", games[0].coverUri?.value)
        assertEquals("https://images.igdb.com/igdb/image/upload/t_thumb/co4jni.jpg", games[0].thumbnailUri?.value)
    }

    @Test
    fun `search returns empty list when API returns empty array`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            respond(
                content = "[]",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Unknown"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `search returns failure when API returns 401`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Unauthorized",
                status = HttpStatusCode.Unauthorized
            )
        }
        val httpClient = HttpClient(mockEngine)
        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnauthenticatedException)
    }

    @Test
    fun `search returns failure when API returns 500`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Internal Server Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        val httpClient = HttpClient(mockEngine)
        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ServiceUnavailableException)
    }

    @Test
    fun `search returns failure when network exception occurs`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            throw java.io.IOException("No internet")
        }
        val httpClient = HttpClient(mockEngine)
        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is NoConnectivityException)
    }

    @Test
    fun `search returns failure when API returns 429`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            respond(
                content = "Too Many Requests",
                status = HttpStatusCode.TooManyRequests
            )
        }
        val httpClient = HttpClient(mockEngine)
        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is RateLimitExceededException)
    }

    @Test
    fun `search returns failure when unexpected exception occurs`() = runTest {
        // Arrange
        val mockEngine = MockEngine { _ ->
            throw IllegalStateException("Something broke")
        }
        val httpClient = HttpClient(mockEngine)
        val service = IgdbSearchService(httpClient, "id", "token")

        // Act
        val result = service.search(NonBlankString("Elden"), Game.Platform.PS5, null)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnexpectedServiceException)
    }
}
