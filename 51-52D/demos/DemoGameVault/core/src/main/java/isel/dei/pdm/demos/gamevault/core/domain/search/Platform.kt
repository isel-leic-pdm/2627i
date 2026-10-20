package isel.dei.pdm.demos.gamevault.core.domain.search

import java.net.URI

/**
 * Represents a platform where a game can be played.
 * @property id The platform's unique identifier, as returned by the search service.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 */
data class Platform(
    val id: Long,
    val abbreviation: SearchString,
    val name: SearchString,
    val logoUri: URI? = null
) {
    /**
     * Convenience constructor that accepts strings for abbreviation and name.
     * @throws IllegalArgumentException if any of the strings does not meet the requirements for
     * being a [SearchString] or a [URI].
     * @see SearchString
     * @see URI
     */
    @Throws(IllegalArgumentException::class)
    constructor(id: Long, abbreviation: String, name: String, logoUri: String? = null) : this(
        id = id,
        abbreviation = SearchString(abbreviation),
        name = SearchString(name),
        logoUri = logoUri?.let { URI.create(it) }
    )
}
