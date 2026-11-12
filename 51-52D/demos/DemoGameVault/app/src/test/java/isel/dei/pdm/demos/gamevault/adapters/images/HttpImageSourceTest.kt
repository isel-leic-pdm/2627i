package isel.dei.pdm.demos.gamevault.adapters.images

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import isel.dei.pdm.demos.gamevault.adapters.igdb.Reply
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.test.assertFailsWith

class HttpImageSourceTest {

    private companion object {
        val IMAGE_URI = Uri("https://images.igdb.com/igdb/image/upload/t_cover_small/co4jni.jpg")
        val IMAGE_BYTES = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
    }

    /**
     * The requests sent, including those that fail with an exception.
     */
    private val requests = mutableListOf<HttpRequestData>()

    private fun newSut(reply: Reply): HttpImageSource =
        HttpImageSource(HttpClient(MockEngine { request -> requests += request; reply(request) }))

    @Test
    fun read_downloads_the_image() = runTest {
        // Arrange
        val sut = newSut { respond(IMAGE_BYTES, HttpStatusCode.OK) }

        // Act
        val bytes = sut.read(IMAGE_URI)

        // Assert
        assertArrayEquals(IMAGE_BYTES, bytes)
        assertEquals(IMAGE_URI.value, requests.single().url.toString())
    }

    @Test
    fun unsuccessful_response_is_reported_as_unavailable() = runTest {
        // Arrange
        val sut = newSut { respond("", HttpStatusCode.NotFound) }

        // Act & Assert
        assertFailsWith<ImageUnavailableException> { sut.read(IMAGE_URI) }
    }

    @Test
    fun network_error_is_reported_as_unavailable() = runTest {
        // Arrange
        val sut = newSut { throw IOException("Simulated network error") }

        // Act & Assert
        val error = assertFailsWith<ImageUnavailableException> { sut.read(IMAGE_URI) }
        assertTrue(error.cause is IOException)
    }

    @Test
    fun uri_that_is_not_http_is_reported_as_unavailable_without_requests() = runTest {
        // Arrange
        val sut = newSut { respond(IMAGE_BYTES, HttpStatusCode.OK) }

        // Act & Assert
        listOf(Uri("vault-image://abc.jpg"), Uri("content://vault/covers/1.jpg")).forEach { uri ->
            assertFailsWith<ImageUnavailableException> { sut.read(uri) }
        }
        assertTrue(requests.isEmpty())
    }
}
