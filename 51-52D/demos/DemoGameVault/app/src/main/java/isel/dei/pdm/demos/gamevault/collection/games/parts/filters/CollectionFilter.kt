package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.parts.CollectionItemView
import isel.dei.pdm.demos.gamevault.collection.games.parts.PlatinumIcon
import isel.dei.pdm.demos.gamevault.collection.games.parts.PlayStateChip
import isel.dei.pdm.demos.gamevault.collection.games.parts.color
import isel.dei.pdm.demos.gamevault.collection.games.parts.icon
import isel.dei.pdm.demos.gamevault.collection.games.parts.labelResId
import isel.dei.pdm.demos.gamevault.collection.games.parts.platinumColor
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform

/**
 * The filters with which the collection can be browsed, as presented to the user (e.g. as chips).
 * The search by name and the alphabetical navigation are not filters in this sense, as they are
 * presented by their own controls.
 */
sealed interface CollectionFilter {

    /**
     * The entries in the given play [state], i.e. what the user is currently doing with them.
     */
    data class InState(val state: PlayStatus.State) : CollectionFilter

    /**
     * The entries that reached the given [completion], i.e. how far the user went in them,
     * whatever their current state (e.g. finished includes the finished games being replayed).
     */
    data class WithCompletion(val completion: Completion) : CollectionFilter

    /**
     * The entries owned on the given [platform], regardless of its version.
     */
    data class OnPlatform(val platform: Platform) : CollectionFilter

    companion object {
        /**
         * The filters offered as shortcuts on the home screen, in the order they are displayed:
         * those of the user's main path (from wanting to play a game to mastering it). Filters of
         * secondary relevance (e.g. paused or dropped games) are only offered when browsing the
         * collection, and entries in the NONE state are not offered, as they were not
         * categorized yet.
         */
        val shortcuts: List<CollectionFilter> =
            listOf(
                InState(PlayStatus.State.PLAYING),
                InState(PlayStatus.State.BACKLOG),
                WithCompletion(Completion.FINISHED),
                WithCompletion(Completion.PLATINUM)
            )
    }
}

/**
 * The filters applied by this query, in the order they are presented: status, completion and
 * platform, as in the filters sheet (see [FiltersSheetContent]). The search by name and the
 * initial are not filters in this sense (see [CollectionFilter]).
 */
fun CollectionQuery.activeFilters(): List<CollectionFilter> =
    listOfNotNull(
        state?.let { CollectionFilter.InState(it) },
        completion?.let { CollectionFilter.WithCompletion(it) },
        platform?.let { CollectionFilter.OnPlatform(it) }
    )

/**
 * The query that applies only this filter (e.g. to browse the collection from a shortcut).
 */
fun CollectionFilter.toQuery(): CollectionQuery =
    when (this) {
        is CollectionFilter.InState -> CollectionQuery(state = state)
        is CollectionFilter.WithCompletion -> CollectionQuery(completion = completion)
        is CollectionFilter.OnPlatform -> CollectionQuery(platform = platform)
    }

/**
 * This query without the criterion of the given [filter], whatever its value: the rest of the
 * query is kept as is.
 */
fun CollectionQuery.without(filter: CollectionFilter): CollectionQuery =
    when (filter) {
        is CollectionFilter.InState -> copy(state = null)
        is CollectionFilter.WithCompletion -> copy(completion = null)
        is CollectionFilter.OnPlatform -> copy(platform = null)
    }

/**
 * The icon displayed for each filter. Filters by play state or platinum use the icons of the
 * status chips (see [PlayStateChip]), so that a filter looks like the status it filters by.
 */
fun CollectionFilter.icon(): ImageVector =
    when (this) {
        is CollectionFilter.InState -> state.icon()
        is CollectionFilter.WithCompletion -> completion.icon()
        is CollectionFilter.OnPlatform -> Icons.Filled.VideogameAsset
    }

/**
 * The color of the icon displayed for each filter. Colors are taken from the theme's color roles,
 * so that they adapt to the dynamic color scheme and to the dark theme: only the filters that are
 * meant to stand out are highlighted.
 */
@Composable
fun CollectionFilter.iconColor(): Color =
    when (this) {
        is CollectionFilter.InState -> state.color()
        is CollectionFilter.WithCompletion -> completion.color()
        is CollectionFilter.OnPlatform -> MaterialTheme.colorScheme.onSurfaceVariant
    }

/**
 * The label displayed to the user for each filter. Platforms are labelled by their abbreviation,
 * as elsewhere in the collection (e.g. in [CollectionItemView]).
 */
@Composable
fun CollectionFilter.label(): String =
    when (this) {
        is CollectionFilter.InState -> stringResource(state.labelResId())
        is CollectionFilter.WithCompletion -> stringResource(completion.labelResId())
        is CollectionFilter.OnPlatform -> platform.abbreviation.value
    }

/**
 * The label displayed to the user for each completion.
 */
@StringRes
fun Completion.labelResId(): Int =
    when (this) {
        Completion.NOT_FINISHED -> R.string.completion_not_finished
        Completion.FINISHED -> R.string.completion_finished
        // TODO: The term used for platinum (e.g. Platinum, All achievements) is to become a user
        //  preference, as it is platform specific
        Completion.PLATINUM -> R.string.accomplishment_platinum
    }

/**
 * The icon displayed for each completion. Finished uses the icon of the FINISHED play state, and
 * platinum the icon of the platinum chip (see [PlayStateChip]); not finished uses the outline of
 * the finished icon, as the flag not yet reached.
 */
fun Completion.icon(): ImageVector =
    when (this) {
        Completion.NOT_FINISHED -> Icons.Outlined.Flag
        Completion.FINISHED -> Icons.Filled.Flag
        Completion.PLATINUM -> PlatinumIcon
    }

/**
 * The color of the icon displayed for each completion: only the platinum stands out, as in the
 * status chips (see [platinumColor]).
 */
@Composable
fun Completion.color(): Color =
    when (this) {
        Completion.PLATINUM -> platinumColor()
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
