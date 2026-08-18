package isel.dei.pdm.mygamevault.core

import java.time.LocalDate

/**
 * Represents a game in the vault.
 * This is the core domain entity for a video game.
 */
data class Game(
    val id: Long,
    val name: String,
    val releaseDate: LocalDate?,
    val coverUri: Uri?,
    val thumbnailUri: Uri?
) {
    init {
        require(name.isNotBlank()) { "Game name cannot be blank" }
    }

    /**
     * Convenience constructor that accepts a string for the cover and thumbnail URIs.
     */
    constructor(
        id: Long,
        name: String,
        releaseDate: LocalDate?,
        coverUri: String?,
        thumbnailUri: String? = null
    ) : this(
        id,
        name,
        releaseDate,
        coverUri?.let { Uri(it) },
        thumbnailUri?.let { Uri(it) }
    )
}
