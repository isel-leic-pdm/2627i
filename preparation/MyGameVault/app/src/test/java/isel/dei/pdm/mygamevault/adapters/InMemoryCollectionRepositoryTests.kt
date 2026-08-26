package isel.dei.pdm.mygamevault.adapters

import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
        assertEquals("Elden Ring", results[0].game.name())
    }

    @Test
    fun `search by platform works`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val results = sut.search(platforms = setOf(Platforms.PS5)).first()
        assertEquals(1, results.size)
        
        val emptyResults = sut.search(platforms = setOf(Platforms.PC)).first()
        assertEquals(0, emptyResults.size)
    }

    @Test
    fun `search by multiple states works`() = runTest {
        val sut = InMemoryCollectionRepository()
        val finishedEntry = testEntry.copy(
            game = testGame.copy(id = 2),
            playStatus = PlayStatus(state = PlayStatus.State.FINISHED)
        )
        val platinumEntry = testEntry.copy(
            game = testGame.copy(id = 3),
            playStatus = PlayStatus(state = PlayStatus.State.PLATINUM)
        )
        val backlogEntry = testEntry.copy(
            game = testGame.copy(id = 4),
            playStatus = PlayStatus(state = PlayStatus.State.BACKLOG)
        )
        
        sut.save(finishedEntry)
        sut.save(platinumEntry)
        sut.save(backlogEntry)
        
        val results = sut.search(states = setOf(PlayStatus.State.FINISHED, PlayStatus.State.PLATINUM)).first()
        
        assertEquals(2, results.size)
        assertTrue(results.any { it.game.id == 2L })
        assertTrue(results.any { it.game.id == 3L })
    }

    @Test
    fun `search returns results ordered primarily by platform and state`() = runTest {
        val sut = InMemoryCollectionRepository()
        
        val pcPlaying = testEntry.copy(game = testGame.copy(id = 10, name = isel.dei.pdm.mygamevault.domain.NonBlankString("PC Game")), platform = Platforms.PC, playStatus = PlayStatus(state = PlayStatus.State.PLAYING))
        val ps5Finished = testEntry.copy(game = testGame.copy(id = 20, name = isel.dei.pdm.mygamevault.domain.NonBlankString("PS5 Finished")), platform = Platforms.PS5, playStatus = PlayStatus(state = PlayStatus.State.FINISHED))
        val ps5Playing = testEntry.copy(game = testGame.copy(id = 30, name = isel.dei.pdm.mygamevault.domain.NonBlankString("PS5 Playing")), platform = Platforms.PS5, playStatus = PlayStatus(state = PlayStatus.State.PLAYING))

        // Save in arbitrary order
        sut.save(ps5Finished)
        sut.save(pcPlaying)
        sut.save(ps5Playing)

        val results = sut.search().first()

        assertEquals(3, results.size)
        // Primary: Platform name (PC < PS5)
        assertEquals(Platforms.PC, results[0].platform)
        // Secondary: State ordinal (PLAYING < FINISHED)
        assertEquals(Platforms.PS5, results[1].platform)
        assertEquals(PlayStatus.State.PLAYING, results[1].playStatus.state)
        assertEquals(Platforms.PS5, results[2].platform)
        assertEquals(PlayStatus.State.FINISHED, results[2].playStatus.state)
    }

    @Test
    fun `search returns results ordered by name within groups`() = runTest {
        val sut = InMemoryCollectionRepository()
        
        val gameB = testEntry.copy(game = testGame.copy(id = 1, name = isel.dei.pdm.mygamevault.domain.NonBlankString("B Game")))
        val gameA = testEntry.copy(game = testGame.copy(id = 2, name = isel.dei.pdm.mygamevault.domain.NonBlankString("A Game")))

        sut.save(gameB)
        sut.save(gameA)

        val results = sut.search(orderBy = CollectionRepository.OrderBy.NAME).first()

        assertEquals(2, results.size)
        assertEquals("A Game", results[0].game.name())
        assertEquals("B Game", results[1].game.name())
    }
}
