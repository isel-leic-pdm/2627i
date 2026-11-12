package isel.dei.pdm.demos.gamevault.images

import isel.dei.pdm.demos.gamevault.domain.Uri

/**
 * Contract to be supported by the stores of local copies of images, so that the images of the
 * user's collection (e.g. games' covers and platforms' logos) do not depend on the network.
 *
 * The contract does not depend on Android types, so that it can be moved to a shared module if
 * the app ever becomes multiplatform.
 *
 * All operations report failures uniformly, by throwing an [ImageUnavailableException].
 */
interface ImageStore {
    /**
     * Stores a local copy of the image at the given [source], if there is none yet. Images are
     * identified by their source, so the same image is copied once, however many times it is
     * stored (e.g. a logo shared by versions of the same platform). Storing a copy (i.e. a URI of
     * this store) has no effect.
     * @param source The URI of the image to be copied (e.g. an https URI).
     * @return The URI of the local copy, which can be read through an [ImageSource] that
     * supports the store's URIs.
     * @throws ImageUnavailableException If the image cannot be read, or its copy cannot be
     * written.
     */
    suspend fun store(source: Uri): Uri

    /**
     * Deletes the local copy at the given [uri]. Has no effect if it is not one of the store's
     * URIs, or if the copy does not exist. As the same copy may be shared, it must only be deleted
     * once no one refers to it (e.g. the last game or platform that uses it is removed).
     * @param uri The URI of the local copy.
     * @throws ImageUnavailableException If the copy exists, but cannot be deleted.
     */
    suspend fun delete(uri: Uri)
}

/**
 * Stores a local copy of the image at the given [uri], if any, keeping the [uri] itself if the
 * copy cannot be stored (e.g. without connectivity), so that the image can still be displayed
 * (when back online) and copied later.
 * @return The URI of the local copy, the given [uri] if it could not be copied, or null if [uri]
 * is null.
 */
suspend fun ImageStore.storeOrKeep(uri: Uri?): Uri? =
    uri?.let {
        try {
            store(it)
        } catch (_: ImageUnavailableException) {
            it
        }
    }
