package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NonBlankStringTests {

    @Test
    fun `non-blank string holds its value`() {
        val s = NonBlankString("test")
        assertEquals("test", s.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty string is not allowed`() {
        NonBlankString("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank string is not allowed`() {
        NonBlankString("   ")
    }

    @Test
    fun `invoke operator returns the value`() {
        val s = NonBlankString("test")
        assertEquals("test", s())
    }

    @Test
    fun `length property returns the correct length`() {
        val s = NonBlankString("test")
        assertEquals(4, s.length)
    }

    @Test
    fun `get returns the correct character`() {
        val s = NonBlankString("test")
        assertEquals('e', s[1])
    }

    @Test
    fun `subSequence returns the correct substring`() {
        val s = NonBlankString("testing")
        val sub = s.subSequence(0, 4)
        assertEquals("test", sub.toString())
    }

    @Test
    fun `contains works directly on NonBlankString`() {
        val s = NonBlankString("Elden Ring")
        assertTrue(s.contains("Elden"))
    }
}
