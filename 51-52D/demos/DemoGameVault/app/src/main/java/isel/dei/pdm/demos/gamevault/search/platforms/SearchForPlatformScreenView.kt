package isel.dei.pdm.demos.gamevault.search.platforms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.ui.common.SearchBox
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.PLATFORM_LIST_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItem
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The view for the "Find Platform" screen. Displays a top bar title, a search box,
 * and a lazy list of matching platforms.
 *
 * @param platforms The list of matching platforms to display.
 * @param onSearchQueryChange Callback invoked when the user enters or updates the search query.
 * @param onSearchQueryCleared Callback invoked when the user clears the search query.
 * @param onPlatformSelected Callback invoked when a platform item is selected.
 * @param onBack Optional callback invoked when the back navigation button is clicked.
 * @param modifier Modifier to be applied to the screen layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchForPlatformScreenView(
    platforms: List<PlatformSearchItemUiState>,
    onSearchQueryChange: (SearchString) -> Unit,
    onSearchQueryCleared: () -> Unit,
    onPlatformSelected: (PlatformInfo) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(SCREEN_TAG),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_platform_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(BACK_BUTTON_TAG)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            SearchBox(
                onQueryChange = onSearchQueryChange,
                onQueryCleared = onSearchQueryCleared
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.search_results_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            if (platforms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.search_no_platforms_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag(PLATFORM_LIST_TAG),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = platforms,
                        key = { it.platform.id }
                    ) { itemState ->
                        PlatformListItem(
                            item = itemState.toListItemModel(),
                            onSelected = { onPlatformSelected(itemState.platform) }
                        )
                    }
                }
            }
        }
    }
}

// Testing & preview utilities

object SearchForPlatformScreenViewTestTags {
    const val SCREEN_TAG = "SearchForPlatformScreen"
    const val PLATFORM_LIST_TAG = "SearchForPlatformList"
    const val BACK_BUTTON_TAG = "SearchForPlatformBackButton"
}

@Suppress("Unused")
class SearchForPlatformScreenViewPreviews {

    private val samplePlatforms = listOf(
        PlatformSearchItemUiState(
            PlatformInfo(1L, "PS5", "PlayStation 5", "https://fake.url/ps5.png"),
            addStatus = PlatformAddStatus.NOT_ADDED
        ),
        PlatformSearchItemUiState(
            PlatformInfo(2L, "XSX", "Xbox Series X/S", "https://fake.url/xbox.png"),
            addStatus = PlatformAddStatus.ADDING
        ),
        PlatformSearchItemUiState(
            PlatformInfo(3L, "NSW", "Nintendo Switch", "https://fake.url/switch.png"),
            addStatus = PlatformAddStatus.ADDED
        ),
        PlatformSearchItemUiState(
            PlatformInfo(4L, "PC", "PC (Microsoft Windows)", "https://fake.url/pc.png"),
            addStatus = PlatformAddStatus.NOT_ADDED
        )
    )

    @Preview(showBackground = true, name = "With Platforms")
    @Composable
    fun SearchForPlatformScreenViewPreview() {
        DemoGameVaultTheme {
            SearchForPlatformScreenView(
                platforms = samplePlatforms,
                onSearchQueryChange = {},
                onSearchQueryCleared = {},
                onPlatformSelected = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Empty List")
    @Composable
    fun SearchForPlatformScreenViewEmptyPreview() {
        DemoGameVaultTheme {
            SearchForPlatformScreenView(
                platforms = emptyList(),
                onSearchQueryChange = {},
                onSearchQueryCleared = {},
                onPlatformSelected = {},
                onBack = {}
            )
        }
    }
}
