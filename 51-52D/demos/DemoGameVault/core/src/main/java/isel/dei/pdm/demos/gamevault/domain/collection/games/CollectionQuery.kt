package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString

/**
 * The criteria used to browse the collection. The set of criteria is fixed and they are all
 * combined (i.e. an entry must meet all of them), so that repositories can implement the query
 * as a single, fixed query. Criteria left at their default values are not applied, so the
 * default query matches all entries.
 *
 * @property state Only entries in this play state, or any state if null.
 * @property platinumOnly Only entries whose platinum was obtained.
 * @property platformId Only entries owned on the platform with this identifier, or any platform
 * if null.
 * @property nameContains Only entries whose game's name contains this text, ignoring case, or any
 * name if null.
 * @property initial Only entries whose game's name is listed under this initial, for alphabetical
 * navigation: an upper case letter (A to Z), or [OTHER_INITIAL] for names starting with digits
 * or symbols. Any initial if null.
 * DRAFT: How names are listed under initials is still to be decided (e.g. whether accents are
 * ignored, such as in Ōkami, or leading articles, such as in The Legend of Zelda).
 */
data class CollectionQuery(
    val state: PlayStatus.State? = null,
    val platinumOnly: Boolean = false,
    val platformId: Long? = null,
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
    }
}
