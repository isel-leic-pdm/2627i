package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference.Provider.IGDB
import isel.dei.pdm.demos.gamevault.domain.Uri
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameTests {

    private fun igdb(id: String) = SourceReference(IGDB, NonBlankString(id))

    // Game

    @Test
    fun `Game primary constructor leaves optional properties absent by default`() {
        // Act
        val game = Game(1L, NonBlankString("Elden Ring"))

        // Assert
        assertNull(game.releaseDate)
        assertNull(game.coverUri)
        assertNull(game.thumbnailUri)
        assertNull(game.source)
    }

    @Test
    fun `Game secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val game = Game(
            id = 1L,
            name = "Elden Ring",
            releaseDate = LocalDate(2022, 2, 25),
            coverUri = "content://vault/covers/1.jpg",
            thumbnailUri = "https://example.com/thumb.jpg",
            source = igdb("119133")
        )

        // Assert
        assertEquals(1L, game.id)
        assertEquals(NonBlankString("Elden Ring"), game.name)
        assertEquals(LocalDate(2022, 2, 25), game.releaseDate)
        assertEquals(Uri("content://vault/covers/1.jpg"), game.coverUri)
        assertEquals(Uri("https://example.com/thumb.jpg"), game.thumbnailUri)
        assertEquals(igdb("119133"), game.source)
    }

    @Test
    fun `Game secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Game(id = 1L, name = "   ")
        }
    }

    @Test
    fun `Game secondary constructor throws IllegalArgumentException when coverUri is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            Game(id = 1L, name = "Elden Ring", coverUri = " invalid_uri ")
        }
    }

    @Test
    fun `Game secondary constructor throws IllegalArgumentException when thumbnailUri is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            Game(id = 1L, name = "Elden Ring", thumbnailUri = " invalid_uri ")
        }
    }

    // NewGame

    @Test
    fun `NewGame primary constructor leaves optional properties absent by default`() {
        // Act
        val newGame = NewGame(NonBlankString("Hades II"))

        // Assert
        assertNull(newGame.releaseDate)
        assertNull(newGame.coverUri)
        assertNull(newGame.thumbnailUri)
        assertNull(newGame.source)
    }

    @Test
    fun `NewGame secondary constructor converts intrinsic types into the corresponding types`() {
        // Act
        val newGame = NewGame(
            name = "Hades II",
            releaseDate = LocalDate(2025, 9, 25),
            coverUri = "content://vault/covers/2.jpg",
            source = igdb("277143")
        )

        // Assert
        assertEquals(NonBlankString("Hades II"), newGame.name)
        assertEquals(LocalDate(2025, 9, 25), newGame.releaseDate)
        assertEquals(Uri("content://vault/covers/2.jpg"), newGame.coverUri)
        assertNull(newGame.thumbnailUri)
        assertEquals(igdb("277143"), newGame.source)
    }

    @Test
    fun `NewGame secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            NewGame(name = "   ")
        }
    }

    @Test
    fun `NewGame secondary constructor throws IllegalArgumentException when coverUri is invalid`() {
        assertFailsWith<IllegalArgumentException> {
            NewGame(name = "Hades II", coverUri = " invalid_uri ")
        }
    }

    // SourceReference

    @Test
    fun `SourceReference cannot be created with a blank externalId`() {
        assertFailsWith<IllegalArgumentException> {
            igdb("   ")
        }
    }

    // Duplicate detection

    @Test
    fun `games with equal names and release years are the same game`() {
        val game = Game(1L, "God of War", LocalDate(2018, 4, 20))
        val other = Game(2L, "God of War", LocalDate(2018, 1, 1))

        assertTrue(game isSameGameAs other)
    }

    @Test
    fun `games with equal names and different release years are not the same game`() {
        val original = Game(1L, "God of War", LocalDate(2005, 3, 22))
        val reboot = Game(2L, "God of War", LocalDate(2018, 4, 20))

        assertFalse(original isSameGameAs reboot)
    }

    @Test
    fun `games with equal names are the same game when a release year is unknown`() {
        val game = Game(1L, "Hades II", LocalDate(2025, 9, 25))
        val withoutDate = Game(2L, "Hades II")

        assertTrue(game isSameGameAs withoutDate)
        assertTrue(withoutDate isSameGameAs game)
    }

    @Test
    fun `name comparison ignores case, punctuation and whitespace differences`() {
        val game = Game(1L, "The Legend of Zelda: Tears of the Kingdom")
        val other = Game(2L, "  the legend of zelda   tears of the KINGDOM ")

        assertTrue(game isSameGameAs other)
    }

    @Test
    fun `games with different names are not the same game`() {
        val game = Game(1L, "Hades")
        val sequel = Game(2L, "Hades II")

        assertFalse(game isSameGameAs sequel)
    }

    @Test
    fun `games from the same provider are compared by their external identifiers only`() {
        val game = Game(1L, "Elden Ring", source = igdb("119133"))
        val renamed = Game(2L, "My favourite game", source = igdb("119133"))
        val homonym = Game(3L, "Elden Ring", source = igdb("999999"))

        assertTrue(game isSameGameAs renamed)
        assertFalse(game isSameGameAs homonym)
    }

    @Test
    fun `games are compared by name and release year when only one has a source`() {
        val fromIgdb = Game(1L, "Elden Ring", source = igdb("119133"))
        val manual = Game(2L, "Elden Ring")

        assertTrue(fromIgdb isSameGameAs manual)
    }

    @Test
    fun `NewGame is the same game as a game in the vault following the same rules`() {
        val inVault = Game(1L, "God of War", LocalDate(2018, 4, 20))

        assertTrue(NewGame("God of War", LocalDate(2018, 4, 20)) isSameGameAs inVault)
        assertFalse(NewGame("God of War", LocalDate(2005, 3, 22)) isSameGameAs inVault)
    }
}
