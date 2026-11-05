package isel.dei.pdm.demos.gamevault.search.platforms

import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemMode
import org.junit.Assert.assertEquals
import org.junit.Test

class PlatformSearchItemUiStateTest {

    private companion object {
        val PS5 = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Standard")
        val PS5_PRO = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Pro")
        val PS5_NO_VERSION = PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png")
    }

    @Test
    fun each_add_status_is_displayed_in_the_corresponding_list_item_mode() {
        // Arrange
        val expectedModes = mapOf(
            PlatformAddStatus.NOT_ADDED to PlatformListItemMode.ADDABLE,
            PlatformAddStatus.ADDING to PlatformListItemMode.ADDING,
            PlatformAddStatus.ADDED to PlatformListItemMode.ADDED
        )

        // Act & Assert - Fails if a new status is added without being mapped here
        assertEquals(PlatformAddStatus.entries.toSet(), expectedModes.keys)
        expectedModes.forEach { (status, mode) ->
            assertEquals(mode, PlatformSearchItemUiState(PS5, status).toListItemModel().mode)
        }
    }

    @Test
    fun list_item_model_holds_the_platform_data() {
        // Act
        val model = PlatformSearchItemUiState(PS5_PRO).toListItemModel()

        // Assert
        assertEquals(PS5_PRO.id, model.id)
        assertEquals(PS5_PRO.abbreviation, model.abbreviation)
        assertEquals(PS5_PRO.name, model.name)
        assertEquals(PS5_PRO.logoUri, model.logoUri)
        assertEquals(PS5_PRO.version, model.version)
    }

    @Test
    fun versions_of_the_same_platform_have_distinct_keys() {
        // Arrange - All share the same id, as provided by the search service
        val items = listOf(PS5, PS5_PRO, PS5_NO_VERSION).map { PlatformSearchItemUiState(it) }

        // Act
        val keys = items.map { it.key }

        // Assert - Duplicate keys crash LazyColumn
        assertEquals(items.size, keys.toSet().size)
    }

    @Test
    fun key_does_not_depend_on_the_add_status() {
        // Act
        val keys = PlatformAddStatus.entries.map { PlatformSearchItemUiState(PS5, it).key }

        // Assert - Otherwise, list items would be recreated (losing their state) as they are added
        assertEquals(1, keys.toSet().size)
    }
}
