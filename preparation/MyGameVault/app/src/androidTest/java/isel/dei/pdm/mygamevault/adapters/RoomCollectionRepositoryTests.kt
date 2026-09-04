package isel.dei.pdm.mygamevault.adapters

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteDatabaseLockedException
import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.adapters.db.CollectionEntryEntity
import isel.dei.pdm.mygamevault.adapters.db.CollectionEntryWithDetails
import isel.dei.pdm.mygamevault.adapters.db.GameDao
import isel.dei.pdm.mygamevault.adapters.db.GameEntity
import isel.dei.pdm.mygamevault.adapters.db.PlatformEntity
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.toPlayTime
import isel.dei.pdm.mygamevault.ports.RecoverablePersistenceException
import isel.dei.pdm.mygamevault.ports.UnrecoverablePersistenceException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.hours
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomCollectionRepositoryTests {

    @get:Rule
    internal val dbRule = InMemoryRoomDBRule()

    private val sut: RoomCollectionRepository by lazy {
        RoomCollectionRepository(dbRule.dao)
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
        // Arrange
        val fakeDao = FakeGameDao(SQLiteDatabaseLockedException("Locked"))
        val repo = RoomCollectionRepository(fakeDao)

        // Act & Assert
        assertThrows(RecoverablePersistenceException::class.java) {
            runTest { repo.save(testEntry) }
        }
    }

    @Test
    fun save_maps_SQLiteDatabaseCorruptException_to_UnrecoverablePersistenceException() = runTest {
        // Arrange
        val fakeDao = FakeGameDao(SQLiteDatabaseCorruptException("Corrupt"))
        val repo = RoomCollectionRepository(fakeDao)

        // Act & Assert
        assertThrows(UnrecoverablePersistenceException::class.java) {
            runTest { repo.save(testEntry) }
        }
    }

    private class FakeGameDao(val errorToThrow: Exception? = null) : GameDao {
        override fun searchByName(partialName: String, limit: Int): Flow<List<CollectionEntryWithDetails>> = throw NotImplementedError()
        override fun searchByPlatforms(platformIds: Set<Long>, limit: Int): Flow<List<CollectionEntryWithDetails>> = throw NotImplementedError()
        override fun searchByStates(states: Set<PlayStatus.State>, limit: Int): Flow<List<CollectionEntryWithDetails>> = throw NotImplementedError()
        override fun searchLatest(limit: Int): Flow<List<CollectionEntryWithDetails>> = throw NotImplementedError()
        override suspend fun count(): Int = throw NotImplementedError()
        override suspend fun countGames(): Int = throw NotImplementedError()
        override suspend fun upsertGame(game: GameEntity) = throw NotImplementedError()
        override suspend fun upsertPlatform(platform: PlatformEntity) = throw NotImplementedError()
        override suspend fun upsertEntry(entry: CollectionEntryEntity) = throw NotImplementedError()
        override suspend fun deleteEntry(gameId: Long, platformId: Long) = throw NotImplementedError()
        override suspend fun getEntry(gameId: Long, platformId: Long): CollectionEntryWithDetails? = throw NotImplementedError()
        override suspend fun saveEntry(game: GameEntity, platform: PlatformEntity, entry: CollectionEntryEntity) {
            if (errorToThrow != null) throw errorToThrow
        }
    }
}
