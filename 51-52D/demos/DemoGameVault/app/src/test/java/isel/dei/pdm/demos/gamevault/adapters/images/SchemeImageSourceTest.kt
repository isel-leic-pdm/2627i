package isel.dei.pdm.demos.gamevault.adapters.images

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.images.ImageSource
import isel.dei.pdm.demos.gamevault.images.ImageUnavailableException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import kotlin.test.assertFailsWith

class SchemeImageSourceTest {

    private val web = ImageSource { byteArrayOf(1) }
    private val local = ImageSource { byteArrayOf(2) }

    private val sut = SchemeImageSource(mapOf("https" to web, "vault-image" to local))

    @Test
    fun images_are_read_from_the_source_of_their_scheme() = runTest {
        assertArrayEquals(byteArrayOf(1), sut.read(Uri("https://example.com/cover.jpg")))
        assertArrayEquals(byteArrayOf(1), sut.read(Uri("HTTPS://example.com/cover.jpg")))
        assertArrayEquals(byteArrayOf(2), sut.read(Uri("vault-image://abc")))
    }

    @Test
    fun images_of_unsupported_schemes_are_reported_as_unavailable() = runTest {
        assertFailsWith<ImageUnavailableException> { sut.read(Uri("content://vault/covers/1.jpg")) }
    }
}
