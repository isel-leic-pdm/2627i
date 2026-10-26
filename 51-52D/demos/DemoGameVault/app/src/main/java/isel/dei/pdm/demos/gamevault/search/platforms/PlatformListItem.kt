package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImage
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * Composable used to display information of a particular platform inside a list of platforms.
 * @param platform The platform to be displayed.
 * @param modifier The modifier to be applied to the layout.
 * @param onSelected Callback to be invoked when the item is selected by the user. If null the
 * item will not be clickable.
 */
@Composable
fun PlatformListItem(
    platform: PlatformInfo,
    modifier: Modifier = Modifier,
    onSelected: (platform: PlatformInfo) -> Unit,
    trailingContent: @Composable (() -> Unit)? = {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
        )
    },
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelected(platform) }
            .testTag(PlatformListItemTestTags.ITEM),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = platform.name.value,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.testTag(PlatformListItemTestTags.NAME),
                )
            },
            supportingContent = {
                Text(
                    text = platform.abbreviation.value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag(PlatformListItemTestTags.ABBREVIATION),
                )
            },
            leadingContent = {
                AsyncImage(
                    uri = platform.logoUri?.toString()?.toUri(),
                    contentDescription = platform.name.value,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag(PlatformListItemTestTags.LOGO),
                )
            },
            trailingContent = trailingContent,
        )
    }
}

// Testing & preview utilities

object PlatformListItemTestTags {
    const val ITEM = "platform_list_item"
    const val LOGO = "platform_logo"
    const val NAME = "platform_name"
    const val ABBREVIATION = "platform_abbreviation"
}

@Suppress("Unused")
class PlatformListItemPreviews {

    @Preview(showBackground = true)
    @Composable
    fun WithIcon() {
        val platform = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "http://example.com/ps5.png",
        )
        DemoGameVaultTheme {
            PlatformListItem(
                platform = platform,
                onSelected = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun NoIcon() {
        val platform = PlatformInfo(
            id = 1L,
            abbreviation = "PS5",
            name = "PlayStation 5",
            logoUri = "http://example.com/ps5.png",
        )
        DemoGameVaultTheme {
            PlatformListItem(
                platform = platform,
                onSelected = {},
                trailingContent = null,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
