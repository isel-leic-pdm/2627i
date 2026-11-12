package isel.dei.pdm.demos.gamevault.adapters.images

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.assertFailsWith

class FileImageStoreTest {

    private companion object {
        val COVER_URI = Uri("https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg")
        val THUMBNAIL_URI = Uri("https://images.igdb.com/igdb/image/upload/t_cover_small/co4jni.jpg")
        val COVER = byteArrayOf(1, 2, 3)
        val THUMBNAIL = byteArrayOf(4, 5)
    }

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    /**
     * An image source that serves the given images, counting the reads. Images that are not
     * served are unavailable.
     */
    private class CountingImageSource(private val images: Map<Uri, ByteArray>) : ImageSource {
        var reads = 0

        override suspend fun read(uri: Uri): ByteArray {
            reads++
            return images[uri] ?: throw ImageUnavailableException("Not served: $uri")
        }
    }

    private val source = CountingImageSource(mapOf(COVER_URI to COVER, THUMBNAIL_URI to THUMBNAIL))

    private val directory: File
        get() = File(temporaryFolder.root, "images")

    private val sut by lazy { FileImageStore(directory, source) }

    // Storing

    @Test
    fun store_copies_the_image_and_returns_the_uri_of_the_copy() = runTest {
        // Act
        val copy = sut.store(COVER_URI)

        // Assert
        assertEquals(FileImageStore.SCHEME, copy.scheme)
        assertArrayEquals(COVER, sut.read(copy))
        assertEquals(1, directory.listFiles()!!.size)
    }

    @Test
    fun copies_are_named_after_their_source_only() = runTest {
        // Act
        val cover = sut.store(COVER_URI)
        val thumbnail = sut.store(THUMBNAIL_URI)

        // Assert - Not paths, which may differ between devices
        assertNotEquals(cover, thumbnail)
        assertFalse(cover.value.contains(temporaryFolder.root.path))
        assertEquals(cover, FileImageStore(directory, source).store(COVER_URI))
    }

    @Test
    fun the_same_image_is_copied_once() = runTest {
        // Act
        val first = sut.store(COVER_URI)
        val second = sut.store(COVER_URI)

        // Assert
        assertEquals(first, second)
        assertEquals(1, source.reads)
    }

    @Test
    fun storing_a_copy_has_no_effect() = runTest {
        // Arrange
        val copy = sut.store(COVER_URI)

        // Act & Assert
        assertEquals(copy, sut.store(copy))
        assertEquals(1, source.reads)
    }

    @Test
    fun image_that_cannot_be_read_is_reported_as_unavailable_and_is_not_copied() = runTest {
        // Act & Assert
        assertFailsWith<ImageUnavailableException> {
            sut.store(Uri("https://images.igdb.com/igdb/image/upload/t_cover_big/unknown.jpg"))
        }
        assertTrue(directory.listFiles().isNullOrEmpty())
    }

    // Reading

    @Test
    fun reading_a_copy_that_does_not_exist_is_reported_as_unavailable() = runTest {
        // Arrange - A valid name, of a copy that was deleted (e.g. not restored from a backup)
        val copy = sut.store(COVER_URI)
        sut.delete(copy)

        // Act & Assert
        assertFailsWith<ImageUnavailableException> { sut.read(copy) }
    }

    @Test
    fun reading_uris_that_are_not_copies_is_reported_as_unavailable() = runTest {
        // Arrange - Including names that would refer to files outside the store's directory
        File(temporaryFolder.root, "secrets").writeText("secret")
        val uris = listOf(
            COVER_URI,
            Uri("${FileImageStore.SCHEME}://../secrets"),
            Uri("${FileImageStore.SCHEME}://secrets"),
            Uri("${FileImageStore.SCHEME}://${"0".repeat(64)}/../../secrets")
        )

        // Act & Assert
        uris.forEach { uri ->
            assertFailsWith<ImageUnavailableException>(uri.value) { sut.read(uri) }
        }
    }

    // Deleting

    @Test
    fun delete_removes_the_copy() = runTest {
        // Arrange
        val copy = sut.store(COVER_URI)

        // Act
        sut.delete(copy)

        // Assert
        assertTrue(directory.listFiles().isNullOrEmpty())
    }

    @Test
    fun deleting_uris_that_are_not_existing_copies_has_no_effect() = runTest {
        // Arrange
        val copy = sut.store(COVER_URI)

        // Act
        sut.delete(COVER_URI)
        sut.delete(Uri("${FileImageStore.SCHEME}://${"0".repeat(64)}"))

        // Assert
        assertArrayEquals(COVER, sut.read(copy))
    }
}
