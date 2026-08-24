package isel.dei.pdm.mygamevault.core

import java.time.LocalDate

/**
 * Represents a game in the vault.
 * This is the core domain entity for a video game.
 */
data class Game(
    val id: Long,
    val name: NonBlankString,
    val releaseDate: LocalDate?,
    val coverUri: Uri?,
    val thumbnailUri: Uri?
) {
    /**
     * Represents the platforms supported by the search service.
     */
    enum class Platform {
        PS5,
        PS4,
        PS3,
        PS2,
        PS,
        XBOX,
        SWITCH,
        SWITCH_2,
        PC,
    }

    /**
     * Represents the categories of games that can be searched.
     */
    enum class Category {
        MAIN_GAME,
        DLC,
        BUNDLE,
        REMAKE,
        REMASTER
    }

    /**
     * Convenience constructor that accepts a string for the name, cover and thumbnail URIs.
     */
    constructor(
        id: Long,
        name: String,
        releaseDate: LocalDate?,
        coverUri: String?,
        thumbnailUri: String? = null
    ) : this(
        id,
        NonBlankString(name),
        releaseDate,
        coverUri?.let { Uri(it) },
        thumbnailUri?.let { Uri(it) }
    )
}
