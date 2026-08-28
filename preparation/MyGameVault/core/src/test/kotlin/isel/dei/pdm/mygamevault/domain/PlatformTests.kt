package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlatformTests {

    @Test
    fun `platform holds its properties`() {
        val platform = Platform(167, "PS5", "PlayStation 5", "https://example.com/logo.png")
        assertEquals(167L, platform.id)
        assertEquals("PS5", platform.abbreviation())
        assertEquals("PlayStation 5", platform.name())
        assertEquals("https://example.com/logo.png", platform.logoUri?.value)
    }

    @Test
    fun `platform logo can be null`() {
        val platform = Platform(167, "PS5", "PlayStation 5")
        assertNull(platform.logoUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `abbreviation cannot be blank`() {
        Platform(167, "", "PlayStation 5")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `name cannot be blank`() {
        Platform(167, "PS5", " ")
    }
}
