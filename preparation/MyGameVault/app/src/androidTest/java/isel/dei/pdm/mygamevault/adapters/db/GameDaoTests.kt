package isel.dei.pdm.mygamevault.adapters.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.adapters.InMemoryRoomDBRule
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTests {

    @get:Rule
    internal val dbRule = InMemoryRoomDBRule()

    @Test
    fun searchLatest_returns_at_most_top_items_ordered_by_addedAt_descending() = runTest {
        // Arrange: Insert 25 entries with increasing addedAt dates
        val platform = PlatformEntity(1, "PS5", "PlayStation 5", null)
        dbRule.dao.upsertPlatform(platform)
        
        for (i in 1..25) {
            val game = GameEntity(i.toLong(), "Game $i", null, null, null)
            val entry = CollectionEntryEntity(
                gameId = i.toLong(),
                platformId = 1,
                timeSpentSeconds = 0,
                state = PlayStatus.State.BACKLOG,
                completedRuns = 0,
                addedAt = i.toLong() // Use i as a simple timestamp
            )
            dbRule.dao.upsertGame(game)
            dbRule.dao.upsertEntry(entry)
        }

        // Act
        val results = dbRule.dao.searchLatest(skip = 0, top = 10).first { it.size >= 10 }

        // Assert
        assertEquals(10, results.size)
        // The first item should be the one with addedAt = 25 (the latest)
        assertEquals(25L, results[0].entry.addedAt)
        // The last item should be the one with addedAt = 16 (the 10th latest)
        assertEquals(16L, results[9].entry.addedAt)
    }

    @Test
    fun searchLatest_supports_pagination() = runTest {
        // Arrange: Insert 10 entries
        val platform = PlatformEntity(1, "PS5", "PlayStation 5", null)
        dbRule.dao.upsertPlatform(platform)
        for (i in 1..10) {
            val game = GameEntity(i.toLong(), "Game $i", null, null, null)
            val entry = CollectionEntryEntity(i.toLong(), 1, 0, PlayStatus.State.BACKLOG, 0, i.toLong())
            dbRule.dao.upsertGame(game)
            dbRule.dao.upsertEntry(entry)
        }

        // Act: Page 2 (skip 5, top 5)
        // Descending: 10, 9, 8, 7, 6, [5, 4, 3, 2, 1]
        val results = dbRule.dao.searchLatest(skip = 5, top = 5).first { it.size == 5 }

        // Assert
        assertEquals(5, results.size)
        assertEquals(5L, results[0].entry.addedAt)
        assertEquals(1L, results[4].entry.addedAt)
    }

    @Test
    fun searchByName_filters_and_paginates_correctly() = runTest {
        // Arrange: Insert entries with different names
        val platform = PlatformEntity(1, "PS5", "PlayStation 5", null)
        dbRule.dao.upsertPlatform(platform)
        val gameNames = listOf("Elden Ring", "Hades", "Hollow Knight", "Hades II")
        for (i in gameNames.indices) {
            val id = (i + 1).toLong()
            val game = GameEntity(id, gameNames[i], null, null, null)
            val entry = CollectionEntryEntity(id, 1, 0, PlayStatus.State.BACKLOG, 0, id)
            dbRule.dao.upsertGame(game)
            dbRule.dao.upsertEntry(entry)
        }

        // Act: Search for "Hades"
        val results = dbRule.dao.searchByName(partialName = "Hades", skip = 0, top = 10).first { it.isNotEmpty() }

        // Assert
        assertEquals(2, results.size)
        assertTrue(results.any { it.game.name == "Hades" })
        assertTrue(results.any { it.game.name == "Hades II" })
    }

    @Test
    fun searchByPlatforms_filters_correctly() = runTest {
        // Arrange: Insert entries on different platforms
        val ps5 = PlatformEntity(1, "PS5", "PlayStation 5", null)
        val pc = PlatformEntity(2, "PC", "PC", null)
        dbRule.dao.upsertPlatform(ps5)
        dbRule.dao.upsertPlatform(pc)
        
        val game1 = GameEntity(1, "Game 1", null, null, null)
        val entry1 = CollectionEntryEntity(1, 1, 0, PlayStatus.State.BACKLOG, 0, 1)
        dbRule.dao.upsertGame(game1)
        dbRule.dao.upsertEntry(entry1)

        val game2 = GameEntity(2, "Game 2", null, null, null)
        val entry2 = CollectionEntryEntity(2, 2, 0, PlayStatus.State.BACKLOG, 0, 2)
        dbRule.dao.upsertGame(game2)
        dbRule.dao.upsertEntry(entry2)

        // Act: Search for PS5 (id=1)
        val results = dbRule.dao.searchByPlatforms(platformIds = setOf(1), skip = 0, top = 10).first { it.isNotEmpty() }

        // Assert
        assertEquals(1, results.size)
        assertEquals(1L, results[0].entry.gameId)
        assertEquals(1L, results[0].entry.platformId)
    }

    @Test
    fun searchByStates_filters_correctly() = runTest {
        // Arrange: Insert entries with different states
        val platform = PlatformEntity(1, "PS5", "PlayStation 5", null)
        dbRule.dao.upsertPlatform(platform)
        
        val game1 = GameEntity(1, "Playing Game", null, null, null)
        val entry1 = CollectionEntryEntity(1, 1, 0, PlayStatus.State.PLAYING, 0, 1)
        dbRule.dao.upsertGame(game1)
        dbRule.dao.upsertEntry(entry1)

        val game2 = GameEntity(2, "Backlog Game", null, null, null)
        val entry2 = CollectionEntryEntity(2, 1, 0, PlayStatus.State.BACKLOG, 0, 2)
        dbRule.dao.upsertGame(game2)
        dbRule.dao.upsertEntry(entry2)

        // Act: Search for PLAYING
        val results = dbRule.dao.searchByStates(states = setOf(PlayStatus.State.PLAYING), skip = 0, top = 10).first { it.isNotEmpty() }

        // Assert
        assertEquals(1, results.size)
        assertEquals(PlayStatus.State.PLAYING, results[0].entry.state)
    }
}
