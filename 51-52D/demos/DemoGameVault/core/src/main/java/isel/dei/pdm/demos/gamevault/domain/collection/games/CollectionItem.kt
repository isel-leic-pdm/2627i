package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import java.net.URI

/**
 * The information of a collection entry needed to display it in listings (e.g. the carousel of
 * games being played, or the results of browsing the collection). It is a lightweight projection
 * of [CollectionEntry], which is only loaded when working with a single game (e.g. on its screen).
 *
 * @property gameId The identifier of the entry's game.
 * @property name The game's name.
 * @property releaseYear The game's release year, if known. Distinguishes games with the same name.
 * @property coverUri The URI of the game's cover image, if available.
 * @property thumbnailUri The URI of the game's thumbnail image, if available.
 * @property platformAbbreviation The abbreviation of the platform the game is owned on.
 * @property state The game's play state.
 * @property timeSpent The total time spent playing the game.
 * @property isFinished Whether the game was finished at least once (see [PlayStatus.isFinished]).
 * @property hasPlatinum Whether the game's platinum was obtained.
 * @property isSessionActive Whether a play session of the game is active.
 */
data class CollectionItem(
    val gameId: Long,
    val name: NonBlankString,
    val releaseYear: Int?,
    val coverUri: URI?,
    val thumbnailUri: URI?,
    val platformAbbreviation: NonBlankString,
    val state: PlayStatus.State,
    val timeSpent: PlayTime,
    val isFinished: Boolean,
    val hasPlatinum: Boolean,
    val isSessionActive: Boolean
)

/**
 * Converts this entry to the corresponding listing item. It defines the projection: repository
 * implementations that produce items directly (e.g. from a database query) must be consistent
 * with it.
 */
fun CollectionEntry.toItem(): CollectionItem = CollectionItem(
    gameId = game.id,
    name = game.name,
    releaseYear = game.releaseDate?.year,
    coverUri = game.coverUri,
    thumbnailUri = game.thumbnailUri,
    platformAbbreviation = platform.abbreviation,
    state = playStatus.state,
    timeSpent = playStatus.timeSpent,
    isFinished = playStatus.isFinished,
    hasPlatinum = playStatus.platinum != null,
    isSessionActive = isSessionActive
)
