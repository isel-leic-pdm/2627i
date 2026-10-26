package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameInfoTests {

    @Test
    fun `primary constructor creates Game instance with valid properties`() {
        // Arrange
        val expectedId = 100L
        val expectedName = NonBlankString("Elden Ring")
        val expectedReleaseDate = LocalDate.of(2022, 2, 25)
        val expectedCoverUri = URI.create("https://example.com/cover.jpg")
        val expectedThumbnailUri = URI.create("https://example.com/thumb.jpg")

        // Act
        val sut = GameInfo(
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
        val sut = GameInfo(
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
        val sut = GameInfo(
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
            GameInfo(
                id = 1L,
                name = "   "
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when coverUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            GameInfo(
                id = 1L,
                name = "Elden Ring",
                coverUri = "http://example.com/cover image.jpg"
            )
        }
    }

    @Test
    fun `secondary constructor throws IllegalArgumentException when thumbnailUri is an invalid URI`() {
        assertFailsWith<IllegalArgumentException> {
            GameInfo(
                id = 1L,
                name = "Elden Ring",
                thumbnailUri = "http://example.com/thumb image.jpg"
            )
        }
    }

    @Test
    fun `two Game instances with same properties are equal`() {
        // Arrange
        val game1 = GameInfo(id = 1L, name = "Elden Ring")
        val game2 = GameInfo(id = 1L, name = "Elden Ring")

        // Act & Assert
        assertEquals(game1, game2)
        assertEquals(game1.hashCode(), game2.hashCode())
    }

    @Test
    fun `Game Category enum contains expected values`() {
        // Arrange
        val categories = GameInfo.Category.entries

        // Act & Assert
        assertEquals(5, categories.size)
        assertTrue(categories.contains(GameInfo.Category.MAIN_GAME))
        assertTrue(categories.contains(GameInfo.Category.MAIN_GAME))
        assertTrue(categories.contains(GameInfo.Category.DLC))
        assertTrue(categories.contains(GameInfo.Category.BUNDLE))
        assertTrue(categories.contains(GameInfo.Category.REMAKE))
        assertTrue(categories.contains(GameInfo.Category.REMASTER))
    }

    @Test
    fun `GameDetails holds game and detailed properties`() {
        // Arrange
        val game = GameInfo(id = 1L, name = "Elden Ring")
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
