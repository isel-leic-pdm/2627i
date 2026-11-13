package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri
import kotlinx.datetime.LocalDate

/**
 * Represents the game information as provided by the game search service.
 * @param id The unique identifier of the game, as assigned by the service.
 * @param name The name of the game.
 * @param releaseDate The release date of the game, if available.
 * @param coverUri The URI of the game's cover image, if available.
 * @param thumbnailUri The URI of the game's thumbnail image, if available.
 * @param category The category of the game, or null if unknown or not one of the [Category]
 * values (e.g. the provider has finer grained categories, such as ports or expansions).
 * @param platforms The platforms the game was released for, as provided by the service. Helps the
 * user telling apart games with similar names (e.g. a game and its remaster).
 */
data class GameInfo(
    val id: Long,
    val name: NonBlankString,
    val releaseDate: LocalDate?,
    val coverUri: Uri?,
    val thumbnailUri: Uri?,
    val category: Category? = null,
    val platforms: List<GamePlatform> = emptyList()
) {
    /**
     * Represents the categories of games that can be used in the search, and that games are
     * classified with.
     */
    enum class Category {
        MAIN_GAME,
        DLC,
        BUNDLE,
        REMAKE,
        REMASTER
    }

    /**
     * Convenience constructor that accepts a string for the name, cover and thumbnail URIs.
     * @throws IllegalArgumentException if any of the strings does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     */
    @Throws
    constructor(
        id: Long,
        name: String,
        releaseDate: LocalDate? = null,
        coverUri: String? = null,
        thumbnailUri: String? = null,
        category: Category? = null,
        platforms: List<GamePlatform> = emptyList()
    ) : this(
        id = id,
        name = NonBlankString(name),
        releaseDate = releaseDate,
        coverUri = coverUri?.let { Uri(it) },
        thumbnailUri = thumbnailUri?.let { Uri(it) },
        category = category,
        platforms = platforms
    )
}

/**
 * Represents a platform a game was released for, as provided by the game search service. Unlike
 * the platforms of the user's collection, versions of a platform are not distinguished.
 * @property id The platform's identifier, as assigned by the service.
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property abbreviation The platform's abbreviation (e.g. PS5), if available.
 * @property alternativeNames Other names the platform is known by (e.g. NSW, for the Nintendo
 * Switch). Together with the name and abbreviation, they are used to recognize platforms matched
 * by name (see [PlatformCriterion]).
 */
data class GamePlatform(
    val id: Long,
    val name: NonBlankString,
    val abbreviation: NonBlankString? = null,
    val alternativeNames: List<NonBlankString> = emptyList()
) {
    // Note: A convenience constructor accepting Strings for name and abbreviation is not possible,
    // because NonBlankString is a value class: both constructors would have the same JVM signature.
}

/**
 * Represents the detailed game information as provided by the game search service. It is shown to
 * the user to help deciding whether this is the game to add to the collection. Once added, the
 * vault keeps its own copy of the details (see the collection's GameDetails).
 * @property game The basic game information.
 * @property description A summary or description of the game.
 * @property developers The developers of the game.
 * @property publishers The publishers of the game.
 * @property genres The genres of the game.
 */
data class GameInfoDetails(
    val game: GameInfo,
    val description: String?,
    val developers: List<String>,
    val publishers: List<String>,
    val genres: List<String>
)
