package isel.dei.pdm.mygamevault.infrastructure

import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.SearchService
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * A fake implementation of [SearchService] that filters a small in-memory list of games.
 */
class FakeSearchService : SearchService {

    private val games = listOf(
        Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er_cover", "cache://er_thumb"),
        Game(2, "The Legend of Zelda: Tears of the Kingdom", LocalDate.of(2023, 5, 12), "cache://totk_cover", "cache://totk_thumb"),
        Game(3, "Baldur's Gate 3", LocalDate.of(2023, 8, 3), "cache://bg3_cover", "cache://bg3_thumb"),
        Game(4, "Cyberpunk 2077", LocalDate.of(2020, 12, 10), "cache://cp2077_cover", "cache://cp2077_thumb"),
        Game(5, "Hades", LocalDate.of(2020, 9, 17), "cache://hades_cover", "cache://hades_thumb"),
        Game(6, "Hollow Knight", LocalDate.of(2017, 2, 24), "cache://hk_cover", "cache://hk_thumb"),
        Game(7, "The Witcher 3: Wild Hunt", LocalDate.of(2015, 5, 19), "cache://w3_cover", "cache://w3_thumb"),
        Game(8, "Red Dead Redemption 2", LocalDate.of(2018, 10, 26), "cache://rdr2_cover", "cache://rdr2_thumb"),
        Game(9, "Stardew Valley", LocalDate.of(2016, 2, 26), "cache://sv_cover", "cache://sv_thumb"),
        Game(10, "Outer Wilds", LocalDate.of(2019, 5, 28), "cache://ow_cover", "cache://ow_thumb")
    )

    override suspend fun search(query: String): List<Game> {
        // Simulate network delay
        delay(500)
        return if (query.isBlank()) {
            emptyList()
        } else {
            games.filter { it.name.contains(query, ignoreCase = true) }
        }
    }
}
