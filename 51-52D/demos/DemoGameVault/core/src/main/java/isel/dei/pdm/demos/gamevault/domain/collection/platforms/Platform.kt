package isel.dei.pdm.demos.gamevault.domain.collection.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference

/**
 * Represents a platform in the user's collection.
 *
 * Each version of a platform the user owns (e.g. PlayStation 5 and PlayStation 5 Slim) is a
 * distinct platform of the collection, identified by its [id]. Versions only matter to the
 * platforms themselves, though: games are released for a platform, regardless of its versions
 * (e.g. there are no PlayStation 5 Slim games, only PlayStation 5 games). Wherever games are
 * involved (e.g. the platforms a game is owned on, or searching games for a platform), versions
 * are therefore not distinguished (see [isSameGamePlatformAs]).
 *
 * @property id The unique identifier of the platform, as assigned by the repository
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 * @property ownedSince The year the platform was acquired, if available.
 * @property version The platform's version (e.g. Pro, Digital Edition), if available.
 * @property source The source from which the platform was obtained, or null if inserted manually.
 * Note that it does not identify the platform: different versions of a platform may share the
 * same source reference (e.g. IGDB uses the same identifier for PlayStation 5 and its Pro version).
 */
data class Platform(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: Uri? = null,
    val ownedSince: Year? = null,
    val version: NonBlankString? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts instrinsic types (String and int).
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     */
    constructor(
        id: Long,
        abbreviation: String,
        name: String,
        logoUri: String? = null,
        ownedSince: Int? = null,
        version: String? = null,
        source: SourceReference? = null
    ) : this(
        id = id,
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { Uri(it) },
        ownedSince = ownedSince?.let { Year(it) },
        version = version?.let { NonBlankString(it) },
        source = source
    )

    /**
     * Checks whether this platform and [other] are the same platform as far as games are
     * concerned, i.e. regardless of their versions (e.g. PlayStation 5 and PlayStation 5 Slim):
     * - If both were obtained from the same provider, they are the same if the provider identifies
     * them the same way (providers use the same identifier for all versions of a platform).
     * - Otherwise, they are the same if they have the same name or the same abbreviation,
     * ignoring case.
     */
    fun isSameGamePlatformAs(other: Platform): Boolean =
        if (source != null && other.source != null && source.provider == other.source.provider) {
            source.externalId == other.source.externalId
        } else {
            name.value.equals(other.name.value, ignoreCase = true) ||
                abbreviation.value.equals(other.abbreviation.value, ignoreCase = true)
        }
}

/**
 * The platforms that are distinct as far as games are concerned (see
 * [Platform.isSameGamePlatformAs]): of each set of versions of the same platform, only the first
 * one is kept, in the original order. Meant, for example, to list the platforms games can be
 * searched for.
 */
fun List<Platform>.distinctGamePlatforms(): List<Platform> =
    fold(emptyList()) { distinct, platform ->
        if (distinct.any { it.isSameGamePlatformAs(platform) }) distinct else distinct + platform
    }

/**
 * Represents a platforms to be added to the user's collection.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 * @property ownedSince The year the platform was acquired, if available.
 * @property version The platform's version (e.g. Pro, Digital Edition), if available.
 * @property source The source from which the platform was obtained, or null if inserted manually.
 */
data class NewPlatform(
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: Uri? = null,
    val ownedSince: Year? = null,
    val version: NonBlankString? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts instrinsic types (String and int).
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     */
    constructor(
        abbreviation: String,
        name: String,
        logoUri: String? = null,
        ownedSince: Int? = null,
        version: String? = null,
        source: SourceReference? = null
    ) : this(
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { Uri(it) },
        ownedSince = ownedSince?.let { Year(it) },
        version = version?.let { NonBlankString(it) },
        source = source
    )
}
