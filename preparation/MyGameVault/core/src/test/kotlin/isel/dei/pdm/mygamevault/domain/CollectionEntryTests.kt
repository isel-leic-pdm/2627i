package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CollectionEntryTests {

    private val sampleGame = Game(
        id = 1,
        name = "Test Game",
        releaseDate = LocalDate.now(),
        coverUri = "https://example.com/cover.jpg",
        thumbnailUri = null
    )

    @Test
    fun `collection entry holds its properties`() {
        val status = PlayStatus(state = PlayStatus.State.FINISHED)
        val addedDate = LocalDate.of(2024, 1, 1)
        val entry = CollectionEntry(
            game = sampleGame,
            platform = Platforms.PS5,
            playStatus = status,
            addedAt = addedDate
        )

        assertEquals(sampleGame, entry.game)
        assertEquals(Platforms.PS5, entry.platform)
        assertEquals(status, entry.playStatus)
        assertEquals(addedDate, entry.addedAt)
    }

    @Test
    fun `collection entry has current date by default`() {
        val entry = CollectionEntry(sampleGame, Platforms.PS5)
        assertEquals(LocalDate.now(), entry.addedAt)
    }
}
