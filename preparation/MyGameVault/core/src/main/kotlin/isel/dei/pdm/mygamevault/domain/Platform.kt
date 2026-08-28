package isel.dei.pdm.mygamevault.domain

/**
 * Represents a platform where a game can be played.
 * @property id The platform's unique identifier.
 * @property abbreviation The platform's abbreviation (e.g. PS5, XBOX).
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property logoUri The URI of the platform's logo.
 */
data class Platform(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: Uri? = null
) {
    /**
     * Convenience constructor that accepts strings for abbreviation and name.
     */
    constructor(id: Long, abbreviation: String, name: String, logoUri: String? = null) : this(
        id,
        NonBlankString(abbreviation),
        NonBlankString(name),
        logoUri?.let { Uri(it) }
    )
}
