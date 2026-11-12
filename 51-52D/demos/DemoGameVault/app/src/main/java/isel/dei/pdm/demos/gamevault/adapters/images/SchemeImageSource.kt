package isel.dei.pdm.demos.gamevault.adapters.images

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException

/**
 * An [ImageSource] that reads each image from the source of its URI's scheme (e.g. https images
 * from the web, and the local copies from the store that keeps them).
 *
 * @param sources The sources of each scheme, in lower case (e.g. "https").
 */
class SchemeImageSource(private val sources: Map<String, ImageSource>) : ImageSource {

    override suspend fun read(uri: Uri): ByteArray {
        val source = sources[uri.scheme]
            ?: throw ImageUnavailableException("Unsupported scheme: $uri")
        return source.read(uri)
    }
}
