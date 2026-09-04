package isel.dei.pdm.mygamevault.adapters.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import isel.dei.pdm.mygamevault.adapters.InMemoryRoomDBRule
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameDaoTests {

    @get:Rule
    internal val dbRule = InMemoryRoomDBRule()

    @Test
    fun searchLatest_returns_at_most_limit_items_ordered_by_addedAt_descending() = runTest {
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
        val results = dbRule.dao.searchLatest(limit = 10).first { it.size >= 10 }

        // Assert
        assertEquals(10, results.size)
        // The first item should be the one with addedAt = 25 (the latest)
        assertEquals(25L, results[0].entry.addedAt)
        // The last item should be the one with addedAt = 16 (the 10th latest)
        assertEquals(16L, results[9].entry.addedAt)
    }
}
