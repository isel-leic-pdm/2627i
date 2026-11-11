package isel.dei.pdm.demos.gamevault.search.games

import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus

/**
 * Represents the UI state for an item in the game search list. Each item is a game on one of the
 * platforms of the user's collection it was released for, so a game released for several of them
 * has one item per platform, and the user selects the one to add the game on. Versions of a
 * platform are not distinguished, as games are oblivious of them: there is a single item for all
 * the versions the user owns.
 *
 * @property game The game information obtained from the search service.
 * @property platform The platform of the user's collection the game is added on, if selected
 * (one of its versions, which is merely a reference).
 * @property addStatus The status of adding the game to the user's collection on the platform. A
 * game may be owned on several platforms, so each item has its own status.
 */
data class GameSearchItemUiState(
    val game: GameInfo,
    val platform: Platform,
    val addStatus: AddStatus = AddStatus.NOT_ADDED
) {
    /**
     * The key that uniquely identifies the item in the search results. The game's id is not
     * enough, because each game has one item per platform.
     */
    val key: String = "${game.id}/${platform.id}"
}
