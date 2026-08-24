package isel.dei.pdm.mygamevault.core

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GameTests {

    @Test
    fun `can create game with valid name`() {
        // Arrange
        val name = "Elden Ring"
        
        // Act
        val game = Game(1, NonBlankString(name), LocalDate.of(2022, 2, 25), coverUri = null as Uri?, thumbnailUri = null)
        
        // Assert
        assertEquals(name, game.name.value)
    }

    @Test
    fun `can create game with string cover uri using convenience constructor`() {
        // Arrange
        val coverUriStr = "https://example.com/cover.jpg"
        
        // Act
        val game = Game(1, "Elden Ring", null, coverUri = coverUriStr, thumbnailUri = null as String?)
        
        // Assert
        assertEquals(Uri(coverUriStr), game.coverUri)
    }

    @Test
    fun `can create game with string thumbnail uri using convenience constructor`() {
        // Arrange
        val thumbUriStr = "https://example.com/thumb.jpg"
        
        // Act
        val game = Game(1, "Elden Ring", null, coverUri = null as String?, thumbnailUri = thumbUriStr)
        
        // Assert
        assertEquals(Uri(thumbUriStr), game.thumbnailUri)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create game with empty name`() {
        Game(1, "", null, coverUri = null as String?, thumbnailUri = null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `cannot create game with blank name`() {
        Game(1, "   ", null, coverUri = null as String?, thumbnailUri = null)
    }
}
