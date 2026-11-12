package isel.dei.pdm.demos.gamevault.adapters.images

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.createBitmap
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.util.concurrent.atomic.AtomicInteger

/**
 * Tests for [cachingImageLoader], with the actual decoding, which requires Android.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CachingImageLoaderTest {

    private companion object {
        val IMAGE_URI = Uri("https://images.igdb.com/igdb/image/upload/t_cover_small/co4jni.jpg")
        val OTHER_URI = Uri("https://images.igdb.com/igdb/image/upload/t_cover_small/co1r76.jpg")

        /**
         * The encoded content (PNG) of an image with the given size.
         */
        fun png(width: Int, height: Int): ByteArray {
            val bitmap = createBitmap(width, height).apply { eraseColor(Color.BLUE) }
            return ByteArrayOutputStream()
                .also { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                .toByteArray()
        }
    }

    /**
     * An image source that serves the given images, counting the reads of each one. Images that
     * are not served are unavailable.
     */
    private class CountingImageSource(private val images: Map<Uri, ByteArray>) : ImageSource {
        val reads = mutableMapOf<Uri, Int>()

        override suspend fun read(uri: Uri): ByteArray {
            reads[uri] = (reads[uri] ?: 0) + 1
            return images[uri] ?: throw ImageUnavailableException("Not served: $uri")
        }
    }

    /**
     * An image source whose reads only complete once the [gate] is opened, counting them. It
     * serves the given [image] for every URI, or reports them as unavailable if there is none.
     */
    private class GatedImageSource(private val image: ByteArray?) : ImageSource {
        val gate = CompletableDeferred<Unit>()
        val reads = AtomicInteger(0)

        override suspend fun read(uri: Uri): ByteArray {
            reads.incrementAndGet()
            gate.await()
            return image ?: throw ImageUnavailableException("Not served: $uri")
        }
    }

    @Test
    fun loads_and_decodes_the_image() = runTest {
        // Arrange
        val sut = cachingImageLoader(CountingImageSource(mapOf(IMAGE_URI to png(90, 128))))

        // Act
        val image = sut(IMAGE_URI).getOrThrow()

        // Assert
        assertEquals(90, image.width)
        assertEquals(128, image.height)
    }

    @Test
    fun loaded_images_are_cached() = runTest {
        // Arrange
        val source = CountingImageSource(mapOf(IMAGE_URI to png(90, 128)))
        val sut = cachingImageLoader(source)

        // Act
        val first = sut(IMAGE_URI).getOrThrow()
        val second = sut(IMAGE_URI).getOrThrow()

        // Assert
        assertTrue(first === second)
        assertEquals(1, source.reads[IMAGE_URI])
    }

    @Test
    fun least_recently_used_images_are_evicted_when_the_cache_is_full() = runTest {
        // Arrange - Room for a single image
        val source = CountingImageSource(mapOf(IMAGE_URI to png(90, 128), OTHER_URI to png(90, 128)))
        val sut = cachingImageLoader(source, maxCacheEntries = 1)

        // Act
        sut(IMAGE_URI)
        sut(OTHER_URI)
        sut(IMAGE_URI)

        // Assert - The first image was evicted by the second one, so it was read again
        assertEquals(2, source.reads[IMAGE_URI])
    }

    @Test
    fun unavailable_image_is_a_failure_and_is_not_cached() = runTest {
        // Arrange
        val source = CountingImageSource(emptyMap())
        val sut = cachingImageLoader(source)

        // Act
        val first = sut(IMAGE_URI)
        val second = sut(IMAGE_URI)

        // Assert - Not cached, so that it is read again (e.g. once back online)
        assertTrue(first.exceptionOrNull() is ImageUnavailableException)
        assertTrue(second.isFailure)
        assertEquals(2, source.reads[IMAGE_URI])
    }

    @Test
    fun content_that_is_not_an_image_is_a_failure() = runTest {
        // Arrange
        val sut =
            cachingImageLoader(CountingImageSource(mapOf(IMAGE_URI to "<html/>".toByteArray())))

        // Act
        val result = sut(IMAGE_URI)

        // Assert
        assertTrue(result.exceptionOrNull() is ImageUnavailableException)
    }

    // Concurrent loads

    @Test
    fun concurrent_loads_of_the_same_image_share_a_single_read() = runTest {
        // Arrange
        val source = GatedImageSource(png(90, 128))
        val sut = cachingImageLoader(source)

        // Act - Both are waiting for the image before it is read
        val first = async { sut(IMAGE_URI) }
        val second = async { sut(IMAGE_URI) }
        runCurrent()
        source.gate.complete(Unit)

        // Assert
        assertTrue(first.await().getOrThrow() === second.await().getOrThrow())
        assertEquals(1, source.reads.get())
    }

    @Test
    fun failure_of_concurrent_loads_is_reported_to_all_and_is_not_cached() = runTest {
        // Arrange
        val source = GatedImageSource(image = null)
        val sut = cachingImageLoader(source)

        // Act
        val first = async { sut(IMAGE_URI) }
        val second = async { sut(IMAGE_URI) }
        runCurrent()
        source.gate.complete(Unit)

        // Assert - Both fail, from a single read, and the next load reads the image again
        assertTrue(first.await().exceptionOrNull() is ImageUnavailableException)
        assertTrue(second.await().exceptionOrNull() is ImageUnavailableException)
        assertEquals(1, source.reads.get())
        assertTrue(sut(IMAGE_URI).isFailure)
        assertEquals(2, source.reads.get())
    }

    @Test
    fun cancelling_the_load_that_started_the_read_does_not_affect_the_others() = runTest {
        // Arrange
        val source = GatedImageSource(png(90, 128))
        val sut = cachingImageLoader(source)

        // Act - e.g. the first item is scrolled away while its image is being read
        val first = launch { sut(IMAGE_URI) }
        val second = async { sut(IMAGE_URI) }
        runCurrent()
        first.cancel()
        source.gate.complete(Unit)

        // Assert - The first one only ends, cancelled, once the load is complete
        assertTrue(second.await().isSuccess)
        first.join()
        assertTrue(first.isCancelled)
        assertEquals(1, source.reads.get())
    }
}
