package isel.dei.pdm.mygamevault.core

import org.junit.Assert.assertEquals
import org.junit.Test

class UriTests {

    @Test
    fun `can create uri with valid schema and ssp`() {
        val validUris = listOf("https://example.com", "file:path/to/file", "a:b")
        validUris.forEach {
            val uri = Uri(it)
            assertEquals(it, uri.value)
        }
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
    fun `cannot create blank uri`() {
        Uri("   ")
    }
}
