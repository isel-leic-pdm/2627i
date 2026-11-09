package isel.dei.pdm.demos.gamevault.domain.collection.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.SourceReference
import java.net.URI
import java.time.Year

/**
 * Represents a platform in the user's collection.
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
    val logoUri: URI? = null,
    val ownedSince: Year? = null,
    val version: NonBlankString? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts instrinsic types (String and int).
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [URI].
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
        logoUri = logoUri?.let { URI.create(it) },
        ownedSince = ownedSince?.let { Year.of(it) },
        version = version?.let { NonBlankString(it) },
        source = source
    )
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
    val logoUri: URI? = null,
    val ownedSince: Year? = null,
    val version: NonBlankString? = null,
    val source: SourceReference? = null
) {
    /**
     * Convenience constructor that accepts instrinsic types (String and int).
     * @throws IllegalArgumentException if any of the arguments does not meet the requirements for
     * being a [NonBlankString] or a [URI].
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
        logoUri = logoUri?.let { URI.create(it) },
        ownedSince = ownedSince?.let { Year.of(it) },
        version = version?.let { NonBlankString(it) },
        source = source
    )
}
