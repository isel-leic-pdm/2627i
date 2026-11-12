package isel.dei.pdm.demos.gamevault.adapters.images

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.URLParserException
import io.ktor.http.isSuccess
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import java.io.IOException

/**
 * An [ImageSource] that downloads the images at http(s) URIs (e.g. IGDB's images, which are
 * public).
 *
 * @param client The HTTP client used to download the images. It must not be IGDB's API client,
 * whose authentication and retry policy are meant for the API, not for images.
 */
class HttpImageSource(private val client: HttpClient) : ImageSource {

    override suspend fun read(uri: Uri): ByteArray {
        if (uri.scheme != "http" && uri.scheme != "https")
            throw ImageUnavailableException("Not an http(s) URI: $uri")
        try {
            val response = client.get(uri.value)
            if (!response.status.isSuccess())
                throw ImageUnavailableException("Image request failed (${response.status}): $uri")
            return response.readRawBytes()
        } catch (e: IOException) {
            throw ImageUnavailableException("The image could not be downloaded: $uri", e)
        } catch (e: URLParserException) {
            throw ImageUnavailableException("Not a valid URL: $uri", e)
        }
    }
}
