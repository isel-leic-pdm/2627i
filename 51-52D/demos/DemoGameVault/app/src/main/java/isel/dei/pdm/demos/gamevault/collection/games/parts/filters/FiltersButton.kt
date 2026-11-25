package isel.dei.pdm.demos.gamevault.collection.games.parts.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The action that gives access to the filters of the games collection. A badge with the number of
 * active filters signals that the displayed games are filtered, even when the filters themselves
 * are not on screen.
 *
 * For accessibility, the badge is announced as the number of active filters (e.g. "2 active"),
 * instead of the bare number.
 *
 * Invalid input is not rejected: the button is always displayed, and only the badge depends on
 * [activeFilterCount]. A negative count is treated as no active filters (i.e. no badge), rather
 * than displayed as is.
 *
 * @param activeFilterCount The number of filters currently applied. The badge is displayed only
 * if it is positive.
 * @param onClick Callback invoked when the user clicks the button.
 * @param modifier Modifier to be applied to the button.
 */
@Composable
fun FiltersButton(
    activeFilterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.testTag(FiltersButtonTestTags.BUTTON)
    ) {
        BadgedBox(
            badge = {
                if (activeFilterCount > 0) {
                    val badgeDescription = pluralStringResource(
                        R.plurals.collection_filters_active_description,
                        activeFilterCount,
                        activeFilterCount
                    )
                    Badge(modifier = Modifier.testTag(FiltersButtonTestTags.BADGE)) {
                        Text(
                            text = activeFilterCount.toString(),
                            modifier = Modifier.semantics { contentDescription = badgeDescription }
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = stringResource(R.string.collection_filters_description)
            )
        }
    }
}

// Testing & preview utilities

object FiltersButtonTestTags {
    const val BUTTON = "FiltersButton"
    const val BADGE = "FiltersButtonBadge"
}

@Suppress("Unused")
class FiltersButtonPreviews {

    @Preview(showBackground = true, name = "Without, with one and with several active filters")
    @Composable
    fun ActiveFilterCountsPreview() {
        DemoGameVaultTheme {
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(0, 1, 3).forEach { FiltersButton(activeFilterCount = it, onClick = {}) }
            }
        }
    }
}
