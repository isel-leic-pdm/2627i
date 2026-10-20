package isel.dei.pdm.demos.gamevault.core.domain.search

import isel.dei.pdm.demos.gamevault.core.domain.NonBlankString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI
import java.time.LocalDate
import kotlin.test.assertFailsWith

class GameTests {

    @Test
    fun `primary constructor creates Game instance with valid properties`() {
        // Arrange
        val expectedId = 100L
        val expectedName = NonBlankString("Elden Ring")
        val expectedReleaseDate = LocalDate.of(2022, 2, 25)
        val expectedCoverUri = URI.create("https://example.com/cover.jpg")
        val expectedThumbnailUri = URI.create("https://example.com/thumb.jpg")

        // Act
        val sut = Game(
            id = expectedId,
            name = expectedName,
            releaseDate = expectedReleaseDate,
            coverUri = expectedCoverUri,
            thumbnailUri = expectedThumbnailUri
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedName, sut.name)
        assertEquals(expectedReleaseDate, sut.releaseDate)
        assertEquals(expectedCoverUri, sut.coverUri)
        assertEquals(expectedThumbnailUri, sut.thumbnailUri)
    }

    @Test
    fun `secondary constructor creates Game instance with valid string inputs`() {
        // Arrange
        val expectedId = 100L
        val expectedName = "Elden Ring"
        val expectedReleaseDate = LocalDate.of(2022, 2, 25)
        val expectedCoverUriString = "https://example.com/cover.jpg"
        val expectedThumbnailUriString = "https://example.com/thumb.jpg"

        // Act
        val sut = Game(
            id = expectedId,
            name = expectedName,
            releaseDate = expectedReleaseDate,
            coverUri = expectedCoverUriString,
            thumbnailUri = expectedThumbnailUriString
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedName, sut.name.value)
        assertEquals(expectedReleaseDate, sut.releaseDate)
        assertEquals(URI.create(expectedCoverUriString), sut.coverUri)
        assertEquals(URI.create(expectedThumbnailUriString), sut.thumbnailUri)
    }

    @Test
    fun `secondary constructor creates Game instance with default optional parameters`() {
        // Arrange
        val expectedId = 200L
        val expectedName = "Hades"

        // Act
        val sut = Game(
            id = expectedId,
            name = expectedName
        )

        // Assert
        assertEquals(expectedId, sut.id)
        assertEquals(expectedName, sut.name.value)
        assertNull(sut.releaseDate)
        assertNull(sut.coverUri)
        assertNull(sut.thumbnailUri)
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            Game(
                id = 1L,
                name = "   "
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when coverUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            Game(
                id = 1L,
                name = "Elden Ring",
                coverUri = "http://example.com/cover image.jpg"
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when thumbnailUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            Game(
                id = 1L,
                name = "Elden Ring",
                thumbnailUri = "http://example.com/thumb image.jpg"
            )
        }
    }

    @Test
    fun `two Game instances with same properties are equal`() {
        // Arrange
        val game1 = Game(id = 1L, name = "Elden Ring")
        val game2 = Game(id = 1L, name = "Elden Ring")

        // Act & Assert
        assertEquals(game1, game2)
        assertEquals(game1.hashCode(), game2.hashCode())
    }

    @Test
    fun `Game Category enum contains expected values`() {
        // Arrange
        val categories = Game.Category.entries

        // Act & Assert
        assertEquals(5, categories.size)
        assertTrue(categories.contains(Game.Category.MAIN_GAME))
        assertTrue(categories.contains(Game.Category.DLC))
        assertTrue(categories.contains(Game.Category.BUNDLE))
        assertTrue(categories.contains(Game.Category.REMAKE))
        assertTrue(categories.contains(Game.Category.REMASTER))
    }

    @Test
    fun `GameDetails holds game and detailed properties`() {
        // Arrange
        val game = Game(id = 1L, name = "Elden Ring")
        val expectedDescription = "An action RPG game"
        val expectedDevelopers = listOf("FromSoftware")
        val expectedPublishers = listOf("Bandai Namco Entertainment")
        val expectedGenres = listOf("Action", "RPG")

        // Act
        val details = GameDetails(
            game = game,
            description = expectedDescription,
            developers = expectedDevelopers,
            publishers = expectedPublishers,
            genres = expectedGenres
        )

        // Assert
        assertEquals(game, details.game)
        assertEquals(expectedDescription, details.description)
        assertEquals(expectedDevelopers, details.developers)
        assertEquals(expectedPublishers, details.publishers)
        assertEquals(expectedGenres, details.genres)
    }
}
