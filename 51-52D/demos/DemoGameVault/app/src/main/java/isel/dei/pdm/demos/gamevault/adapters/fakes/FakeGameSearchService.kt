package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo.Category
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfoDetails
import isel.dei.pdm.demos.gamevault.domain.search.games.GamePlatform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of the [GameSearchService] interface.
 * We use it to have fine control over the service behavior while developing the app.
 *
 * Platforms are described as IGDB does (e.g. PlayStation 5, or PS5), so that the platforms added
 * to the collection through the actual platform search service are matched as they would be by
 * the actual game search service: by their name, abbreviation, or one of their alternative names,
 * ignoring case (see [PlatformCriterion.matches]).
 *
 * The catalogue includes games with similar names (e.g. a game and its remake), which is what the
 * search results must help telling apart.
 */
class FakeGameSearchService : GameSearchService {

    // The platforms games are released for

    private val ps1 = platform(7L, "PlayStation", "PS1", "PSX", "PS one")
    private val ps3 = platform(9L, "PlayStation 3", "PS3")
    private val ps4 = platform(48L, "PlayStation 4", "PS4")
    private val ps5 = platform(167L, "PlayStation 5", "PS5")
    private val xboxOne = platform(49L, "Xbox One", "XONE")
    private val xboxSeries =
        platform(169L, "Xbox Series X|S", "Series X|S", "XSX", "Xbox Series X", "Xbox Series S")
    private val switch = platform(130L, "Nintendo Switch", "Switch", "NSW", "Switch 1")
    private val switch2 = platform(508L, "Nintendo Switch 2", "Switch 2", "NS2")
    private val pc = platform(6L, "PC (Microsoft Windows)", "PC", "Windows", "Win")

