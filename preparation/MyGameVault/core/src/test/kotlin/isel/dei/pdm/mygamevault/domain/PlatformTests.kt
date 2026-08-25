package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlatformTests {

    @Test
    fun `platform holds its properties`() {
        val platform = Platform("PS5", "PlayStation 5", "https://example.com/logo.png")
        assertEquals("PS5", platform.abbreviation.value)
        assertEquals("PlayStation 5", platform.name.value)
        assertEquals("https://example.com/logo.png", platform.logoUri?.value)
    }

    @Test
    fun `platform logo can be null`() {
        val platform = Platform("PS5", "PlayStation 5")
        assertNull(platform.logoUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `abbreviation cannot be blank`() {
        Platform("", "PlayStation 5")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `name cannot be blank`() {
        Platform("PS5", " ")
    }
}
