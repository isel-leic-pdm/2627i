package isel.dei.pdm.demos.gamevault.collection.games

import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery

/**
 * A section of the collection listing: the items whose names are listed under the same initial
 * (see [CollectionQuery.initialOf]).
 *
 * @property initial The section's initial: an upper case letter (A to Z), or
 * [CollectionQuery.OTHER_INITIAL].
 * @property items The section's items, in the order they are to be displayed. There is at least
 * one.
 */
data class CollectionSection(val initial: Char, val items: List<CollectionItem>)

/**
 * Groups the items into sections by the initial they are listed under. Sections are sorted
 * alphabetically, followed by the section of names starting with digits or symbols
 * ([CollectionQuery.OTHER_INITIAL]), as in a contacts list. Within each section, the items keep
 * their relative order (e.g. the order of the repository, by name), so this does not sort the
 * items, it only groups them.
 *
 * Grouping is purely alphabetical: items that the repository lists first for other reasons (e.g.
 * the game being played) are listed under their initial, like the others.
 */
fun List<CollectionItem>.toSections(): List<CollectionSection> =
    groupBy { CollectionQuery.initialOf(it.name) }
        .map { (initial, items) -> CollectionSection(initial, items) }
        .sortedWith(compareBy({ it.initial == CollectionQuery.OTHER_INITIAL }, { it.initial }))

/**
 * The minimum number of screens the listing must span for the initials index to be displayed.
 * Lists that fit in a couple of screens are easily scrolled, and the index would be clutter.
 */
internal const val INDEX_SHOW_SCREENS = 3

/**
 * The number of screens below which a displayed initials index is hidden. It is lower than
 * [INDEX_SHOW_SCREENS] so that the index does not blink in and out as the listing changes size
 * around the threshold (e.g. while filters are being changed).
 */
internal const val INDEX_HIDE_SCREENS = 2

/**
 * The minimum number of sections for the initials index to be displayed. With fewer initials,
 * the index offers few places to jump to, and is clutter.
 */
internal const val INDEX_MIN_INITIALS = 5

/**
 * Whether the initials index is to be displayed, given the size of the listing. The index is
 * displayed when the listing spans at least [INDEX_SHOW_SCREENS] screens and has at least
 * [INDEX_MIN_INITIALS] initials, and once displayed, it is only hidden when the listing spans
 * less than [INDEX_HIDE_SCREENS] screens or has too few initials.
 *
 * Invalid input is not rejected: without visible items (e.g. before the listing is laid out),
 * the listing's size in screens is unknown, so the index keeps its visibility.
 *
 * @param totalItems The number of items of the listing, including the section headers.
 * @param visibleItems The number of items that fit in the screen (i.e. that are visible).
 * @param initials The number of initials (i.e. of sections) of the listing.
 * @param isShown Whether the index is currently displayed.
 */
fun shouldShowInitialsIndex(
    totalItems: Int,
    visibleItems: Int,
    initials: Int,
    isShown: Boolean
): Boolean {
    if (visibleItems <= 0) return isShown
    if (initials < INDEX_MIN_INITIALS) return false
    val screens = totalItems.toFloat() / visibleItems
    return if (isShown) screens >= INDEX_HIDE_SCREENS else screens >= INDEX_SHOW_SCREENS
}
