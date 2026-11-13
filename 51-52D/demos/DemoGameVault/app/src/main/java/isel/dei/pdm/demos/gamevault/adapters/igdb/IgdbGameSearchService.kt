package isel.dei.pdm.demos.gamevault.adapters.igdb

import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfoDetails
import isel.dei.pdm.demos.gamevault.domain.search.games.GamePlatform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameSearchService
import isel.dei.pdm.demos.gamevault.domain.search.games.PlatformCriterion
import isel.dei.pdm.demos.gamevault.domain.toNonBlankStringOrNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * A [GameSearchService] that searches for games in IGDB.
 *
 * Platforms are matched by name, against the name, abbreviation and alternative name of IGDB's
 * platforms, exactly but ignoring case (see [PlatformCriterion]).
 *
 * @param api The API used to execute the queries.
 */
class IgdbGameSearchService(private val api: IgdbApi) : GameSearchService {

    override suspend fun searchGames(
        name: SearchString,
        platforms: List<PlatformCriterion>,
        category: GameInfo.Category?
    ): List<GameInfo> {
        require(platforms.isNotEmpty()) { "There must be at least one platform" }
        val query = gamesQuery(name, platforms, category)
        return decode(api.query(endpoint = "games", query = query))
            .mapNotNull { it.toGameInfo() }
    }

    override suspend fun fetchGameDetails(gameId: Long): GameInfoDetails? =
        decode(api.query(endpoint = "games", query = gameDetailsQuery(gameId)))
            .firstOrNull()
            ?.toGameInfoDetails()

    /**
     * Decodes the games in the given response.
     * @throws UnexpectedServiceException If the response is not the expected one.
     */
    private fun decode(json: String): List<IgdbGame> =
        try {
            igdbJson.decodeFromString<List<IgdbGame>>(json)
        } catch (e: SerializationException) {
            throw UnexpectedServiceException("Unexpected response from IGDB", e)
        }
}

/**
 * The maximum number of results returned by a search.
 */
private const val MAX_RESULTS = 50

/**
 * The fields of the games included in the search results, which are those of [GameInfo].
 */
private const val GAME_FIELDS =
    "name, first_release_date, cover.image_id, game_type, " +
        "platforms.name, platforms.abbreviation, platforms.alternative_name"

/**
 * The IGDB game types (formerly, categories) of each supported [GameInfo.Category]. Games of the
 * remaining types (e.g. ports or expansions) have none of the supported categories.
 */
private val GAME_TYPES = mapOf(
    GameInfo.Category.MAIN_GAME to 0L,
    GameInfo.Category.DLC to 1L,
    GameInfo.Category.BUNDLE to 3L,
    GameInfo.Category.REMAKE to 8L,
    GameInfo.Category.REMASTER to 9L
)

/**
 * Builds the Apicalypse query that searches for the games whose name contains the given text,
 * ignoring case, released for any of the given platforms, and of the given category, if any.
 * @param name The text to search for.
 * @param platforms The platforms the games must be released for.
 * @param category The category of the games, or null for any category.
 * @return the payload to be sent in IGDB requests, following the syntax of the Apicalypse idiom.
 * @throws IllegalArgumentException If [platforms] is empty.
 */
internal fun gamesQuery(
    name: SearchString,
    platforms: List<PlatformCriterion>,
    category: GameInfo.Category?
): String {
    require(platforms.isNotEmpty()) { "There must be at least one platform" }
    val platformsCondition = platformsCondition(platforms)
    val text = name.value.trim().escapeForApicalypse()
    val categoryCondition = category?.let { " & game_type = ${GAME_TYPES.getValue(it)}" }.orEmpty()
    return """
        fields $GAME_FIELDS;
        where name ~ *"$text"* & ($platformsCondition)$categoryCondition;
        sort name asc;
        limit $MAX_RESULTS;
    """.trimIndent()
}

/**
 * Builds the Apicalypse query that fetches the details of the game with the given identifier.
 */
internal fun gameDetailsQuery(gameId: Long): String =
    """
        fields $GAME_FIELDS, summary, genres.name, involved_companies.company.name, involved_companies.developer, involved_companies.publisher;
        where id = $gameId;
        limit 1;
    """.trimIndent()

/**
 * The Apicalypse condition that matches the games released for any of the given platforms: those
 * with a platform whose name, abbreviation or alternative name is equal, ignoring case, to the
 * name or abbreviation of one of the given platforms.
 */
