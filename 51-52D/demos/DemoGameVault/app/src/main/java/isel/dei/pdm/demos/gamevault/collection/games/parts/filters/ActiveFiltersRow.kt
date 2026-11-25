package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The filters currently applied to the collection, as a row of chips that scrolls horizontally if
 * they do not fit. Clicking a chip removes its filter. The row is meant to tell the user why the
 * displayed games are fewer than the whole collection (e.g. when arriving from a shortcut of the
 * home screen), while taking up no space when the collection is not filtered: without filters,
 * nothing is displayed.
 *
 * @param filters The filters currently applied, in the order they are to be displayed.
 * @param onRemoveFilter Callback invoked when the user removes one of the [filters].
 * @param modifier Modifier to be applied to the row.
 */
@Composable
fun ActiveFiltersRow(
    filters: List<CollectionFilter>,
    onRemoveFilter: (CollectionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filters.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .testTag(ActiveFiltersRowTestTags.ROW),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            // Keyed by filter, so that removing one does not move the others' state
            key(filter) {
                ActiveFilterChip(filter = filter, onRemove = { onRemoveFilter(filter) })
            }
        }
    }
}

/**
 * A chip displaying an active [filter], with the icon and label that identify it, and a trailing
 * icon signaling that clicking it removes the filter.
 */
@Composable
private fun ActiveFilterChip(filter: CollectionFilter, onRemove: () -> Unit) {
    val label = filter.label()
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(label) },
        leadingIcon = {
            // The label describes the filter, so the icon is decorative
            Icon(
                imageVector = filter.icon(),
                contentDescription = null,
                tint = filter.iconColor(),
                modifier = Modifier.size(InputChipDefaults.IconSize)
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(R.string.collection_filter_remove_description, label),
                modifier = Modifier.size(InputChipDefaults.IconSize)
            )
        },
        modifier = Modifier.testTag(ActiveFiltersRowTestTags.CHIP)
    )
}

// Testing & preview utilities

object ActiveFiltersRowTestTags {
    const val ROW = "ActiveFiltersRow"
    const val CHIP = "ActiveFilterChip"
}

@Suppress("Unused")
class ActiveFiltersRowPreviews {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")

    @Preview(showBackground = true, name = "One filter and all kinds of filters")
    @Composable
    fun ActiveFiltersPreview() {
        DemoGameVaultTheme {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActiveFiltersRow(
                    filters = listOf(CollectionFilter.WithCompletion(Completion.PLATINUM)),
                    onRemoveFilter = {}
                )
                ActiveFiltersRow(
                    filters = listOf(
                        CollectionFilter.InState(PlayStatus.State.PLAYING),
                        CollectionFilter.WithCompletion(Completion.FINISHED),
                        CollectionFilter.OnPlatform(ps5)
                    ),
                    onRemoveFilter = {}
                )
            }
        }
    }
}
