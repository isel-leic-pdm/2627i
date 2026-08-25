package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class UriTests {

    @Test
    fun `can create uri with https schema`() {
        val input = "https://example.com"
        val uri = Uri(input)
        assertEquals(input, uri.value)
    }

    @Test
    fun `can create uri with file schema`() {
        val input = "file:path/to/file"
        val uri = Uri(input)
        assertEquals(input, uri.value)
    }

    @Test
    fun `can create uri with simple schema`() {
        val input = "a:b"
        val uri = Uri(input)
        assertEquals(input, uri.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create uri without colon`() {
        Uri("https//example.com")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create uri with blank schema`() {
        Uri(":ssp")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create uri with blank ssp`() {
        Uri("https:")
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun `cannot create empty uri`() {
        Uri("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create blank uri`() {
        Uri("   ")
    }
}
