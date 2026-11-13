package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri
import kotlinx.datetime.LocalDate

/**
 * Represents a game in the user's vault. The vault is authoritative: the game is identified by
 * its [id] alone, and its information is the vault's, however it was first obtained (copied from
 * a search service, as a convenience, or inserted manually). The vault does not keep track of
 * where the information came from.
 *
 * @property id The unique identifier of the game, as assigned by the repository.
 * @property name The game's name.
 * @property releaseDate The game's release date, if known.
 * @property coverUri The URI of the game's cover image, if available. Typically refers to a local
 * copy of the image, but any URI supported by the app's image loader is valid.
 * @property thumbnailUri The URI of the game's thumbnail image, if available.
 */
data class Game(
    val id: Long,
    val name: NonBlankString,
    val releaseDate: LocalDate? = null,
    val coverUri: Uri? = null,
    val thumbnailUri: Uri? = null
) {
    /**
     * Convenience constructor that accepts strings for the name, cover and thumbnail URIs.
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     */
    @Throws(IllegalArgumentException::class)
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
        coverUri = coverUri?.let { Uri(it) },
        thumbnailUri = thumbnailUri?.let { Uri(it) }
    )

    /**
     * Checks whether this game and [other] are the same game. See [isSameGame] for the rules.
     */
    infix fun isSameGameAs(other: Game): Boolean =
        isSameGame(name, releaseDate, other.name, other.releaseDate)
}

/**
 * Represents a game to be added to the user's vault.
 *
 * @property name The game's name.
 * @property releaseDate The game's release date, if known.
 * @property coverUri The URI of the game's cover image, if available.
 * @property thumbnailUri The URI of the game's thumbnail image, if available.
 */
data class NewGame(
    val name: NonBlankString,
    val releaseDate: LocalDate? = null,
    val coverUri: Uri? = null,
    val thumbnailUri: Uri? = null
) {
    /**
     * Convenience constructor that accepts strings for the name, cover and thumbnail URIs.
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     */
    @Throws(IllegalArgumentException::class)
    constructor(
        name: String,
        releaseDate: LocalDate? = null,
        coverUri: String? = null,
        thumbnailUri: String? = null
    ) : this(
        name = NonBlankString(name),
        releaseDate = releaseDate,
        coverUri = coverUri?.let { Uri(it) },
        thumbnailUri = thumbnailUri?.let { Uri(it) }
    )

    /**
     * Checks whether this game is the same as [game], already in the vault. See [isSameGame] for
     * the rules.
     */
    infix fun isSameGameAs(game: Game): Boolean =
        isSameGame(name, releaseDate, game.name, game.releaseDate)
}

/**
 * Determines whether two games are the same game, used to prevent the user from adding duplicates
 * to the vault by accident: they are the same game if their names are equal, ignoring case,
 * punctuation and whitespace differences, and their release years are equal, if known for both.
 * The release year distinguishes games that share their name (e.g. God of War, from 2005 and
 * 2018). It is a safeguard, not an identity: duplicates that slip through (e.g. games named
 * differently) are to be consolidated by the user.
 */
private fun isSameGame(
    name: NonBlankString, releaseDate: LocalDate?,
    otherName: NonBlankString, otherReleaseDate: LocalDate?
): Boolean {
    val sameName = name.normalized() == otherName.normalized()
    val sameYear = releaseDate == null || otherReleaseDate == null ||
            releaseDate.year == otherReleaseDate.year
    return sameName && sameYear
}

private val punctuation = Regex("[^\\p{L}\\p{N}\\s]")
private val whitespace = Regex("\\s+")

/**
 * The name used to compare games: lower case, without punctuation, and with single spaces.
 * Kotlin's lowercase is locale invariant, so the comparison does not depend on the device's
 * language (e.g. Turkish, where the lower case of 'I' is not 'i').
 */
private fun NonBlankString.normalized(): String =
    value.lowercase()
        .replace(punctuation, " ")
        .replace(whitespace, " ")
        .trim()
