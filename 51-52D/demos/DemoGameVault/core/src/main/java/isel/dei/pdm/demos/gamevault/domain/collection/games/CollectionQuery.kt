package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform

/**
 * The criteria used to browse the collection. The set of criteria is fixed, and they are all
 * combined (i.e. an entry must meet all of them), so that repositories can implement the query
 * as a single, fixed query. Criteria left at their default values are not applied, so the
 * default query matches all entries.
 *
 * @property state Only entries in this play state (i.e. what the user is currently doing with
 * them), or any state if null.
 * @property completion Only entries that reached this completion (i.e. how far the user went in
 * them, regardless of their state; see [PlayStatus.meets]), or any completion if null. For
 * example, [Completion.FINISHED] includes the finished games being replayed, and those whose
 * platinum was obtained.
 * @property platform Only entries owned on this platform, regardless of its version (see
 * [CollectionEntry.isOwnedOn]), among others, as games may be owned on several platforms. Any
 * platform if null. For example, filtering by the PS5 includes the games registered as owned on
 * the PS5 Slim.
 * @property nameContains Only entries whose game's name contains this text, ignoring case, or any
 * name if null.
 * @property initial Only entries whose game's name is listed under this initial (see
 * [initialOf]), for alphabetical navigation: an upper case letter (A to Z), or [OTHER_INITIAL]
 * for names starting with digits or symbols. Any initial if null.
 */
data class CollectionQuery(
    val state: PlayStatus.State? = null,
    val completion: Completion? = null,
    val platform: Platform? = null,
    val nameContains: NonBlankString? = null,
    val initial: Char? = null
) {
    init {
        require(initial == null || initial == OTHER_INITIAL || initial in 'A'..'Z') {
            "The initial must be an upper case letter (A to Z) or '$OTHER_INITIAL'"
        }
    }

    companion object {
        /**
         * The initial under which the names starting with digits or symbols are listed.
         */
        const val OTHER_INITIAL = '#'

        /**
         * The initial under which the given game name is listed, for alphabetical navigation
         * (e.g. the sections of a listing, or [initial]): its first letter, in upper case,
         * ignoring leading whitespace, if it is a letter from A to Z; otherwise, [OTHER_INITIAL].
         * Repositories that list under initials themselves (e.g. in a database query) must be
         * consistent with it.
         *
         * DRAFT: Accents and leading articles are not taken into account yet (e.g. Ōkami is
         * listed under [OTHER_INITIAL], and The Legend of Zelda under T), as the rules are
         * still to be decided.
         */
        fun initialOf(name: NonBlankString): Char {
            val first = name.value.trimStart().first().uppercaseChar()
            return if (first in 'A'..'Z') first else OTHER_INITIAL
        }
    }
}
