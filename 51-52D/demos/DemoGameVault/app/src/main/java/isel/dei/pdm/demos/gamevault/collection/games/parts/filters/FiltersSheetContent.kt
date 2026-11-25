package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.parts.icon
import isel.dei.pdm.demos.gamevault.collection.games.parts.labelResId
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The contents of the sheet with all the filters of the games collection, one [FilterSection] per
 * criterion: status, completion and platform. Each section has an option that stands for not
 * filtering by it (Any), which is how a filter is removed from the sheet.
 *
 * Status and completion are independent, and can be combined (e.g. playing a finished game, i.e.
 * replaying it):
 * - The status is what the user is currently doing with a game. The FINISHED play state is not
 * offered, as finished games are found through the completion, whatever their state. If [state] is
 * FINISHED (e.g. set elsewhere), no status option is selected.
 * - The completion is how far the user went in a game: not finished, finished or platinum. As the
 * platinum implies being finished, these are offered as mutually exclusive options, finished
 * including the games whose platinum was obtained (see [Completion]).
 *
 * Filters are applied as they are selected (i.e. there is no confirmation), so each section emits
 * its own changes. As in [FilterSection], only changes are emitted.
 *
 * The platform section is only displayed when there is something to choose from or to remove:
 * without [platforms] and without a selected [platform], it would offer nothing but Any. When the
 * selected platform is not one of [platforms] (e.g. it was removed from the collection after the
 * filter was applied), no option is selected, and selecting Any removes the filter.
 *
 * The contents scroll vertically if they do not fit (e.g. on small screens, or with many
 * platforms).
 *
 * @param state The play state filtered by, or null for any state.
 * @param completion The completion filtered by, or null for any completion.
 * @param platform The platform filtered by, or null for any platform.
 * @param platforms The platforms offered as options, in the order they are to be displayed. As
 * filtering by a platform includes all its versions, they are expected to be distinct as game
 * platforms (e.g. not both the PS5 and the PS5 Pro), otherwise the same option is offered twice.
 * @param onStateSelected Callback invoked when the user selects another play state, or null for
 * any state.
 * @param onCompletionSelected Callback invoked when the user selects another completion, or null
 * for any completion.
 * @param onPlatformSelected Callback invoked when the user selects another platform, or null for
 * any platform.
 * @param modifier Modifier to be applied to the contents.
 */
@Composable
fun FiltersSheetContent(
    state: PlayStatus.State?,
    completion: Completion?,
    platform: Platform?,
    platforms: List<Platform>,
    onStateSelected: (PlayStatus.State?) -> Unit,
    onCompletionSelected: (Completion?) -> Unit,
    onPlatformSelected: (Platform?) -> Unit,
    modifier: Modifier = Modifier
) {
    val any = stringResource(R.string.collection_filter_any)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
            .testTag(FiltersSheetContentTestTags.CONTENT),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.collection_filters_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .semantics { heading() }
        )

        // null stands for any state, and comes first
        FilterSection(
            title = stringResource(R.string.collection_filters_status_title),
            options = stateOptions,
            selected = state,
            onSelected = onStateSelected,
            optionLabel = { option -> option?.let { stringResource(it.labelResId()) } ?: any },
            optionIcon = { it?.icon() }
        )

        // null stands for any completion, and comes first
        FilterSection(
            title = stringResource(R.string.collection_filters_completion_title),
            options = listOf(null) + Completion.entries,
            selected = completion,
            onSelected = onCompletionSelected,
            optionLabel = { option -> option?.let { stringResource(it.labelResId()) } ?: any },
            optionIcon = { it?.icon() }
        )

        if (platforms.isNotEmpty() || platform != null) {
            // null stands for any platform, and comes first
            FilterSection(
                title = stringResource(R.string.collection_filters_platform_title),
                options = listOf(null) + platforms,
                selected = platform,
                onSelected = onPlatformSelected,
                optionLabel = { option -> option?.abbreviation?.value ?: any }
            )
        }
    }
}

/**
 * The options of the status section, in the order they are displayed: any state (null), and the
 * play states but FINISHED, as finished games are found through the completion.
 */
private val stateOptions: List<PlayStatus.State?> =
    listOf(null) + PlayStatus.State.entries.filter { it != PlayStatus.State.FINISHED }

// Testing & preview utilities

object FiltersSheetContentTestTags {
    const val CONTENT = "FiltersSheetContent"
}

@Suppress("Unused")
class FiltersSheetContentPreviews {

    private val platforms = listOf(
        Platform(1L, "PS5", "PlayStation 5"),
        Platform(2L, "Switch", "Nintendo Switch"),
        Platform(3L, "PC", "PC (Microsoft Windows)")
    )

    @Composable
    private fun PreviewOf(
        state: PlayStatus.State? = null,
        completion: Completion? = null,
        platform: Platform? = null,
        platforms: List<Platform> = this.platforms
    ) {
        DemoGameVaultTheme {
            FiltersSheetContent(
                state = state,
                completion = completion,
                platform = platform,
                platforms = platforms,
                onStateSelected = {},
                onCompletionSelected = {},
                onPlatformSelected = {}
            )
        }
    }

    @Preview(showBackground = true, widthDp = 360, name = "No filters")
    @Composable
    fun NoFiltersPreview() = PreviewOf()

    @Preview(showBackground = true, widthDp = 360, name = "Replaying platinum games on the PS5")
    @Composable
    fun AllFiltersPreview() = PreviewOf(
        state = PlayStatus.State.PLAYING,
        completion = Completion.PLATINUM,
        platform = platforms[0]
    )

    @Preview(showBackground = true, widthDp = 360, name = "No platforms")
    @Composable
    fun NoPlatformsPreview() = PreviewOf(platforms = emptyList())
}
