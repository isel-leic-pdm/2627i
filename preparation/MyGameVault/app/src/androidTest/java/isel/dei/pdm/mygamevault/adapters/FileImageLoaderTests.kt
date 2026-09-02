package isel.dei.pdm.mygamevault.adapters

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileImageLoaderTests {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun file_cache_hit_returns_cached_bitmap_and_does_not_call_decorated_loader() = runTest {
        // Arrange
        val cacheDir = tempFolder.newFolder("images_hit")
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.success(createBitmap(1, 1).asImageBitmap())
        }
        val sut = createFileCachedImageLoader(cacheDir, decorated, maxEntries = 2)
        val uri = Uri("https://example.com/1")

        // Act: First call (Miss & Store)
        sut(uri)
        assertEquals(1, callCount)

        // Act: Second call (Hit)
        val result = sut(uri)

        // Assert
        assertEquals(1, callCount)
        assertTrue(result.isSuccess)
    }

    @Test
    fun file_cache_miss_calls_decorated_loader_and_stores_result() = runTest {
        // Arrange
        val cacheDir = tempFolder.newFolder("images_miss")
        var lastUriCalled: Uri? = null
        val decorated: ImageLoader = { uri ->
            lastUriCalled = uri
            Result.success(createBitmap(1, 1).asImageBitmap())
        }
        val sut = createFileCachedImageLoader(cacheDir, decorated, maxEntries = 2)
        val uri = Uri("https://example.com/1")

        // Act
        val result = sut(uri)

        // Assert
        assertEquals(uri, lastUriCalled)
        assertTrue(result.isSuccess)
        
        // Verify file exists
        val expectedFile = File(cacheDir, uri.value.hashCode().toString() + ".cache")
        assertTrue("Cache file should exist", expectedFile.exists())
    }

    @Test
    fun file_cache_uses_LRU_policy() = runTest {
        // Arrange
        val cacheDir = tempFolder.newFolder("images_lru")
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.success(createBitmap(1, 1).asImageBitmap())
        }
        val sut = createFileCachedImageLoader(cacheDir, decorated, maxEntries = 2)
        val uri1 = Uri("https://example.com/1")
        val uri2 = Uri("https://example.com/2")
        val uri3 = Uri("https://example.com/3")

        // Act
        sut(uri1) // [uri1]
        Thread.sleep(100)
        sut(uri2) // [uri1, uri2]
        Thread.sleep(100)
        
        // Access uri1 to make it most recent
        sut(uri1) // Hit. Updates lastModified of uri1
        Thread.sleep(100)
        
        // This should evict uri2 because uri1 was recently accessed
        sut(uri3) 
        assertEquals(3, callCount)

        // Assert: uri1 is still there (Hit)
        sut(uri1)
        assertEquals(3, callCount)

        // Assert: uri2 was evicted (Miss)
        sut(uri2)
        assertEquals(4, callCount)
    }

    @Test
    fun file_cache_recovers_from_corrupted_file() = runTest {
        // Arrange
        val cacheDir = tempFolder.newFolder("images_corrupted")
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.success(createBitmap(1, 1).asImageBitmap())
        }
        val sut = createFileCachedImageLoader(cacheDir, decorated, maxEntries = 2)
        val uri = Uri("https://example.com/corrupted")

        // Create a corrupted file (0 bytes is invalid for a PNG)
        val file = File(cacheDir, uri.value.hashCode().toString() + ".cache")
        file.createNewFile()
        assertTrue("Corrupted file should exist", file.exists())
        assertEquals(0, file.length())

        // Act
        val result = sut(uri)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("Should have called decorated loader to recover", 1, callCount)

        // Verify corrupted file was replaced with a valid one
        assertTrue("Valid file should have content", file.length() > 0)
    }

    @Test
    fun failed_loads_are_not_stored_in_file_cache() = runTest {
        // Arrange
        val cacheDir = tempFolder.newFolder("images_fail")
        var callCount = 0
        val decorated: ImageLoader = {
            callCount++
            Result.failure(RuntimeException("Fail"))
        }
        val sut = createFileCachedImageLoader(cacheDir, decorated, maxEntries = 2)
        val uri = Uri("https://example.com/fail")

        // Act
        sut(uri)
        sut(uri)

        // Assert
        assertEquals(2, callCount)
        val files = cacheDir.listFiles()
        assertTrue(files == null || files.isEmpty())
    }
}
