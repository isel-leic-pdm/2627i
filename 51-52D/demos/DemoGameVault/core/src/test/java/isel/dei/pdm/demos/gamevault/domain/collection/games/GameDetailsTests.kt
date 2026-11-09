package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameDetailsTests {

    private val game = Game(1L, "Elden Ring")

    @Test
    fun `GameDetails has no details nor notes by default`() {
        // Act
        val details = GameDetails(game)

        // Assert
        assertEquals(game, details.game)
        assertNull(details.description)
        assertTrue(details.developers.isEmpty())
        assertTrue(details.publishers.isEmpty())
        assertTrue(details.genres.isEmpty())
        assertNull(details.notes)
    }

    @Test
    fun `GameDetails holds the game and its detailed properties`() {
        // Arrange
        val description = NonBlankString("An action RPG set in the Lands Between")
        val developers = listOf(NonBlankString("FromSoftware"))
        val publishers = listOf(NonBlankString("Bandai Namco"), NonBlankString("FromSoftware"))
        val genres = listOf(NonBlankString("Action"), NonBlankString("RPG"))
        val notes = NonBlankString("Lent the disc to João")

        // Act
        val details = GameDetails(game, description, developers, publishers, genres, notes)

        // Assert
        assertEquals(game, details.game)
        assertEquals(description, details.description)
        assertEquals(developers, details.developers)
        assertEquals(publishers, details.publishers)
        assertEquals(genres, details.genres)
        assertEquals(notes, details.notes)
    }

    // Convenience factory

    @Test
    fun `of converts strings into the corresponding types`() {
        // Act
        val details = GameDetails.of(
            game = game,
            description = "An action RPG set in the Lands Between",
            developers = listOf("FromSoftware"),
            publishers = listOf("Bandai Namco", "FromSoftware"),
            genres = listOf("Action", "RPG"),
            notes = "Lent the disc to João"
        )

        // Assert
        val expected = GameDetails(
            game = game,
            description = NonBlankString("An action RPG set in the Lands Between"),
            developers = listOf(NonBlankString("FromSoftware")),
            publishers = listOf(NonBlankString("Bandai Namco"), NonBlankString("FromSoftware")),
            genres = listOf(NonBlankString("Action"), NonBlankString("RPG")),
            notes = NonBlankString("Lent the disc to João")
        )
        assertEquals(expected, details)
    }

    @Test
    fun `of leaves optional properties absent by default`() {
        assertEquals(GameDetails(game), GameDetails.of(game))
    }

    @Test
    fun `of throws IllegalArgumentException when description is blank`() {
        assertFailsWith<IllegalArgumentException> {
            GameDetails.of(game, description = "   ")
        }
    }

    @Test
    fun `of throws IllegalArgumentException when notes are blank`() {
        assertFailsWith<IllegalArgumentException> {
            GameDetails.of(game, notes = "   ")
        }
    }

    // NewGameDetails

    @Test
    fun `NewGameDetails has no details nor notes by default`() {
        assertEquals(GameDetails(game), NewGameDetails().toGameDetails(game))
    }

    @Test
    fun `NewGameDetails produces the details of the added game`() {
        // Arrange
        val newDetails = NewGameDetails(
            description = NonBlankString("An action RPG"),
            developers = listOf(NonBlankString("FromSoftware")),
            publishers = listOf(NonBlankString("Bandai Namco")),
            genres = listOf(NonBlankString("RPG")),
            notes = NonBlankString("Try a magic build")
        )

        // Act
        val details = newDetails.toGameDetails(game)

        // Assert
        val expected = GameDetails(
            game = game,
            description = newDetails.description,
            developers = newDetails.developers,
            publishers = newDetails.publishers,
            genres = newDetails.genres,
            notes = newDetails.notes
        )
        assertEquals(expected, details)
    }

    @Test
    fun `of throws IllegalArgumentException when any list element is blank`() {
        assertFailsWith<IllegalArgumentException> {
            GameDetails.of(game, developers = listOf("FromSoftware", " "))
        }
        assertFailsWith<IllegalArgumentException> {
            GameDetails.of(game, publishers = listOf(""))
        }
        assertFailsWith<IllegalArgumentException> {
            GameDetails.of(game, genres = listOf("RPG", "\t"))
        }
    }
}
