package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class CollectionItemTests {

    private val game = Game(
        id = 7L,
        name = "Elden Ring",
        releaseDate = LocalDate(2022, 2, 25),
        coverUri = "content://vault/covers/7.jpg",
        thumbnailUri = "content://vault/thumbnails/7.jpg"
    )
    private val platform = Platform(1L, "PS5", "PlayStation 5", logoUri = "https://example.com/ps5.png")
    private val march = LocalDate(2026, 3, 12)

    // CollectionEntry to CollectionItem

    @Test
    fun `toItem keeps the information needed in listings`() {
        // Arrange
        val entry = CollectionEntry(
            game = game,
            platforms = setOf(platform),
            playStatus = PlayStatus(PlayTime(42, 10), State.PLAYING, listOf(Accomplishment(march)))
        )

        // Act
        val item = entry.toItem()

        // Assert
        val expected = CollectionItem(
            gameId = 7L,
            name = NonBlankString("Elden Ring"),
            releaseYear = Year(2022),
            coverUri = Uri("content://vault/covers/7.jpg"),
            thumbnailUri = Uri("content://vault/thumbnails/7.jpg"),
            platformAbbreviations = listOf(NonBlankString("PS5")),
            state = State.PLAYING,
            timeSpent = PlayTime(42, 10),
            isFinished = true,
            hasPlatinum = false,
            isSessionActive = false
        )
        assertEquals(expected, item)
    }

    @Test
    fun `toItem signals the platinum and the active session`() {
        // Arrange
        val entry = CollectionEntry(
            game = game,
            platforms = setOf(platform),
            playStatus = PlayStatus(PlayTime(80, 0), State.PLAYING, platinum = Accomplishment(march)),
            sessionStartTime = Instant.parse("2026-11-08T20:00:00Z")
        )

        // Act
        val item = entry.toItem()

        // Assert
        assertFalse(item.isFinished)
        assertTrue(item.hasPlatinum)
        assertTrue(item.isSessionActive)
    }

    @Test
    fun `toItem of a game without release date nor images has them absent`() {
        val item = CollectionEntry(Game(1L, "Hades II"), setOf(platform)).toItem()

        assertNull(item.releaseYear)
        assertNull(item.coverUri)
        assertNull(item.thumbnailUri)
    }

    @Test
    fun `toItem lists the abbreviations of the platforms in the order they were added`() {
        // Arrange
        val switch = Platform(2L, "Switch", "Nintendo Switch")
        val entry = CollectionEntry(game, setOf(switch)).addPlatform(platform)

        // Act & Assert
        assertEquals(listOf(NonBlankString("Switch"), NonBlankString("PS5")), entry.toItem().platformAbbreviations)
    }

    // CollectionQuery

    @Test
    fun `CollectionQuery applies no criteria by default`() {
        // Act
        val query = CollectionQuery()

        // Assert
        assertNull(query.state)
        assertFalse(query.platinumOnly)
        assertNull(query.platformId)
        assertNull(query.nameContains)
        assertNull(query.initial)
    }

    @Test
    fun `CollectionQuery accepts upper case letters and the other initial`() {
        listOf('A', 'M', 'Z', CollectionQuery.OTHER_INITIAL).forEach { initial ->
            assertEquals(initial, CollectionQuery(initial = initial).initial)
        }
    }

    @Test
    fun `CollectionQuery throws IllegalArgumentException when the initial is invalid`() {
        listOf('a', '1', '?', 'Ō').forEach { initial ->
            assertFailsWith<IllegalArgumentException> {
                CollectionQuery(initial = initial)
            }
        }
    }
}
