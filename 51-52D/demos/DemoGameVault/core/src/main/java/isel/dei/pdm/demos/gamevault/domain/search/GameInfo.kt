package isel.dei.pdm.demos.gamevault.domain.search

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI
import java.time.LocalDate

/**
 * Represents the game information as provided by the game search service.
 * @param id The unique identifier of the game, as assigned by the service.
 * @param name The name of the game.
 * @param releaseDate The release date of the game, if available.
 * @param coverUri The URI of the game's cover image, if available.
 * @param thumbnailUri The URI of the game's thumbnail image, if available.
 */
data class GameInfo(
    val id: Long,
    val name: NonBlankString,
    val releaseDate: LocalDate?,
    val coverUri: URI?,
    val thumbnailUri: URI?
) {
    /**
     * Represents the categories of games that can be used in the search.
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
     * being a [NonBlankString] or a [URI].
     */
    @Throws
    constructor(
        id: Long,
        name: String,
        releaseDate: LocalDate? = null,
        coverUri: String? = null,
        thumbnailUri: String? = null
    ) : this(
        id = id,
        name = NonBlankString(name),
        releaseDate = releaseDate,
        coverUri = coverUri?.let { URI.create(it) },
        thumbnailUri = thumbnailUri?.let { URI.create(it) }
    )
}

/**
 * Represents the full set of information shown on the details screen.
 * @property game The basic game information.
 * @property description A summary or description of the game.
 * @property developers The developers of the game.
 * @property publishers The publishers of the game.
 * @property genres The genres of the game.
 */
data class GameDetails(
    val game: GameInfo,
    val description: String?,
    val developers: List<String>,
    val publishers: List<String>,
    val genres: List<String>
)
