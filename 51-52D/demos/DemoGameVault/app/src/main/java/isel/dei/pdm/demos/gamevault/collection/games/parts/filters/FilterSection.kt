package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.parts.icon
import isel.dei.pdm.demos.gamevault.collection.games.parts.labelResId
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * A titled group of mutually exclusive options of a filter (e.g. the play states), each one
 * displayed as a chip, of which at most one is selected. The chips wrap onto new lines instead of
 * scrolling, so that all options are visible at once.
 *
 * Options that stand for not filtering (e.g. any state) are options like the others, typically
 * represented by null and placed first, so that the section always has a selection.
 *
 * If [selected] is not one of the [options] (e.g. a platform removed from the collection after
 * the filter was applied), no chip is selected, as none of them describes the current filter.
 * Selecting any option is then a change, and is emitted.
 *
 * Only changes are emitted: selecting the already selected option emits nothing.
 *
 * @param title The title of the section, naming what is filtered by (e.g. Status).
 * @param options The options, in the order they are to be displayed.
 * @param selected The selected option, which is expected to be one of [options].
 * @param onSelected Callback invoked when the user selects another option.
 * @param optionLabel The label displayed for each option.
 * @param modifier Modifier to be applied to the section.
 * @param optionIcon The icon displayed for each option, or null for none.
 */
@Composable
fun <T> FilterSection(
    title: String,
    options: List<T>,
    selected: T,
    onSelected: (T) -> Unit,
    optionLabel: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    optionIcon: (T) -> ImageVector? = { null }
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag(FilterSectionTestTags.SECTION)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .semantics { heading() }
                .testTag(FilterSectionTestTags.TITLE)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = option == selected
                val icon = optionIcon(option)
                FilterChip(
                    selected = isSelected,
                    onClick = { if (!isSelected) onSelected(option) },
                    label = { Text(optionLabel(option)) },
                    leadingIcon = icon?.let {
                        {
                            // The label describes the option, so the icon is decorative
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    },
                    modifier = Modifier.testTag(FilterSectionTestTags.OPTION)
                )
            }
        }
    }
}

// Testing & preview utilities

object FilterSectionTestTags {
    const val SECTION = "FilterSection"
    const val TITLE = "FilterSectionTitle"
    const val OPTION = "FilterSectionOption"
}

@Suppress("Unused")
class FilterSectionPreviews {

    private val platforms = listOf(
        Platform(1L, "PS5", "PlayStation 5"),
        Platform(2L, "Switch", "Nintendo Switch"),
        Platform(3L, "PC", "PC (Microsoft Windows)")
    )

    @Preview(showBackground = true, widthDp = 360, name = "Status and platform sections")
    @Composable
    fun SectionsPreview() {
        DemoGameVaultTheme {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterSection(
                    title = stringResource(R.string.collection_filters_status_title),
                    options = listOf(null) + PlayStatus.State.entries,
                    selected = PlayStatus.State.PLAYING,
                    onSelected = {},
                    optionLabel = { state ->
                        state?.let { stringResource(it.labelResId()) }
                            ?: stringResource(R.string.collection_filter_any)
                    },
                    optionIcon = { it?.icon() }
                )
                FilterSection(
                    title = stringResource(R.string.collection_filters_platform_title),
                    options = listOf(null) + platforms,
                    selected = null,
                    onSelected = {},
                    optionLabel = { platform ->
                        platform?.abbreviation?.value
                            ?: stringResource(R.string.collection_filter_any)
                    }
                )
            }
        }
    }
}
