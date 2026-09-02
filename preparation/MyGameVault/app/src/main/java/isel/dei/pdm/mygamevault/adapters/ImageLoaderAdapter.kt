package isel.dei.pdm.mygamevault.adapters

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsBytes
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.ports.SecretsRepository
import isel.dei.pdm.mygamevault.ports.UnauthenticatedException
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

private val TAG = MyGameVaultApplication.buildTag("ImageLoaderAdapter")

/**
 * Creates an [ImageLoader] specialized for the IGDB API.
 */
fun createIgdbImageLoader(
    httpClient: HttpClient,
    secretsRepository: SecretsRepository
): ImageLoader = { uri ->
    runCatching {
        Log.d(TAG, "createIgdbImageLoader: fetching uri $uri")
        val secrets = secretsRepository.secrets.first()
            ?: throw UnauthenticatedException("API credentials not configured")

        if (!uri.value.startsWith("https")) {
            throw IllegalArgumentException("Unsupported URI scheme: ${uri.value}")
        }

        val response = httpClient.get(uri.value) {
            header("Client-ID", secrets.clientId)
            header("Authorization", "Bearer ${secrets.clientSecret}")
        }

        val bytes = response.bodyAsBytes()
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        bitmap?.asImageBitmap() ?: throw IllegalStateException("Could not decode bitmap")
    }.onFailure {
        Log.e(TAG, "createIgdbImageLoader: error fetching uri $uri", it)
    }.onSuccess {
        Log.d(TAG, "createIgdbImageLoader: successfully fetched uri $uri")
    }
}

/**
 * Decorates an [ImageLoader] with an in-memory LRU cache.
 *
 * @param decorated The [ImageLoader] to be decorated.
 * @param maxEntries The maximum number of entries to keep in the cache.
 * @return A new [ImageLoader] with caching capabilities.
 */
fun createCachedImageLoader(
    decorated: ImageLoader,
    maxEntries: Int
): ImageLoader {
    require(maxEntries > 0) { "maxEntries must be greater than 0" }

    val cache = Collections.synchronizedMap(
        object : LinkedHashMap<Uri, ImageBitmap>(maxEntries, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<Uri, ImageBitmap>): Boolean {
                val evict = size > maxEntries
                if (evict) {
                    Log.d(TAG, "createCachedImageLoader: evicting entry ${eldest.key}")
                }
                return evict
            }
        }
    )

    return { uri ->
        val cached = cache[uri]
        if (cached != null) {
            Log.d(TAG, "createCachedImageLoader: cache hit for uri $uri")
            Result.success(cached)
        } else {
            Log.d(TAG, "createCachedImageLoader: cache miss for uri $uri")
            decorated(uri).onSuccess { bitmap ->
                cache[uri] = bitmap
            }
        }
    }
}

/**
 * Decorates an [ImageLoader] with a file-system based LRU cache.
 *
 * @param context The application context.
 * @param decorated The [ImageLoader] to be decorated.
 * @param maxEntries The maximum number of entries to keep in the cache.
 * @return A new [ImageLoader] with persistent caching capabilities.
 */
fun createFileCachedImageLoader(
    context: Context,
    decorated: ImageLoader,
    maxEntries: Int
): ImageLoader = createFileCachedImageLoader(
    cacheDir = File(context.cacheDir, "images"),
    decorated = decorated,
    maxEntries = maxEntries
)

/**
 * Internal version of the file-system based LRU cache loader, for testing purposes.
 */
internal fun createFileCachedImageLoader(
    cacheDir: File,
    decorated: ImageLoader,
    maxEntries: Int
): ImageLoader {
    require(maxEntries > 0) { "maxEntries must be greater than 0" }
    cacheDir.mkdirs()

    fun Uri.toFile() = File(cacheDir, this.value.hashCode().toString() + ".cache")

    return { uri ->
        val file = uri.toFile()
        var cachedResult: Result<ImageBitmap>? = null
        if (file.exists()) {
            val result = runCatching {
                val bytes = file.readBytes()
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    ?: throw IllegalStateException("Could not decode cached bitmap")
            }
            if (result.isSuccess) {
                Log.d(TAG, "createFileCachedImageLoader: cache hit for uri $uri")
                file.setLastModified(System.currentTimeMillis())
                cachedResult = result
            } else {
                Log.w(TAG, "createFileCachedImageLoader: corrupted file for $uri, deleting.")
                file.delete()
            }
        }

        cachedResult ?: run {
            Log.d(TAG, "createFileCachedImageLoader: cache miss or recovery for uri $uri")
            decorated(uri).onSuccess { bitmap ->
                runCatching {
                    // Evict oldest before writing new
                    val files = cacheDir.listFiles()
                    if (files != null && files.size >= maxEntries) {
                        files.minByOrNull { it.lastModified() }?.let { oldest ->
                            Log.d(TAG, "createFileCachedImageLoader: evicting oldest file ${oldest.name}")
                            oldest.delete()
                        }
                    }

                    // Save to disk
                    FileOutputStream(file).use { out ->
                        bitmap.asAndroidBitmap().compress(
                            android.graphics.Bitmap.CompressFormat.PNG,
                            100,
                            out
                        )
                    }
                    Log.d(TAG, "createFileCachedImageLoader: stored uri $uri to disk")
                }
            }
        }
    }
}
