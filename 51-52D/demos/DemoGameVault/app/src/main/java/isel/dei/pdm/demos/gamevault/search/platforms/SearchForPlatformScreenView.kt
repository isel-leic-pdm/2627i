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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.PLATFORM_LIST_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenViewTestTags.SEARCH_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItemView
import isel.dei.pdm.demos.gamevault.ui.common.SearchBox
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The view used to search for platforms.
 *
 * @param state The current state of the screen.
 * @param onSearchQueryChange Callback invoked when the user enters or updates the search query.
 * @param onSearchQueryCleared Callback invoked when the user clears the search query.
 * @param onAddPlatform Callback invoked when the user requests a platform to be added to the
 * collection. Only platforms that are not added (nor being added) can be selected.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun SearchForPlatformScreenView(
    state: SearchForPlatformScreenState,
    onSearchQueryChange: (SearchString) -> Unit,
    onSearchQueryCleared: () -> Unit,
    onAddPlatform: (PlatformInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        SearchBox(
            onQueryChange = onSearchQueryChange,
            onQueryCleared = onSearchQueryCleared,
            initialQuery = state.queryOrNull
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (state) {
                is SearchForPlatformScreenState.Start -> CenteredMessage(
                    text = stringResource(R.string.search_platform_start_hint)
                )
                is SearchForPlatformScreenState.Search ->
                    if (state.isIndicatorVisible) {
                        LabelledProgressIndicator(
                            text = stringResource(R.string.search_platform_searching),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag(SEARCH_INDICATOR_TAG)
                        )
                    }
                is SearchForPlatformScreenState.Results -> SearchResults(
                    items = state.items,
                    onAddPlatform = onAddPlatform
                )
                // Displayed by the screen, in place of this view
                is SearchForPlatformScreenState.Error -> Unit
            }
        }
    }
}

/**
 * The query of the state, if it has one.
 */
private val SearchForPlatformScreenState.queryOrNull: SearchString?
    get() = when (this) {
        is SearchForPlatformScreenState.Start -> null
        is SearchForPlatformScreenState.Search -> query
        is SearchForPlatformScreenState.Results -> query
        is SearchForPlatformScreenState.Error -> query
    }

/**
 * The search results, preceded by a header. If there are no results, a message is displayed.
 */
@Composable
private fun SearchResults(
    items: List<PlatformSearchItemUiState>,
    onAddPlatform: (PlatformInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
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

        if (items.isEmpty()) {
            CenteredMessage(text = stringResource(R.string.search_no_platforms_found))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(PLATFORM_LIST_TAG),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = items, key = { it.key }) { itemState ->
                    PlatformListItemView(
                        item = itemState.toListItemModel(),
                        onSelected = { onAddPlatform(itemState.platform) }
                    )
                }
            }
        }
    }
}

/**
 * A message displayed in the center of the available area.
 */
@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Testing & preview utilities

object SearchForPlatformScreenViewTestTags {
    const val PLATFORM_LIST_TAG = "SearchForPlatformList"
    const val SEARCH_INDICATOR_TAG = "SearchForPlatformSearchIndicator"
}

@Suppress("Unused")
class SearchForPlatformScreenViewPreviews {

    private val query = SearchString("PlayStation")

    private val sampleItems = listOf(
        PlatformSearchItemUiState(
            PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Standard"),
            addStatus = AddStatus.NOT_ADDED
        ),
        PlatformSearchItemUiState(
            PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", "Pro"),
            addStatus = AddStatus.ADDED
        ),
        PlatformSearchItemUiState(
            PlatformInfo(4L, "PS4", "PlayStation 4", "https://fake.url/ps4.png"),
            addStatus = AddStatus.ADDING
        )
    )

    @Composable
    private fun PreviewOf(state: SearchForPlatformScreenState) {
        DemoGameVaultTheme {
            SearchForPlatformScreenView(
                state = state,
                onSearchQueryChange = {},
                onSearchQueryCleared = {},
                onAddPlatform = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Start")
    @Composable
    fun StartPreview() = PreviewOf(SearchForPlatformScreenState.Start)

    @Preview(showBackground = true, name = "Search - With indicator")
    @Composable
    fun SearchPreview() =
        PreviewOf(SearchForPlatformScreenState.Search(query, isIndicatorVisible = true))

    @Preview(showBackground = true, name = "Results")
    @Composable
    fun ResultsPreview() = PreviewOf(SearchForPlatformScreenState.Results(query, sampleItems))

    @Preview(showBackground = true, name = "Results - Empty")
    @Composable
    fun EmptyResultsPreview() = PreviewOf(SearchForPlatformScreenState.Results(query, emptyList()))
}
