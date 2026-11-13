package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString

/**
 * Represents the detailed information of a game in the user's vault, shown on the game's screen.
 * It is kept apart from [Game], which holds the information needed in listings, so that the
 * details are only loaded when needed.
 *
 * The details belong to the game, not to its collection entries: a game owned on several
 * platforms has a single set of details. They are either copied from the search service when the
 * game is added, as a convenience, or inserted by the user. Either way, they are the vault's, and
 * the user may edit them.
 *
 * @property game The game these details refer to.
 * @property description A summary or description of the game, if available.
 * @property developers The developers of the game, in the order they are to be displayed.
 * @property publishers The publishers of the game, in the order they are to be displayed.
 * @property genres The genres of the game, in the order they are to be displayed.
 * @property notes The user's personal notes about the game, if any.
 */
data class GameDetails(
    val game: Game,
    val description: NonBlankString? = null,
    val developers: List<NonBlankString> = emptyList(),
    val publishers: List<NonBlankString> = emptyList(),
    val genres: List<NonBlankString> = emptyList(),
    val notes: NonBlankString? = null
) {
    companion object {
        /**
         * Convenience factory that accepts strings instead of [NonBlankString]s.
         *
         * Note: It is not a secondary constructor, because it would have the same JVM signature
         * as the primary one: NonBlankString is a value class (compiled as a String) and the
         * lists' type arguments are erased. Neither is it named after the class (as a top-level
         * function or a companion `invoke`), because calls such as `GameDetails(game)` would then
         * be ambiguous.
         *
         * @throws IllegalArgumentException if any of the strings is blank. To discard blank
         * values instead, convert them beforehand (e.g. using
         * [isel.dei.pdm.demos.gamevault.domain.toNonBlankStringOrNull]).
         */
        @Throws(IllegalArgumentException::class)
        fun of(
            game: Game,
            description: String? = null,
            developers: List<String> = emptyList(),
            publishers: List<String> = emptyList(),
            genres: List<String> = emptyList(),
            notes: String? = null
        ): GameDetails = GameDetails(
            game = game,
            description = description?.let { NonBlankString(it) },
            developers = developers.map { NonBlankString(it) },
            publishers = publishers.map { NonBlankString(it) },
            genres = genres.map { NonBlankString(it) },
            notes = notes?.let { NonBlankString(it) }
        )
    }
}

/**
 * Represents the details of a game to be added to the user's vault, i.e. [GameDetails] without the
 * game, which does not have an identifier yet.
 *
 * @property description A summary or description of the game, if available.
 * @property developers The developers of the game, in the order they are to be displayed.
 * @property publishers The publishers of the game, in the order they are to be displayed.
 * @property genres The genres of the game, in the order they are to be displayed.
 * @property notes The user's personal notes about the game, if any.
 */
data class NewGameDetails(
    val description: NonBlankString? = null,
    val developers: List<NonBlankString> = emptyList(),
    val publishers: List<NonBlankString> = emptyList(),
    val genres: List<NonBlankString> = emptyList(),
    val notes: NonBlankString? = null
) {
    /**
     * The details of the given [game], once it was added to the vault.
     */
    fun toGameDetails(game: Game): GameDetails =
        GameDetails(game, description, developers, publishers, genres, notes)
}
