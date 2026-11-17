package isel.dei.pdm.demos.gamevault.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectionSectionsTest {

    private var nextId = 1L

    private fun item(name: String, isSessionActive: Boolean = false) = CollectionItem(
        gameId = nextId++,
        name = NonBlankString(name),
        releaseYear = null,
        coverUri = null,
        thumbnailUri = null,
        platformAbbreviations = listOf(NonBlankString("PS5")),
        state = if (isSessionActive) PlayStatus.State.PLAYING else PlayStatus.State.BACKLOG,
        timeSpent = PlayTime(),
        isFinished = false,
        hasPlatinum = false,
        isSessionActive = isSessionActive
    )

    private fun List<CollectionSection>.initials() = map { it.initial }

    // Sections

    @Test
    fun no_items_have_no_sections() {
        assertEquals(emptyList<CollectionSection>(), emptyList<CollectionItem>().toSections())
    }

    @Test
    fun items_are_grouped_by_the_initial_they_are_listed_under() {
        // Arrange
        val celeste = item("Celeste")
        val control = item("control")
        val hades = item("Hades")

        // Act
        val sections = listOf(celeste, control, hades).toSections()

        // Assert
        assertEquals(
            listOf(
                CollectionSection('C', listOf(celeste, control)),
                CollectionSection('H', listOf(hades))
            ),
            sections
        )
    }

    @Test
    fun sections_are_sorted_alphabetically_with_the_other_initial_last() {
        // Arrange - In the order of a repository sorting by name (digits first, accents last)
        val items = listOf("1080° Snowboarding", "Celeste", "Hades", "Zelda", "Ōkami").map { item(it) }

        // Act
        val sections = items.toSections()

        // Assert
        assertEquals(listOf('C', 'H', 'Z', CollectionQuery.OTHER_INITIAL), sections.initials())
        assertEquals(
            listOf("1080° Snowboarding", "Ōkami"),
            sections.last().items.map { it.name.value }
        )
    }

    @Test
    fun items_keep_their_relative_order_within_sections() {
        // Arrange - Not sorted by name, to show that grouping does not sort
        val items = listOf(item("Hollow Knight"), item("Hades"), item("Halo"))

        // Act
        val sections = items.toSections()

        // Assert
        assertEquals(listOf(items), sections.map { it.items })
    }

    @Test
    fun the_game_being_played_is_listed_under_its_initial() {
        // Arrange - The repository lists the game being played first
        val zelda = item("Zelda", isSessionActive = true)
        val celeste = item("Celeste")

        // Act
        val sections = listOf(zelda, celeste).toSections()

        // Assert
        assertEquals(listOf('C', 'Z'), sections.initials())
    }

    // Initials index visibility

    @Test
    fun index_is_shown_for_long_listings_with_enough_initials() {
        assertTrue(shouldShowInitialsIndex(totalItems = 30, visibleItems = 10, initials = 5, isShown = false))
    }

    @Test
    fun index_is_not_shown_for_listings_shorter_than_the_threshold() {
        assertFalse(shouldShowInitialsIndex(totalItems = 29, visibleItems = 10, initials = 26, isShown = false))
    }

    @Test
    fun index_is_not_shown_for_listings_with_few_initials() {
        assertFalse(shouldShowInitialsIndex(totalItems = 300, visibleItems = 10, initials = 4, isShown = false))
        assertFalse(shouldShowInitialsIndex(totalItems = 300, visibleItems = 10, initials = 4, isShown = true))
    }

    @Test
    fun shown_index_is_kept_until_the_listing_is_shorter_than_the_lower_threshold() {
        assertTrue(shouldShowInitialsIndex(totalItems = 25, visibleItems = 10, initials = 5, isShown = true))
        assertTrue(shouldShowInitialsIndex(totalItems = 20, visibleItems = 10, initials = 5, isShown = true))
        assertFalse(shouldShowInitialsIndex(totalItems = 19, visibleItems = 10, initials = 5, isShown = true))
    }

    @Test
    fun index_keeps_its_visibility_while_the_listing_is_not_laid_out() {
        assertTrue(shouldShowInitialsIndex(totalItems = 300, visibleItems = 0, initials = 26, isShown = true))
        assertFalse(shouldShowInitialsIndex(totalItems = 300, visibleItems = 0, initials = 26, isShown = false))
    }
}