private fun platformsCondition(platforms: List<PlatformCriterion>): String =
    platforms
        .flatMap { listOf(it.name.value, it.abbreviation.value) }
        .map { it.trim().escapeForApicalypse() }
        .distinct()
        .flatMap { platformName ->
            listOf("name", "abbreviation", "alternative_name")
                .map { field -> """platforms.$field ~ "$platformName"""" }
        }
        .joinToString(" | ")

/**
 * Escapes the characters that have a special meaning inside Apicalypse strings, so that the
 * user's text cannot change the query. This prevents injection attacks.
 * @return the escaped string.
 */
private fun String.escapeForApicalypse(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

/**
 * A game, as returned by IGDB. Only the requested fields are declared, the details' fields being
 * absent from the search results.
 */
@Serializable
internal data class IgdbGame(
    val id: Long,
    val name: String? = null,
    @SerialName("first_release_date") val firstReleaseDate: Long? = null,
    val cover: IgdbCover? = null,
    @SerialName("game_type") val gameType: Long? = null,
    val platforms: List<IgdbGamePlatform> = emptyList(),
    val summary: String? = null,
    val genres: List<IgdbGenre> = emptyList(),
    @SerialName("involved_companies") val involvedCompanies: List<IgdbInvolvedCompany> = emptyList()
)

@Serializable
internal data class IgdbCover(
    @SerialName("image_id") val imageId: String
)

@Serializable
internal data class IgdbGamePlatform(
    val id: Long,
    val name: String? = null,
    val abbreviation: String? = null,
    @SerialName("alternative_name") val alternativeName: String? = null
)

@Serializable
internal data class IgdbGenre(val name: String? = null)

@Serializable
internal data class IgdbInvolvedCompany(
    val company: IgdbCompany? = null,
    val developer: Boolean = false,
    val publisher: Boolean = false
)

@Serializable
internal data class IgdbCompany(val name: String? = null)

/**
 * Converts this game into a [GameInfo], or null if its data does not meet the domain requirements
 * (e.g. it has no name), so that it is skipped instead of failing the whole search. The conversion
 * rules are:
 * - The release date is the first one, in UTC (IGDB provides it in seconds since the epoch).
 * - The cover and thumbnail are sizes of the same image (see [coverUri] and [thumbnailUri]).
 * - Game types other than the supported categories (e.g. ports) have no category.
 * - Platforms without name are left out.
 */
internal fun IgdbGame.toGameInfo(): GameInfo? {
    val name = name?.toNonBlankStringOrNull() ?: return null
    return GameInfo(
        id = id,
        name = name,
        releaseDate = firstReleaseDate?.toLocalDate(),
        coverUri = cover?.let { Uri(coverUri(it.imageId)) },
        thumbnailUri = cover?.let { Uri(thumbnailUri(it.imageId)) },
        category = GAME_TYPES.entries.firstOrNull { it.value == gameType }?.key,
        platforms = platforms.mapNotNull { it.toGamePlatform() }
    )
}

/**
 * Converts this game into a [GameInfoDetails], or null if it cannot be converted into a
 * [GameInfo]. Companies may be both developers and publishers.
 */
internal fun IgdbGame.toGameInfoDetails(): GameInfoDetails? {
    val game = toGameInfo() ?: return null
    fun companies(involved: (IgdbInvolvedCompany) -> Boolean) =
        involvedCompanies.filter(involved).mapNotNull { it.company?.name?.takeIf(String::isNotBlank) }
    return GameInfoDetails(
        game = game,
        description = summary?.takeIf { it.isNotBlank() },
        developers = companies { it.developer },
        publishers = companies { it.publisher },
        genres = genres.mapNotNull { it.name?.takeIf(String::isNotBlank) }
    )
}

/**
 * Converts this platform into a [GamePlatform], or null if it has no name.
 */
private fun IgdbGamePlatform.toGamePlatform(): GamePlatform? {
    val name = name?.toNonBlankStringOrNull() ?: return null
    return GamePlatform(
        id = id,
        name = name,
        abbreviation = abbreviation?.toNonBlankStringOrNull(),
        alternativeNames = listOfNotNull(alternativeName?.toNonBlankStringOrNull())
    )
}

/**
 * The date of this instant, given in seconds since the epoch, in UTC.
 */
private fun Long.toLocalDate(): LocalDate =
    Instant.fromEpochSeconds(this).toLocalDateTime(TimeZone.UTC).date

/**
 * The URI of the cover with the given image identifier. IGDB images are available in several
 * sizes: cover_big (264x374) suits the game's screen.
 */
private fun coverUri(imageId: String): String =
    "https://images.igdb.com/igdb/image/upload/t_cover_big/$imageId.jpg"

/**
 * The URI of the thumbnail of the cover with the given image identifier: cover_small (90x128)
 * suits the list items.
 */
private fun thumbnailUri(imageId: String): String =
    "https://images.igdb.com/igdb/image/upload/t_cover_small/$imageId.jpg"
