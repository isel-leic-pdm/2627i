package isel.dei.pdm.mygamevault.adapters

import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class InMemoryCollectionRepositoryTests {

    private val testGame = Game(
        id = 1,
        name = "Elden Ring",
        releaseDate = LocalDate.of(2022, 2, 25),
        coverUri = "https://example.com/cover.jpg",
        thumbnailUri = null
    )

    private val testEntry = CollectionEntry(
        game = testGame,
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING)
    )

    @Test
    fun `save and get works correctly`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val retrieved = sut.get(testGame.id, Platforms.PS5)
        
        assertNotNull(retrieved)
        assertEquals(testEntry, retrieved)
    }

    @Test
    fun `delete works correctly`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        sut.delete(testGame.id, Platforms.PS5)
        
        val retrieved = sut.get(testGame.id, Platforms.PS5)
        assertNull(retrieved)
    }

    @Test
    fun `getCurrentlyPlaying emits only playing games`() = runTest {
        val sut = InMemoryCollectionRepository()
        val playingEntry = testEntry
        val backlogEntry = testEntry.copy(
            game = testGame.copy(id = 2),
            playStatus = PlayStatus(state = PlayStatus.State.BACKLOG)
        )
        
        sut.save(playingEntry)
        sut.save(backlogEntry)
        
        val currentlyPlaying = sut.getCurrentlyPlaying().first()
        
        assertEquals(1, currentlyPlaying.size)
        assertEquals(playingEntry, currentlyPlaying[0])
    }

    @Test
    fun `search by partial name works`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val results = sut.search(partialName = "Elden").first()
        
        assertEquals(1, results.size)
        assertEquals("Elden Ring", results[0].game.name.value)
    }

    @Test
    fun `search by platform works`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val results = sut.search(platform = Platforms.PS5).first()
        assertEquals(1, results.size)
        
        val emptyResults = sut.search(platform = Platforms.PC).first()
        assertEquals(0, emptyResults.size)
    }
}
