package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import java.net.URI
import java.time.LocalDate
import java.util.Locale

/**
 * Represents a game in the user's vault, independently of how it was obtained (from a search
 * service or inserted manually).
 *
 * @property id The unique identifier of the game, as assigned by the repository.
 * @property name The game's name.
 * @property releaseDate The game's release date, if known.
 * @property coverUri The URI of the game's cover image, if available. Typically refers to a local
 * copy of the image, but any URI supported by the app's image loader is valid.
 * @property thumbnailUri The URI of the game's thumbnail image, if available.
 * @property source The source from which the game was obtained, or null if inserted manually.
 */
data class Game(
    val id: Long,
    val name: NonBlankString,
    val releaseDate: LocalDate? = null,
    val coverUri: URI? = null,
    val thumbnailUri: URI? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts strings for the name, cover and thumbnail URIs.
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [URI].
     */
    @Throws(IllegalArgumentException::class)
    constructor(
        id: Long,
        name: String,
        releaseDate: LocalDate? = null,
        coverUri: String? = null,
        thumbnailUri: String? = null,
        source: SourceReference? = null
    ) : this(
        id = id,
        name = NonBlankString(name),
        releaseDate = releaseDate,
        coverUri = coverUri?.let { URI.create(it) },
        thumbnailUri = thumbnailUri?.let { URI.create(it) },
        source = source
    )

    /**
     * Checks whether this game and [other] are the same game. See [isSameGame] for the rules.
     */
    infix fun isSameGameAs(other: Game): Boolean =
        isSameGame(name, releaseDate, source, other.name, other.releaseDate, other.source)
}

/**
 * Represents a game to be added to the user's vault.
 *
 * @property name The game's name.
 * @property releaseDate The game's release date, if known.
 * @property coverUri The URI of the game's cover image, if available.
 * @property thumbnailUri The URI of the game's thumbnail image, if available.
 * @property source The source from which the game was obtained, or null if inserted manually.
 */
data class NewGame(
    val name: NonBlankString,
    val releaseDate: LocalDate? = null,
    val coverUri: URI? = null,
    val thumbnailUri: URI? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts strings for the name, cover and thumbnail URIs.
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [URI].
     */
    @Throws(IllegalArgumentException::class)
    constructor(
        name: String,
        releaseDate: LocalDate? = null,
        coverUri: String? = null,
        thumbnailUri: String? = null,
        source: SourceReference? = null
    ) : this(
        name = NonBlankString(name),
        releaseDate = releaseDate,
        coverUri = coverUri?.let { URI.create(it) },
        thumbnailUri = thumbnailUri?.let { URI.create(it) },
        source = source
    )

    /**
     * Checks whether this game is the same as [game], already in the vault. See [isSameGame] for
     * the rules.
     */
    infix fun isSameGameAs(game: Game): Boolean =
        isSameGame(name, releaseDate, source, game.name, game.releaseDate, game.source)
}

/**
 * Determines whether two games are the same game, used to detect duplicates in the vault:
 * - If both games come from the same provider, they are the same game if they have the same
 * external identifier, regardless of their names (which the user may have edited).
 * - Otherwise, they are the same game if their names are equal, ignoring case, punctuation and
 * whitespace differences, and their release years are equal, if known for both. The release year
 * distinguishes games that share their name (e.g. God of War, from 2005 and 2018).
 */
private fun isSameGame(
    name: NonBlankString, releaseDate: LocalDate?, source: SourceReference?,
    otherName: NonBlankString, otherReleaseDate: LocalDate?, otherSource: SourceReference?
): Boolean {
    if (source != null && otherSource != null && source.provider == otherSource.provider) {
        return source.externalId == otherSource.externalId
    }
    val sameName = name.normalized() == otherName.normalized()
    val sameYear = releaseDate == null || otherReleaseDate == null ||
            releaseDate.year == otherReleaseDate.year
    return sameName && sameYear
}

private val punctuation = Regex("[^\\p{L}\\p{N}\\s]")
private val whitespace = Regex("\\s+")

/**
 * The name used to compare games: lower case, without punctuation, and with single spaces.
 */
private fun NonBlankString.normalized(): String =
    value.lowercase(Locale.ROOT)
        .replace(punctuation, " ")
        .replace(whitespace, " ")
        .trim()
