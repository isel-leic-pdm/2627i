package isel.dei.pdm.demos.gamevault.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UriTests {

    @Test
    fun `Uri accepts URIs of any scheme`() {
        listOf(
            "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg",
            "content://vault/covers/1.jpg",
            "file:///data/user/0/app/files/images/abc.jpg",
            "vault-image://abc.jpg",
            "urn:isbn:0451450523",
            "x+y.z-w:path"
        ).forEach { value ->
            assertEquals(value, Uri(value).value)
        }
    }

    @Test
    fun `scheme is the part before the colon, in lower case`() {
        assertEquals("https", Uri("https://example.com/ps5.png").scheme)
        assertEquals("https", Uri("HTTPS://example.com/ps5.png").scheme)
        assertEquals("vault-image", Uri("vault-image://abc").scheme)
        assertEquals("urn", Uri("urn:isbn:0451450523").scheme)
    }

    @Test
    fun `Uri is displayed as its value`() {
        assertEquals("https://example.com/ps5.png", Uri("https://example.com/ps5.png").toString())
    }

    @Test
    fun `Uri rejects values without scheme`() {
        listOf("example.com/ps5.png", "/images/ps5.png", "://example.com", "1http://example.com", "")
            .forEach { value ->
                assertFailsWith<IllegalArgumentException>(value) { Uri(value) }
            }
    }

    @Test
    fun `Uri rejects values with nothing after the scheme`() {
        assertFailsWith<IllegalArgumentException> { Uri("https:") }
    }

    @Test
    fun `Uri rejects values with whitespace`() {
        listOf("http://example.com/cover image.jpg", " https://example.com", "https://example.com\n")
            .forEach { value ->
                assertFailsWith<IllegalArgumentException>(value) { Uri(value) }
            }
    }

    @Test
    fun `toUriOrNull converts URIs and returns null otherwise`() {
        assertEquals(Uri("https://example.com/ps5.png"), "https://example.com/ps5.png".toUriOrNull())
        assertNull("example.com/ps5.png".toUriOrNull())
        assertNull("http://example.com/cover image.jpg".toUriOrNull())
    }
}
