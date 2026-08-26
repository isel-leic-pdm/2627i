package isel.dei.pdm.mygamevault.domain

import java.time.LocalDate

/**
 * Represents an entry in the user's game collection.
 * An entry consists of a game, the platform it's owned on, its playing status,
 * and the date it was added to the collection.
 * @property game The catalog game entry.
 * @property platform The platform the game is owned on.
 * @property playStatus The user's playing status for this game on this platform.
 * @property addedAt The date the game was added to the collection.
 */
data class CollectionEntry(
    val game: Game,
    val platform: Platform,
    val playStatus: PlayStatus = PlayStatus(),
    val addedAt: LocalDate = LocalDate.now()
)
