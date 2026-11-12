package isel.dei.pdm.demos.gamevault.images

import isel.dei.pdm.demos.gamevault.domain.Uri

/**
 * Contract to be supported by the sources of images (e.g. the web, or the local copies of the
 * collection's images). Sources provide the images' encoded content (e.g. JPEG or PNG bytes),
 * leaving decoding to whoever displays them.
 *
 * The contract does not depend on Android types, so that it can be moved to a shared module if
 * the app ever becomes multiplatform.
 *
 * All operations report failures uniformly, by throwing an [ImageUnavailableException].
 * Implementations must translate the errors of the underlying technology (e.g. network or file
 * system errors) into [ImageUnavailableException]s.
 */
fun interface ImageSource {
    /**
     * Reads the encoded content of the image at the given [uri].
     * @param uri The image's URI.
     * @return The image's encoded content.
     * @throws ImageUnavailableException If the image cannot be read (e.g. there is no
     * connectivity, it does not exist, or the source does not support the URI's scheme).
     */
    suspend fun read(uri: Uri): ByteArray
}

/**
 * Thrown when an image cannot be obtained.
 */
class ImageUnavailableException(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause)
