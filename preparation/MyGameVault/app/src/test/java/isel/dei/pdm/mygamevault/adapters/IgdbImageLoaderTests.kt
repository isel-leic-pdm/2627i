package isel.dei.pdm.mygamevault.adapters

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.ports.Secrets
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.ports.UnauthenticatedException
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IgdbImageLoaderTests {

    private class FakeSecretsRepository(clientId: String, secret: String) : SecretsRepository {
        override val secrets = flowOf(Secrets(clientId, secret))
        override suspend fun saveSecrets(secrets: Secrets) = Result.success(Unit)
    }

    @Test
    fun `loader sends correct headers`() = runTest {
        // Arrange
        val clientId = "test-id"
        val secret = "test-secret"
        var capturedHeaders = headersOf()
        
        val mockEngine = MockEngine { request ->
            capturedHeaders = request.headers
            respond(
                content = byteArrayOf(1, 2, 3),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "image/jpeg")
            )
        }
        val httpClient = HttpClient(mockEngine)
        val sut = createIgdbImageLoader(httpClient, FakeSecretsRepository(clientId, secret))
        val uri = Uri("https://example.com/image.jpg")

        // Act
        sut(uri)

        // Assert
        assertEquals(clientId, capturedHeaders["Client-ID"])
        assertEquals("Bearer $secret", capturedHeaders["Authorization"])
    }

    @Test
    fun `loader fails if secrets are missing`() = runTest {
        // Arrange
        val emptyRepo = object : SecretsRepository {
            override val secrets = flowOf<Secrets?>(null)
            override suspend fun saveSecrets(secrets: Secrets) = Result.success(Unit)
        }
        val sut = createIgdbImageLoader(HttpClient(MockEngine { respond("") }), emptyRepo)

        // Act
        val result = sut(Uri("https://example.com/image.jpg"))

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is UnauthenticatedException)
    }

    @Test
    fun `loader fails for non-https uris`() = runTest {
        // Arrange
        val sut = createIgdbImageLoader(HttpClient(MockEngine { respond("") }), FakeSecretsRepository("id", "secret"))

        // Act
        val result = sut(Uri("http://insecure.com/image.jpg"))

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}
