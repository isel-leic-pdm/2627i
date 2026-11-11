package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo.Category
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeGameSearchServiceTest {

    private companion object {
        // The fake identifies platforms by their IGDB identifiers
        val PS5 = byId("167")
        val PS4 = byId("48")
        val PC = byId("6")
        val SWITCH = byId("130")
        val SWITCH_2 = byId("508")

        fun byId(id: String) = PlatformCriterion.ById(NonBlankString(id))

        fun byName(name: String, abbreviation: String) =
            PlatformCriterion.ByName(NonBlankString(name), NonBlankString(abbreviation))
    }

    private val sut = FakeGameSearchService()

    private suspend fun search(
        name: String,
        vararg platforms: PlatformCriterion,
        category: Category? = null
    ) = sut.searchGames(SearchString(name), platforms.toList(), category)

    private suspend fun searchNames(
        name: String,
        vararg platforms: PlatformCriterion,
        category: Category? = null
    ) = search(name, *platforms, category = category).map { it.name.value }

    // Search

    @Test
    fun `search requires at least one platform`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            sut.searchGames(SearchString("Hades"), emptyList(), null)
        }
    }

    @Test
    fun `search matches part of the name ignoring case`() = runTest {
        // Act
        val names = searchNames("zelda", SWITCH)

        // Assert
        assertEquals(
            listOf(
                "The Legend of Zelda: Breath of the Wild",
                "The Legend of Zelda: Tears of the Kingdom"
            ),
            names
        )
    }

    @Test
    fun `search only includes games released for the given platforms`() = runTest {
        // Act - Demon's Souls was released for the PS3, and remade for the PS5
        val games = search("Demon's Souls", PS5)

        // Assert
        assertEquals(1, games.size)
        assertEquals(Category.REMAKE, games.single().category)
    }

    @Test
    fun `search includes games released for any of the given platforms`() = runTest {
        // Act
        val names = searchNames("Zelda", SWITCH, SWITCH_2)

        // Assert
        assertEquals(3, names.size)
        assertTrue(names.contains("The Legend of Zelda: Tears of the Kingdom - Nintendo Switch 2 Edition"))
    }

    @Test
    fun `search lists each game once even if it matches several platforms`() = runTest {
        // Act - Including equal criteria, as for versions of the same platform
        val names = searchNames("Hades", PC, SWITCH, PS5, PS5)

        // Assert
        assertEquals(listOf("Hades", "Hades II"), names)
    }

    @Test
    fun `search filters by category when one is given`() = runTest {
        // Act
        val names = searchNames("Final Fantasy VII", PS4, PS5, PC, category = Category.REMAKE)

        // Assert
        assertEquals(listOf("Final Fantasy VII Remake"), names)
    }

    @Test
    fun `search includes every category when none is given`() = runTest {
        // Act
        val names = searchNames("Final Fantasy VII", PS5)

        // Assert - Including games that have none of the supported categories
        assertEquals(
            listOf(
                "Final Fantasy VII Remake Intergrade",
                "Final Fantasy VII Rebirth",
                "Final Fantasy VII Remake & Rebirth Twin Pack"
            ),
            names
        )
    }

    @Test
    fun `search returns an empty list when no game matches`() = runTest {
        assertEquals(emptyList(), search("Pong", PS5, PC, SWITCH))
    }

    @Test
    fun `search returns the games with their category and platforms`() = runTest {
        // Act
        val game = search("Elden Ring: Shadow", PS5).single()

        // Assert
        assertEquals(Category.DLC, game.category)
        assertTrue(game.platforms.any { it.id == 167L && it.abbreviation?.value == "PS5" })
    }

    // Platforms matched by name

    @Test
    fun `platforms matched by name are recognized by their name ignoring case`() = runTest {
        // Act
        val names = searchNames("Demon's Souls", byName("playstation 5", "PlayStation5"))

        // Assert
        assertEquals(1, names.size)
    }

    @Test
    fun `platforms matched by name are recognized by their abbreviation`() = runTest {
        // Act
        val names = searchNames("Demon's Souls", byName("Sony PlayStation 3", "PS3"))

        // Assert
        assertEquals(1, names.size)
    }

    @Test
    fun `platforms matched by name are recognized by their alternative names`() = runTest {
        // Act - NSW is an alternative name of the Nintendo Switch
        val names = searchNames("Tears of the Kingdom", byName("My Switch", "NSW"))

        // Assert
        assertEquals(listOf("The Legend of Zelda: Tears of the Kingdom"), names)
    }

    @Test
    fun `platforms matched by name require an exact match`() = runTest {
        // Act - "PS" is part of several abbreviations, but the abbreviation of none
        val names = searchNames("Final Fantasy", byName("Sony", "PS"))

        // Assert
        assertEquals(emptyList(), names)
    }

    @Test
    fun `unrecognized platforms matched by name contribute no games`() = runTest {
        // Act
        val names = searchNames("Hades", byName("Commodore 64", "C64"), PC)

        // Assert - Only the games for the recognized platform are included
        assertEquals(listOf("Hades", "Hades II"), names)
    }

    // Details

    @Test
    fun `fetchGameDetails returns the details of a game returned by the search`() = runTest {
        // Arrange
        val game = search("Elden Ring", PS5).first()

        // Act
        val details = sut.fetchGameDetails(game.id)

        // Assert
        assertNotNull(details)
        assertEquals(game, details.game)
        assertEquals(listOf("FromSoftware"), details.developers)
    }

    @Test
    fun `fetchGameDetails returns null for an unknown game`() = runTest {
        assertNull(sut.fetchGameDetails(-1L))
    }
}
