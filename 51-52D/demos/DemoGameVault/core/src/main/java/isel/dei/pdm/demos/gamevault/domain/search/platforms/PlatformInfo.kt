package isel.dei.pdm.demos.gamevault.domain.search.platforms

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Uri

/**
 * Represents the platform where a game can be played, as provided by the platform search service.
 * Each version of a platform (e.g. PlayStation 5 and PlayStation 5 Pro) is represented by a
 * distinct instance.
 * @property id The platform's identifier, as assigned by the service. Different versions of the
 * same platform share the same identifier.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo, if available.
 * @property version The platform's version (e.g. Pro, Digital Edition), if available.
 */
data class PlatformInfo(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: Uri? = null,
    val version: NonBlankString? = null
) {
    /**
     * Convenience constructor that accepts strings for abbreviation, name, logo URI and version.
     * @throws IllegalArgumentException if any of the strings does not meet the requirements for
     * being a [NonBlankString] or a [Uri].
     * @see NonBlankString
     * @see Uri
     */
    @Throws(IllegalArgumentException::class)
    constructor(
        id: Long,
        abbreviation: String,
        name: String,
        logoUri: String? = null,
        version: String? = null
    ) : this(
        id = id,
        abbreviation = NonBlankString(abbreviation),
        name = NonBlankString(name),
        logoUri = logoUri?.let { Uri(it) },
        version = version?.let { NonBlankString(it) }
    )
}
