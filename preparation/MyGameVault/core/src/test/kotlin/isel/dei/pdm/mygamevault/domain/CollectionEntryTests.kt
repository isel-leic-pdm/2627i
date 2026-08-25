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
        val entry = CollectionEntry(
            game = sampleGame,
            platform = Platforms.PS5,
            playStatus = status
        )

        assertEquals(sampleGame, entry.game)
        assertEquals(Platforms.PS5, entry.platform)
        assertEquals(status, entry.playStatus)
    }
}
