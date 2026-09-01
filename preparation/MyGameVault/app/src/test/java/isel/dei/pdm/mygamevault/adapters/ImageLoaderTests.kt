package isel.dei.pdm.mygamevault.adapters

import androidx.compose.ui.graphics.ImageBitmap
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageLoaderTests {

    private class MockImageBitmap : ImageBitmap {
        override val width: Int = 1
        override val height: Int = 1
        override val config: androidx.compose.ui.graphics.ImageBitmapConfig = androidx.compose.ui.graphics.ImageBitmapConfig.Argb8888
        override val colorSpace: androidx.compose.ui.graphics.colorspace.ColorSpace = androidx.compose.ui.graphics.colorspace.ColorSpaces.Srgb
        override val hasAlpha: Boolean = true
        override fun prepareToDraw() {}
        override fun readPixels(buffer: IntArray, startX: Int, startY: Int, width: Int, height: Int, bufferOffset: Int, stride: Int) {}
    }

    @Test
    fun `cache hit returns cached bitmap and does not call decorated loader`() = runTest {
        // Arrange
        var callCount = 0
        val expectedBitmap = MockImageBitmap()
        val decorated: ImageLoader = {
            callCount++
            Result.success(expectedBitmap)
        }
        val sut = createCachedImageLoader(decorated, maxEntries = 2)
        val uri = Uri("https://example.com/1")

        // Act: First call (Miss)
        val result1 = sut(uri)
        // Act: Second call (Hit)
        val result2 = sut(uri)

        // Assert
        assertEquals(1, callCount)
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        assertSame(expectedBitmap, result1.getOrNull())
        assertSame(expectedBitmap, result2.getOrNull())
    }

    @Test
    fun `cache miss calls decorated loader and stores result`() = runTest {
        // Arrange
        var lastUriCalled: Uri? = null
        val decorated: ImageLoader = { uri ->
            lastUriCalled = uri
            Result.success(MockImageBitmap())
        }
        val sut = createCachedImageLoader(decorated, maxEntries = 2)
        val uri = Uri("https://example.com/1")

        // Act
        val result = sut(uri)

        // Assert
        assertEquals(uri, lastUriCalled)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `cache evicts oldest entry when limit reached`() = runTest {
        // Arrange
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.success(MockImageBitmap())
        }
        val sut = createCachedImageLoader(decorated, maxEntries = 2)
        val uri1 = Uri("https://example.com/1")
        val uri2 = Uri("https://example.com/2")
        val uri3 = Uri("https://example.com/3")

        // Act
        sut(uri1) // Miss, size 1, [uri1]
        sut(uri2) // Miss, size 2, [uri1, uri2]
        assertEquals(2, callCount)

        sut(uri3) // Miss, size 2, evicts uri1, [uri2, uri3]
        assertEquals(3, callCount)

        // Re-request uri1 (should be a Miss now)
        sut(uri1)
        assertEquals(4, callCount)

        // Re-request uri3 (should still be a Hit)
        sut(uri3)
        assertEquals(4, callCount)
    }

    @Test
    fun `cache uses LRU policy`() = runTest {
        // Arrange
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.success(MockImageBitmap())
        }
        val sut = createCachedImageLoader(decorated, maxEntries = 2)
        val uri1 = Uri("https://example.com/1")
        val uri2 = Uri("https://example.com/2")
        val uri3 = Uri("https://example.com/3")

        // Act
        sut(uri1) // [uri1]
        sut(uri2) // [uri1, uri2]
        sut(uri1) // Hit, moves uri1 to end: [uri2, uri1]
        assertEquals(2, callCount)

        sut(uri3) // Miss, evicts uri2: [uri1, uri3]
        assertEquals(3, callCount)

        // uri1 should still be a Hit
        sut(uri1)
        assertEquals(3, callCount)

        // uri2 should be a Miss
        sut(uri2)
        assertEquals(4, callCount)
    }

    @Test
    fun `failed loads are not cached`() = runTest {
        // Arrange
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.failure(RuntimeException("Fail"))
        }
        val sut = createCachedImageLoader(decorated, maxEntries = 2)
        val uri = Uri("https://example.com/fail")

        // Act
        sut(uri)
        sut(uri)

        // Assert
        assertEquals(2, callCount)
    }
}
