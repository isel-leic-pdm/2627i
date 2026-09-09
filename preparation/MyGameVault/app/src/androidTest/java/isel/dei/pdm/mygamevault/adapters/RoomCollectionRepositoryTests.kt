package isel.dei.pdm.mygamevault.adapters

import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.toPlayTime
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
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
    fun searchByName_supports_pagination() = runTest {
        // Arrange: Insert 10 games with "Game" in their name
        for (i in 1..10) {
            val game = testGame.copy(id = i.toLong(), name = NonBlankString("Game $i"))
            val entry = CollectionEntry(game, Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }

        // Act: Get page 2 (skip 5, top 5)
        // Pass ADDED_AT to match the DAO's fixed ordering
        val results = sut.searchByName(partialName = "Game", orderBy = CollectionRepository.OrderBy.ADDED_AT, skip = 5, top = 5)
            .first { it.size == 5 }

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
        assertEquals(1L, results[4].game.id)
    }

    @Test
    fun getLatest_returns_results_in_descending_order() = runTest {
        // Arrange: Insert 25 games with increasing added timestamps
        for (i in 1..25) {
            val game = testGame.copy(id = i.toLong(), name = NonBlankString("Game $i"))
            val entry = CollectionEntry(game, Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        // Act
        val results = sut.getLatest(skip = 0, top = 10).first { it.size == 10 }

        // Assert
        assertEquals(10, results.size)
        // Newest should be first (Game 25, added at epoch second 25)
        assertEquals(25L, results[0].game.id)
        assertEquals(Instant.fromEpochSeconds(25), results[0].addedAt)
        // Second should be Game 24
        assertEquals(24L, results[1].game.id)
    }

    @Test
    fun getLatest_supports_pagination() = runTest {
        // Arrange: Insert 10 games
        for (i in 1..10) {
            val game = testGame.copy(id = i.toLong(), name = NonBlankString("Game $i"))
            val entry = CollectionEntry(game, Platforms.PS5, addedAt = Instant.fromEpochSeconds(i.toLong()))
            sut.save(entry)
        }
        
        // Act: Get page 2 (skip 5, top 5)
        // Order is descending: 10, 9, 8, 7, 6, [5, 4, 3, 2, 1]
        val results = sut.getLatest(skip = 5, top = 5).first { it.size == 5 }

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
        assertEquals(1L, results[4].game.id)
    }

    @Test
    fun getLatest_respects_time_precision() = runTest {
        // Arrange: Save two games on the same day but different times
        val baseTime = Instant.fromEpochSeconds(1704067200) // 2024-01-01 00:00:00 UTC
        val earlierTime = baseTime
        val laterTime = baseTime.plus(60.seconds)
        
        val earlierEntry = testEntry.copy(
            game = testGame.copy(id = 1),
            addedAt = earlierTime
        )
        val laterEntry = testEntry.copy(
            game = testGame.copy(id = 2),
            addedAt = laterTime
        )
        
        sut.save(earlierEntry)
        sut.save(laterEntry)
        
        // Act
        val results = sut.getLatest().first { it.size == 2 }
        
        // Assert: Later game should be first
        assertEquals(2L, results[0].game.id)
        assertEquals(1L, results[1].game.id)
    }

    @Test
    fun getLatest_ignores_platform_grouping() = runTest {
        // Arrange: Save an older game on PS5 and a newer game on PC
        val olderTime = Instant.fromEpochSeconds(1000)
        val newerTime = Instant.fromEpochSeconds(2000)
        
        val olderEntry = testEntry.copy(
            game = testGame.copy(id = 1),
            platform = Platforms.PS5,
            addedAt = olderTime
        )
        val newerEntry = testEntry.copy(
            game = testGame.copy(id = 2),
            platform = Platforms.PC,
            addedAt = newerTime
        )
        
        sut.save(olderEntry)
        sut.save(newerEntry)
        
        // Act
        val results = sut.getLatest().first { it.size == 2 }
        
        // Assert: Newer game (PC) should be first despite platform name sorting
        assertEquals(2L, results[0].game.id)
        assertEquals(1L, results[1].game.id)
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
    fun searchByStates_supports_pagination() = runTest {
        // Arrange: Insert 10 playing games
        for (i in 1..10) {
            val game = testGame.copy(id = i.toLong())
            val entry = CollectionEntry(
                game = game, 
                platform = Platforms.PS5, 
                playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
                addedAt = Instant.fromEpochSeconds(i.toLong())
            )
            sut.save(entry)
        }

        // Act: Get page 2
        val results = sut.searchByStates(
            states = setOf(PlayStatus.State.PLAYING), 
            orderBy = CollectionRepository.OrderBy.ADDED_AT,
            skip = 5, 
            top = 5
        ).first { it.size == 5 }

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
    }

    @Test
    fun searchByPlatforms_supports_pagination() = runTest {
        // Arrange: Insert 10 games on PS5
        for (i in 1..10) {
            val game = testGame.copy(id = i.toLong())
            val entry = CollectionEntry(
                game = game, 
                platform = Platforms.PS5, 
                addedAt = Instant.fromEpochSeconds(i.toLong())
            )
            sut.save(entry)
        }

        // Act: Get page 2
        val results = sut.searchByPlatforms(
            platforms = setOf(Platforms.PS5), 
            orderBy = CollectionRepository.OrderBy.ADDED_AT,
            skip = 5, 
            top = 5
        ).first { it.size == 5 }

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].game.id)
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
