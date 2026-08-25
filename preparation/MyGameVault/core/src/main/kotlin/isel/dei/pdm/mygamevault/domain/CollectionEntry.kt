package isel.dei.pdm.mygamevault.domain

/**
 * Represents an entry in the user's game collection.
 * An entry consists of a game, the platform it's owned on, and its playing status.
 * @property game The catalog game entry.
 * @property platform The platform the game is owned on.
 * @property playStatus The user's playing status for this game on this platform.
 */
data class CollectionEntry(
    val game: Game,
    val platform: Platform,
    val playStatus: PlayStatus = PlayStatus()
)
