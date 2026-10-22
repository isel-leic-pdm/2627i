package isel.dei.pdm.demos.gamevault.domain.search

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI

/**
 * Represents the platform where a game can be played, as provided by the platform search service.
 * @property id The platform's unique identifier.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 */
data class PlatformInfo(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: URI? = null
) {
    /**
     * Convenience constructor that accepts strings for abbreviation and name.
     * @throws IllegalArgumentException if any of the strings does not meet the requirements for
     * being a [NonBlankString] or a [URI].
     * @see NonBlankString
     * @see URI
     */
    @Throws(IllegalArgumentException::class)
    constructor(id: Long, abbreviation: String, name: String, logoUri: String? = null) : this(
        id = id,
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { URI.create(it) }
    )
}
