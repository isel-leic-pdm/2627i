package isel.dei.pdm.demos.gamevault.adapters.images

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import isel.dei.pdm.demos.gamevault.images.ImageStore
import okhttp3.Cache
import java.io.File

/**
 * The maximum size of the disk cache of the downloaded images, used by default. Searching is
 * occasional (e.g. when adding a game), so the cache only needs to hold the recently seen images:
 * about a thousand thumbnails (about 5 KB each), i.e. some twenty searches of up to 50 results.
 * It is an upper bound: the cache only takes the space of the images it holds.
 */
private const val DEFAULT_CACHE_SIZE_BYTES = 5L * 1024 * 1024

/**
 * Creates the HTTP client used to download images (see [HttpImageSource]). It is not IGDB's API
 * client, whose authentication and retry policy are meant for the API (IGDB's images are public).
 *
 * The downloaded images are kept in a disk cache, following the caching headers of the responses
 * (IGDB's images are served for long periods). Therefore, images of repeated searches are not
 * downloaded again, and are displayed even without connectivity. Unlike the copies kept by the
 * [ImageStore], the cache is meant for transient images (e.g. search results): it is bounded, and
 * should be placed where the system may clear it (e.g. the app's cache directory).
 *
 * @param cacheDirectory The directory of the disk cache. It is created when needed.
 * @param cacheSizeBytes The maximum size of the disk cache, beyond which the least recently used
 * images are evicted.
 */
fun createImageHttpClient(
    cacheDirectory: File,
    cacheSizeBytes: Long = DEFAULT_CACHE_SIZE_BYTES
): HttpClient =
    HttpClient(
        OkHttp.create {
            config { cache(Cache(cacheDirectory, cacheSizeBytes)) }
        }
    )
