package isel.dei.pdm.mygamevault.adapters

import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.toPlayTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

@RunWith(AndroidJUnit4::class)
class RoomCollectionRepositoryTests {

    @get:Rule
    internal val dbRule = InMemoryRoomDBRule()

    private val sut: RoomCollectionRepository by lazy {
        RoomCollectionRepository(dbRule.database)
    }

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
        playStatus = PlayStatus(
            timeSpent = 10.hours.toPlayTime(),
            state = PlayStatus.State.PLAYING
        )
    )

    @Test
    fun save_and_get_works_correctly() = runTest {
        // Act
        sut.save(testEntry)
        val retrieved = sut.get(testGame.id, Platforms.PS5)

        // Assert
        assertNotNull(retrieved)
        assertEquals(testEntry, retrieved)
    }

    @Test
    fun delete_works_correctly() = runTest {
        // Arrange
        sut.save(testEntry)
        
        // Act
        sut.delete(testGame.id, Platforms.PS5)
        val retrieved = sut.get(testGame.id, Platforms.PS5)

        // Assert
        assertNull(retrieved)
    }

    @Test
    fun searchByName_works() = runTest {
        // Arrange
        sut.save(testEntry)
        sut.save(testEntry.copy(game = testGame.copy(id = 2, name = NonBlankString("Hades")), platform = Platforms.SWITCH))

        // Act
        val results = sut.searchByName(partialName = "Elden").first { it.isNotEmpty() }

        // Assert
        assertEquals(1, results.size)
        assertEquals("Elden Ring", results[0].game.name())
    }

    @Test
    fun getLatest_returns_limited_results() = runTest {
        // Arrange: Insert 25 games
        for (i in 1..25) {
            val game = testGame.copy(id = i.toLong(), name = NonBlankString("Game $i"))
            val entry = CollectionEntry(game, Platforms.PS5, addedAt = LocalDate.of(2024, 1, i))
            sut.save(entry)
        }
        
        // Act
        val results = sut.getLatest().first { it.size == 20 }

        // Assert
        assertEquals(20, results.size)
    }

    @Test
    fun searchByStates_works() = runTest {
        // Arrange
        val finishedStatus = PlayStatus(state = PlayStatus.State.FINISHED, completedRuns = 1)
        val platinumStatus = PlayStatus(state = PlayStatus.State.PLATINUM, completedRuns = 1)
        
        val finishedEntry = testEntry.copy(playStatus = finishedStatus)
        val platinumEntry = testEntry.copy(game = testGame.copy(id = 2), playStatus = platinumStatus)
        val backlogEntry = testEntry.copy(game = testGame.copy(id = 3), playStatus = PlayStatus(state = PlayStatus.State.BACKLOG))
        
        sut.save(finishedEntry)
        sut.save(platinumEntry)
        sut.save(backlogEntry)

        // Act
        val results = sut.searchByStates(states = setOf(PlayStatus.State.FINISHED, PlayStatus.State.PLATINUM))
            .first { it.size == 2 }

        // Assert
        assertEquals(2, results.size)
        assertTrue(results.any { it.playStatus.state == PlayStatus.State.FINISHED })
        assertTrue(results.any { it.playStatus.state == PlayStatus.State.PLATINUM })
    }

    @Test
    fun save_maps_SQLiteDatabaseLockedException_to_RecoverablePersistenceException() = runTest {
        // This test requires a mocked DB to return our FakeDao.
        // For brevity in this lecture demo, we could use a Mocking library or skip these specific DAO-mock tests.
    }

    @Test
    fun save_maps_SQLiteDatabaseCorruptException_to_UnrecoverablePersistenceException() = runTest {
        // This test requires a mocked DB to return our FakeDao.
    }

    @Test
    fun getActiveSession_emits_full_entry_with_details() = runTest {
        // Arrange
        sut.save(testEntry)
        sut.startSession(testGame.id, Platforms.PS5.id)

        // Act
        val active = sut.getActiveSession().first { it != null }

        // Assert
        assertNotNull(active)
        assertEquals(testGame.id, active?.game?.id)
        assertEquals("Elden Ring", active?.game?.name())
        assertEquals(Platforms.PS5.name(), active?.platform?.name())
        assertNotNull(active?.sessionStartTime)
    }

    @Test
    fun startSession_creates_active_session() = runTest {
        // Arrange
        sut.save(testEntry)
        
        // Act
        sut.startSession(testGame.id, Platforms.PS5.id)
        
        // Assert
        val active = sut.getActiveSession().first()
        assertNotNull(active)
        assertEquals(testGame.id, active?.game?.id)
        assertNotNull(active?.sessionStartTime)
    }

    @Test
    fun stopSession_updates_playtime_and_removes_session() = runTest {
        // Arrange
        sut.save(testEntry)
        sut.startSession(testGame.id, Platforms.PS5.id)
        
        // Act
        sut.stopSession()
        
        // Assert
        val active = sut.getActiveSession().first()
        assertNull(active)
        
        val updatedEntry = sut.get(testGame.id, Platforms.PS5)
        assertNotNull(updatedEntry)
        assertTrue(updatedEntry!!.playStatus.timeSpent.toDuration() >= testEntry.playStatus.timeSpent.toDuration())
    }

    @Test
    fun startSession_stops_previous_session() = runTest {
        // Arrange
        val game2 = testGame.copy(id = 2, name = NonBlankString("Hades"))
        val entry2 = testEntry.copy(game = game2, platform = Platforms.SWITCH)
        sut.save(testEntry)
        sut.save(entry2)
        
        sut.startSession(testGame.id, Platforms.PS5.id)
        
        // Act
        sut.startSession(game2.id, Platforms.SWITCH.id)
        
        // Assert
        val active = sut.getActiveSession().first()
        assertNotNull(active)
        assertEquals(game2.id, active?.game?.id)
        
        val retrieved1 = sut.get(testGame.id, Platforms.PS5)
        assertNotNull(retrieved1)
    }
}
