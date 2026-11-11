package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADDED_ICON
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADDING_INDICATOR
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicatorTestTags.ADD_ICON
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Represents the status of adding a search result to the user's collection.
 */
enum class AddStatus {
    NOT_ADDED,
    ADDING,
    ADDED
}

/**
 * The indicator of a search result's addition status, displayed at the end of the search results'
 * items: a plus sign while it can be added, a progress indicator while it is being added, or a
 * check mark once added.
 *
 * The indicator is not interactive: the item that displays it must handle the selection (e.g. by
 * being clickable as a whole while not added). That makes the whole item the touch target, and
 * the indicator's description is read as part of the item by accessibility services.
 *
 * @param status The addition status to be displayed.
 * @param addDescription The accessibility description of the plus sign (e.g. "Add game").
 * @param addedDescription The accessibility description of the check mark (e.g. "Game in
 * collection").
 * @param modifier The modifier to be applied to the indicator.
 */
@Composable
fun AddStatusIndicator(
    status: AddStatus,
    addDescription: String,
    addedDescription: String,
    modifier: Modifier = Modifier
) {
    when (status) {
        AddStatus.NOT_ADDED -> Icon(
            imageVector = Icons.Default.Add,
            contentDescription = addDescription,
            modifier = modifier.testTag(ADD_ICON)
        )
        AddStatus.ADDING -> CircularProgressIndicator(
            modifier = modifier
                .size(24.dp)
                .testTag(ADDING_INDICATOR),
            strokeWidth = 2.dp
        )
        AddStatus.ADDED -> Icon(
            imageVector = Icons.Default.Check,
            contentDescription = addedDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = modifier.testTag(ADDED_ICON)
        )
    }
}

// Testing & preview utilities

object AddStatusIndicatorTestTags {
    const val ADD_ICON = "AddStatusAddIcon"
    const val ADDING_INDICATOR = "AddStatusAddingIndicator"
    const val ADDED_ICON = "AddStatusAddedIcon"
}

@Suppress("Unused")
class AddStatusIndicatorPreviews {

    @Preview(showBackground = true, name = "All statuses")
    @Composable
    fun AllStatusesPreview() {
        DemoGameVaultTheme {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                AddStatus.entries.forEach {
                    AddStatusIndicator(
                        status = it,
                        addDescription = "Add",
                        addedDescription = "In collection"
                    )
                }
            }
        }
    }
}