    /**
     * The games known by the service, with their details.
     */
    private val catalogue: List<GameInfoDetails> = listOf(
        game(
            1L, "Elden Ring", LocalDate(2022, 2, 25), "elden_ring", Category.MAIN_GAME,
            listOf(pc, ps4, ps5, xboxOne, xboxSeries),
            "An action RPG set in the Lands Between.",
            developers = listOf("FromSoftware"),
            publishers = listOf("Bandai Namco Entertainment"),
            genres = listOf("Role-playing (RPG)", "Adventure")
        ),
        game(
            2L, "Elden Ring: Shadow of the Erdtree", LocalDate(2024, 6, 21), "erdtree", Category.DLC,
            listOf(pc, ps4, ps5, xboxOne, xboxSeries),
            "An expansion that takes the player to the Land of Shadow.",
            developers = listOf("FromSoftware"),
            publishers = listOf("Bandai Namco Entertainment"),
            genres = listOf("Role-playing (RPG)", "Adventure")
        ),
        game(
            3L, "Demon's Souls", LocalDate(2009, 2, 5), "demons_souls_2009", Category.MAIN_GAME,
            listOf(ps3),
            "A dark fantasy action RPG set in the kingdom of Boletaria.",
            developers = listOf("FromSoftware"),
            publishers = listOf("Sony Computer Entertainment", "Atlus"),
            genres = listOf("Role-playing (RPG)")
        ),
        game(
            4L, "Demon's Souls", LocalDate(2020, 11, 12), "demons_souls_2020", Category.REMAKE,
            listOf(ps5),
            "A remake of the 2009 game, rebuilt from the ground up.",
            developers = listOf("Bluepoint Games"),
            publishers = listOf("Sony Interactive Entertainment"),
            genres = listOf("Role-playing (RPG)")
        ),
        game(
            5L, "Final Fantasy VII", LocalDate(1997, 1, 31), "ff7", Category.MAIN_GAME,
            listOf(ps1, pc, ps4, switch, xboxOne),
            "Cloud Strife joins the eco-terrorist group AVALANCHE to fight the Shinra corporation.",
            developers = listOf("Square"),
            publishers = listOf("Sony Computer Entertainment", "Square Enix"),
            genres = listOf("Role-playing (RPG)")
        ),
        game(
            6L, "Final Fantasy VII Remake", LocalDate(2020, 4, 10), "ff7_remake", Category.REMAKE,
            listOf(ps4),
            "The first part of the reimagining of Final Fantasy VII, set in the city of Midgar.",
            developers = listOf("Square Enix"),
            publishers = listOf("Square Enix"),
            genres = listOf("Role-playing (RPG)", "Hack and slash/Beat 'em up")
        ),
        game(
            7L, "Final Fantasy VII Remake Intergrade", LocalDate(2021, 6, 10), "ff7_intergrade",
            // An expanded game in IGDB, which is none of the supported categories
            category = null,
            platforms = listOf(ps5, pc, switch2, xboxSeries),
            description = "An enhanced version of Final Fantasy VII Remake, with a new episode.",
            developers = listOf("Square Enix"),
            publishers = listOf("Square Enix"),
            genres = listOf("Role-playing (RPG)")
        ),
        game(
            8L, "Final Fantasy VII Rebirth", LocalDate(2024, 2, 29), "ff7_rebirth", Category.MAIN_GAME,
            listOf(ps5, pc),
            "The second part of the reimagining of Final Fantasy VII, beyond Midgar.",
            developers = listOf("Square Enix"),
            publishers = listOf("Square Enix"),
            genres = listOf("Role-playing (RPG)", "Adventure")
        ),
        game(
            9L, "Final Fantasy VII Remake & Rebirth Twin Pack", LocalDate(2025, 1, 23), "ff7_twin_pack",
            Category.BUNDLE,
            listOf(ps5, pc),
            "Both parts of the Final Fantasy VII remake project released so far.",
            developers = listOf("Square Enix"),
            publishers = listOf("Square Enix"),
            genres = listOf("Role-playing (RPG)")
        ),
        game(
            10L, "The Last of Us", LocalDate(2013, 6, 14), "tlou", Category.MAIN_GAME,
            listOf(ps3),
            "Joel and Ellie cross a post-pandemic United States.",
            developers = listOf("Naughty Dog"),
            publishers = listOf("Sony Computer Entertainment"),
            genres = listOf("Shooter", "Adventure")
        ),
        game(
            11L, "The Last of Us Remastered", LocalDate(2014, 7, 29), "tlou_remastered", Category.REMASTER,
            listOf(ps4),
            "The 2013 game, remastered for the PlayStation 4, with its expansion included.",
            developers = listOf("Naughty Dog"),
            publishers = listOf("Sony Computer Entertainment"),
            genres = listOf("Shooter", "Adventure")
        ),
        game(
            12L, "The Last of Us Part I", LocalDate(2022, 9, 2), "tlou_part_1", Category.REMAKE,
            listOf(ps5, pc),
            "A remake of the 2013 game.",
            developers = listOf("Naughty Dog"),
            publishers = listOf("Sony Interactive Entertainment"),
            genres = listOf("Shooter", "Adventure")
        ),
        game(
            13L, "The Legend of Zelda: Breath of the Wild", LocalDate(2017, 3, 3), "botw",
            Category.MAIN_GAME,
            listOf(switch),
            "Link awakens in a ruined Hyrule, to defeat Calamity Ganon.",
            developers = listOf("Nintendo EPD"),
            publishers = listOf("Nintendo"),
            genres = listOf("Adventure", "Role-playing (RPG)")
        ),
        game(
            14L, "The Legend of Zelda: Tears of the Kingdom", LocalDate(2023, 5, 12), "totk",
            Category.MAIN_GAME,
            listOf(switch),
            "Link explores the skies, lands and depths of Hyrule.",
            developers = listOf("Nintendo EPD"),
            publishers = listOf("Nintendo"),
            genres = listOf("Adventure", "Role-playing (RPG)")
        ),
        game(
            15L, "The Legend of Zelda: Tears of the Kingdom - Nintendo Switch 2 Edition",
            LocalDate(2025, 6, 5), "totk_switch_2", Category.REMASTER,
            listOf(switch2),
            "Tears of the Kingdom, enhanced for the Nintendo Switch 2.",
            developers = listOf("Nintendo EPD"),
            publishers = listOf("Nintendo"),
            genres = listOf("Adventure", "Role-playing (RPG)")
        ),
        game(
            16L, "Hades", LocalDate(2020, 9, 17), "hades", Category.MAIN_GAME,
            listOf(pc, switch, ps4, ps5, xboxOne, xboxSeries),
            "Zagreus fights his way out of the Underworld.",
            developers = listOf("Supergiant Games"),
            publishers = listOf("Supergiant Games"),
            genres = listOf("Role-playing (RPG)", "Hack and slash/Beat 'em up", "Indie")
        ),
        game(
            17L, "Hades II", LocalDate(2025, 9, 25), "hades_2", Category.MAIN_GAME,
            listOf(pc, switch, switch2),
            "Melinoë battles beyond the Underworld against the Titan of Time.",
            developers = listOf("Supergiant Games"),
            publishers = listOf("Supergiant Games"),
            genres = listOf("Role-playing (RPG)", "Hack and slash/Beat 'em up", "Indie")
        ),
    )

    override suspend fun searchGames(
        name: SearchString,
        platforms: List<PlatformCriterion>,
        category: Category?
    ): List<GameInfo> {
        require(platforms.isNotEmpty()) { "There must be at least one platform" }
        delay(500.milliseconds) // Simulate network delay
        return catalogue
            .map { it.game }
            .filter { game ->
                game.name.value.contains(name.value, ignoreCase = true) &&
                    (category == null || game.category == category) &&
                    game.platforms.any { platform -> platforms.any { it.matches(platform) } }
            }
    }

    override suspend fun fetchGameDetails(gameId: Long): GameInfoDetails? {
        delay(500.milliseconds) // Simulate network delay
        return catalogue.firstOrNull { it.game.id == gameId }
    }

}

private fun platform(id: Long, name: String, abbreviation: String, vararg alternativeNames: String) =
    GamePlatform(
        id = id,
        name = NonBlankString(name),
        abbreviation = NonBlankString(abbreviation),
        alternativeNames = alternativeNames.map { NonBlankString(it) }
    )

private fun game(
    id: Long,
    name: String,
    releaseDate: LocalDate,
    imageName: String,
    category: Category?,
    platforms: List<GamePlatform>,
    description: String,
    developers: List<String>,
    publishers: List<String>,
    genres: List<String>
) = GameInfoDetails(
    game = GameInfo(
        id = id,
        name = name,
        releaseDate = releaseDate,
        coverUri = "https://fake.url/image/${imageName}_cover.png",
        thumbnailUri = "https://fake.url/image/${imageName}_thumb.png",
        category = category,
        platforms = platforms
    ),
    description = description,
    developers = developers,
    publishers = publishers,
    genres = genres
)
