package isel.dei.pdm.demos.gamevault.search.games

import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameSearchFiltersTest {

    private companion object {
        val PS5 = Platform(1L, "PS5", "PlayStation 5")
        val SWITCH = Platform(2L, "Switch", "Nintendo Switch")
    }

    @Test
    fun all_platforms_and_any_category_are_selected_by_default() {
        // Act
        val filters = GameSearchFilters(platforms = listOf(PS5, SWITCH))

        // Assert
        assertNull(filters.selectedPlatform)
        assertNull(filters.category)
    }

    @Test
    fun one_of_the_platforms_can_be_selected() {
        // Act
        val filters = GameSearchFilters(platforms = listOf(PS5, SWITCH), selectedPlatform = SWITCH)

        // Assert
        assertEquals(SWITCH, filters.selectedPlatform)
    }

    @Test(expected = IllegalArgumentException::class)
    fun selected_platform_must_be_one_of_the_platforms() {
        GameSearchFilters(platforms = listOf(PS5), selectedPlatform = SWITCH)
    }

    @Test(expected = IllegalArgumentException::class)
    fun platforms_cannot_include_versions_of_the_same_platform() {
        // Games are oblivious of versions, so games are searched for each platform once
        GameSearchFilters(platforms = listOf(PS5, Platform(3L, "PS5", "PlayStation 5", version = "Pro")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun there_must_be_at_least_one_platform() {
        // An empty collection is represented by SearchForGamesScreenState.NoPlatforms instead
        GameSearchFilters(platforms = emptyList())
    }
}
