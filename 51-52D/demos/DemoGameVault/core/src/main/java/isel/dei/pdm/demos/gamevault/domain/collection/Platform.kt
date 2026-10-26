package isel.dei.pdm.demos.gamevault.domain.collection

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
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
 */
data class Platform(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: URI? = null,
    val ownedSince: Year? = null
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
        ownedSince: Int? = null
    ) : this(
        id = id,
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { URI.create(it) },
        ownedSince = ownedSince?.let { Year.of(it) }
    )
}

/**
 * Represents a platforms to be added to the user's collection.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 * @property ownedSince The year the platform was acquired, if available.
 */
data class NewPlatform(
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: URI? = null,
    val ownedSince: Year? = null
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
        ownedSince: Int? = null
    ) : this(
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { URI.create(it) },
        ownedSince = ownedSince?.let { Year.of(it) }
    )
}
