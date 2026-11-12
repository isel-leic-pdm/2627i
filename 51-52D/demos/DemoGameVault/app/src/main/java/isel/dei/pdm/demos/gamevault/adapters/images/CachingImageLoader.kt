package isel.dei.pdm.demos.gamevault.adapters.images

import android.graphics.BitmapFactory
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageLoader
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * The maximum number of images kept in the memory cache, used by default. The images displayed
 * are mostly thumbnails (about 46 KB each, once decoded), so this amounts to some 9 MB.
 */
private const val DEFAULT_MAX_CACHE_ENTRIES: Int = 200

/**
 * Creates the [ImageLoader] used by the app: it reads the images from the given [source], decodes
 * them, and keeps the decoded images in a memory cache, so that they are displayed immediately
 * when needed again (e.g. when scrolling back in a list).
 *
 * Concurrent loads of the same image (e.g. the entries of the same game, one per platform, in the
 * search results) share a single read and decoding, performed by the first caller, while the
 * others wait for its result. The first caller completes the load even if canceled (e.g. an item
 * that is scrolled away), so that the others still get the image: its cancellation only takes
 * effect once the load is complete.
 *
 * Images that cannot be read or decoded are reported as failures, and are not cached, so that
 * they are read again the next time (e.g. once the device is back online).
 *
 * @param source The source the images are read from.
 * @param maxCacheEntries The maximum number of images kept in the memory cache.
 * @param decode Decodes the encoded content of an image, returning null if it is not one.
 */
fun cachingImageLoader(
    source: ImageSource,
    maxCacheEntries: Int = DEFAULT_MAX_CACHE_ENTRIES,
    decode: suspend (ByteArray) -> ImageBitmap? = ::decodeImageBitmap
): ImageLoader {

    // Holds the images being loaded, and those already loaded
    val cache = LruCache<Uri, Deferred<ImageBitmap>>(maxCacheEntries)
    val cacheMutex = Mutex()

    /**
     * Loads the image at [uri], completing [loading] with it, or with the failure. The cache entry
     * is removed before completing with a failure, so that the callers that see it and retry load
     * the image again. Only if not already replaced (e.g. after being evicted).
     */
    suspend fun load(uri: Uri, loading: CompletableDeferred<ImageBitmap>) {
        try {
            val image = decode(source.read(uri))
                ?: throw ImageUnavailableException("Not an image: $uri")
            loading.complete(image)
        } catch (e: Throwable) {
            // Any failure, so that the other callers are never left waiting
            cacheMutex.withLock { if (cache[uri] === loading) cache.remove(uri) }
            loading.completeExceptionally(e)
        }
    }

    return { uri ->
        var loading: CompletableDeferred<ImageBitmap>? = null
        val entry = cacheMutex.withLock {
            cache[uri] ?: CompletableDeferred<ImageBitmap>().also {
                loading = it
                cache.put(uri, it)
            }
        }

        // The first caller loads the image, even if canceled meanwhile, as the others depend on
        // it. The load must be completed within the block: if this caller was canceled, the
        // block's result is discarded and withContext throws CancellationException.
        loading?.let { withContext(NonCancellable) { load(uri, it) } }

        try {
            Result.success(entry.await())
        } catch (e: ImageUnavailableException) {
            Result.failure(e)
        }
    }
}

/**
 * Decodes the given encoded image (e.g. JPEG or PNG), off the main thread, as decoding is CPU
 * intensive.
 * @return The decoded image, or null if the content is not an image.
 */
private suspend fun decodeImageBitmap(bytes: ByteArray): ImageBitmap? =
    withContext(Dispatchers.Default) {
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
