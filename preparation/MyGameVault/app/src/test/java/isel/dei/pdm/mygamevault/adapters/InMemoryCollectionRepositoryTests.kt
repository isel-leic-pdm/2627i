package isel.dei.pdm.mygamevault.adapters

import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
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
    fun `searchByName works`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val results = sut.searchByName(partialName = "Elden").first()
        
        assertEquals(1, results.size)
        assertEquals("Elden Ring", results[0].game.name())
    }

    @Test
    fun `searchByName supports pagination`() = runTest {
        val sut = InMemoryCollectionRepository()
        for (i in 1..10) {
            val entry = CollectionEntry(testGame.copy(id = i.toLong(), name = NonBlankString("Game $i")), Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        val results = sut.searchByName(partialName = "Game", orderBy = CollectionRepository.OrderBy.ADDED_AT, skip = 5, top = 5).first()
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
        assertEquals(1L, results[4].game.id)
    }

    @Test
    fun `searchByPlatforms works`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        
        val results = sut.searchByPlatforms(platforms = setOf(Platforms.PS5)).first()
        assertEquals(1, results.size)
        
        val emptyResults = sut.searchByPlatforms(platforms = setOf(Platforms.PC)).first()
        assertEquals(0, emptyResults.size)
    }

    @Test
    fun `searchByPlatforms supports pagination`() = runTest {
        val sut = InMemoryCollectionRepository()
        for (i in 1..10) {
            val entry = CollectionEntry(testGame.copy(id = i.toLong()), Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        val results = sut.searchByPlatforms(platforms = setOf(Platforms.PS5), orderBy = CollectionRepository.OrderBy.ADDED_AT, skip = 5, top = 5).first()
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
    }

    @Test
    fun `searchByStates supports pagination`() = runTest {
        val sut = InMemoryCollectionRepository()
        for (i in 1..10) {
            val entry = CollectionEntry(
                testGame.copy(id = i.toLong()), 
                Platforms.PS5, 
                playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
                addedAt = Instant.fromEpochSeconds(i.toLong())
            )
            sut.save(entry)
        }
        
        val results = sut.searchByStates(states = setOf(PlayStatus.State.PLAYING), orderBy = CollectionRepository.OrderBy.ADDED_AT, skip = 5, top = 5).first()
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
    }

    @Test
    fun `getLatest returns results in descending order`() = runTest {
        val sut = InMemoryCollectionRepository()
        for (i in 1..25) {
            val entry = CollectionEntry(testGame.copy(id = i.toLong()), Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        val results = sut.getLatest(skip = 0, top = 10).first()
        assertEquals(10, results.size)
        // Newest first
        assertEquals(25L, results[0].game.id)
        assertEquals(24L, results[1].game.id)
    }

    @Test
    fun `getLatest supports pagination`() = runTest {
        val sut = InMemoryCollectionRepository()
        for (i in 1..10) {
            val entry = CollectionEntry(testGame.copy(id = i.toLong()), Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        // Act: Page 2 (skip 5, top 5)
        // Descending: 10, 9, 8, 7, 6, [5, 4, 3, 2, 1]
        val results = sut.getLatest(skip = 5, top = 5).first()

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
        assertEquals(1L, results[4].game.id)
    }

    @Test
    fun `getLatest respects time precision`() = runTest {
        val sut = InMemoryCollectionRepository()
        val baseTime = Instant.fromEpochSeconds(1704067200)
        val earlierTime = baseTime
        val laterTime = baseTime.plus(60.seconds)
        
        val earlierEntry = testEntry.copy(game = testGame.copy(id = 1), addedAt = earlierTime)
        val laterEntry = testEntry.copy(game = testGame.copy(id = 2), addedAt = laterTime)
        
        sut.save(earlierEntry)
        sut.save(laterEntry)
        
        val results = sut.getLatest().first()
        assertEquals(2L, results[0].game.id)
        assertEquals(1L, results[1].game.id)
    }

    @Test
    fun `getLatest ignores platform grouping`() = runTest {
        val sut = InMemoryCollectionRepository()
        val olderTime = Instant.fromEpochSeconds(1000)
        val newerTime = Instant.fromEpochSeconds(2000)
        
        val olderEntry = testEntry.copy(game = testGame.copy(id = 1), platform = Platforms.PS5, addedAt = olderTime)
        val newerEntry = testEntry.copy(game = testGame.copy(id = 2), platform = Platforms.PC, addedAt = newerTime)
        
        sut.save(olderEntry)
        sut.save(newerEntry)
        
        val results = sut.getLatest().first()
        assertEquals(2L, results[0].game.id)
        assertEquals(1L, results[1].game.id)
    }

    @Test
    fun `getActiveSession returns entry with sessionStartTime`() = runTest {
        val sut = InMemoryCollectionRepository()
        sut.save(testEntry)
        sut.startSession(testEntry.game.id, testEntry.platform.id)
        
        val activeSession = sut.getActiveSession().first()
        assertNotNull(activeSession)
        assertEquals(testEntry.game.id, activeSession?.game?.id)
        assertNotNull(activeSession?.sessionStartTime)
    }
}
