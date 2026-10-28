package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.Platform
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.net.URI

/**
 * Composable used to display information of a particular platform inside a list of platforms.
 * Encapsulates mode-specific interactions, styling, and trailing indicators.
 *
 * @param item The platform list item model containing data and display mode.
 * @param onSelected Callback to be invoked when the item is selected.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun PlatformListItem(
    item: PlatformListItemModel,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClickable = item.mode == PlatformListItemMode.ADDABLE || item.mode == PlatformListItemMode.NAVIGABLE

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable(onClick = onSelected) else Modifier)
            .testTag(PlatformListItemTestTags.ITEM),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = item.name.value,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(PlatformListItemTestTags.NAME),
                )
            },
            supportingContent = {
                Text(
                    text = item.abbreviation.value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(PlatformListItemTestTags.ABBREVIATION),
                )
            },
            leadingContent = {
                AsyncImage(
                    uri = item.logoUri?.toString()?.toUri(),
                    contentDescription = item.name.value,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag(PlatformListItemTestTags.LOGO),
                )
            },
            trailingContent = {
                when (item.mode) {
                    PlatformListItemMode.ADDABLE -> {
                        IconButton(
                            onClick = onSelected,
                            modifier = Modifier.testTag(PlatformListItemTestTags.ADD_BUTTON)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.search_platform_add_description),
                            )
                        }
                    }
                    PlatformListItemMode.ADDING -> {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .testTag(PlatformListItemTestTags.ADDING_INDICATOR),
                            strokeWidth = 2.dp
                        )
                    }
                    PlatformListItemMode.ADDED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.search_platform_added_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag(PlatformListItemTestTags.ADDED_ICON)
                        )
                    }
                    PlatformListItemMode.NAVIGABLE -> {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.testTag(PlatformListItemTestTags.CHEVRON_ICON)
                        )
                    }
                }
            },
        )
    }
}

/**
 * Represents the presentation mode of a [PlatformListItem].
 */
enum class PlatformListItemMode {
    ADDABLE,    // Item can be added to the collection
    ADDING,     // Item is currently being added to the collection
    ADDED,      // Item has been added to the collection
    NAVIGABLE   // Item can be navigated to a detailed view
}

/**
 * UI display model representing a platform item in the several lists used in the application.
 * @property id The platform's unique identifier. It is used to distinguish between different platforms.
 * @property abbreviation The platform's abbreviation.
 * @property name The platform's name.
 * @property logoUri The platform's logo URI.
 * @property mode The presentation mode.
 */
data class PlatformListItemModel(
    val id: Long,
    val abbreviation: NonBlankString,
    val name: NonBlankString,
    val logoUri: URI?,
    val mode: PlatformListItemMode = PlatformListItemMode.ADDABLE
)

/**
 * Extension function to convert a [PlatformInfo] to a [PlatformListItemModel].
 */
fun PlatformInfo.toListItemModel(mode: PlatformListItemMode = PlatformListItemMode.ADDABLE) =
    PlatformListItemModel(
        id = id,
        abbreviation = abbreviation,
        name = name,
        logoUri = logoUri,
        mode = mode
    )

/**
 * Extension function to convert a [Platform] to a [PlatformListItemModel].
 */
fun Platform.toListItemModel(mode: PlatformListItemMode = PlatformListItemMode.NAVIGABLE) =
    PlatformListItemModel(
        id = id,
        abbreviation = abbreviation,
        name = name,
        logoUri = logoUri,
        mode = mode
    )


// Testing & preview utilities

object PlatformListItemTestTags {
    const val ITEM = "PlatformListItem"
    const val LOGO = "PlatformLogo"
    const val NAME = "PlatformName"
    const val ABBREVIATION = "PlatformAbbreviation"
    const val ADD_BUTTON = "PlatformAddButton"
    const val ADDING_INDICATOR = "PlatformAddingIndicator"
    const val ADDED_ICON = "PlatformAddedIcon"
    const val CHEVRON_ICON = "PlatformChevronIcon"
}

@Suppress("Unused")
class PlatformListItemPreviews {

    private val platformModel = PlatformListItemModel(
        id = 1L,
        abbreviation = NonBlankString("PS5"),
        name = NonBlankString("PlayStation 5"),
        logoUri = URI.create("http://example.com/ps5.png"),
        mode = PlatformListItemMode.ADDABLE
    )

    @Preview(showBackground = true, name = "Addable")
    @Composable
    fun AddablePreview() {
        DemoGameVaultTheme {
            PlatformListItem(
                item = platformModel,
                onSelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Preview(showBackground = true, name = "Adding")
    @Composable
    fun AddingPreview() {
        DemoGameVaultTheme {
            PlatformListItem(
                item = platformModel.copy(mode = PlatformListItemMode.ADDING),
                onSelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Preview(showBackground = true, name = "Added")
    @Composable
    fun AddedPreview() {
        DemoGameVaultTheme {
            PlatformListItem(
                item = platformModel.copy(mode = PlatformListItemMode.ADDED),
                onSelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Preview(showBackground = true, name = "Navigable")
    @Composable
    fun NavigablePreview() {
        DemoGameVaultTheme {
            PlatformListItem(
                item = platformModel.copy(mode = PlatformListItemMode.NAVIGABLE),
                onSelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
