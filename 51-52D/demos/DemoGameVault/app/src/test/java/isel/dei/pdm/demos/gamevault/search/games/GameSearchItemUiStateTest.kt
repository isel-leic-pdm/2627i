package isel.dei.pdm.demos.gamevault.search.games

import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class GameSearchItemUiStateTest {

    private companion object {
        val ELDEN_RING = GameInfo(1L, "Elden Ring")
        val HADES = GameInfo(2L, "Hades")
        val PS5 = Platform(1L, "PS5", "PlayStation 5")
        val PC = Platform(2L, "PC", "PC (Microsoft Windows)")
    }

    @Test
    fun items_of_the_same_game_on_different_platforms_have_distinct_keys() {
        // Arrange
        val items = listOf(PS5, PC).map { GameSearchItemUiState(ELDEN_RING, it) }

        // Act
        val keys = items.map { it.key }

        // Assert - Duplicate keys crash LazyColumn
        assertEquals(items.size, keys.toSet().size)
    }

    @Test
    fun items_of_different_games_on_the_same_platform_have_distinct_keys() {
        // Arrange
        val items = listOf(ELDEN_RING, HADES).map { GameSearchItemUiState(it, PS5) }

        // Act
        val keys = items.map { it.key }

        // Assert
        assertEquals(items.size, keys.toSet().size)
    }

    @Test
    fun key_does_not_depend_on_the_add_status() {
        // Act
        val keys = AddStatus.entries.map { GameSearchItemUiState(ELDEN_RING, PS5, it).key }

        // Assert - Otherwise, list items would be recreated (losing their state) as they are added
        assertEquals(1, keys.toSet().size)
    }
}
