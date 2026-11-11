package isel.dei.pdm.demos.gamevault.search.games

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.ADD_PLATFORM_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.CATEGORY_FILTER_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.GAME_LIST_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.PLATFORM_FILTER_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenViewTestTags.SEARCH_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.common.SearchBox
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.time.LocalDate

/**
 * The view used to search for games.
 *
 * The filters only emit changes: selecting the already selected platform or category emits
 * nothing.
 *
 * @param state The current state of the screen.
 * @param onSearchQueryChange Callback invoked when the user enters or updates the search query.
 * @param onSearchQueryCleared Callback invoked when the user clears the search query.
 * @param onPlatformSelected Callback invoked when the user selects another platform to search on,
 * or null for all platforms.
 * @param onCategorySelected Callback invoked when the user selects another category, or null for
 * any category.
 * @param onAddGame Callback invoked when the user requests a game to be added to the collection,
 * on the platform of the selected item. Only items that are not added (nor being added) can be
 * selected.
 * @param onAddPlatform Callback invoked when the user requests to add a platform to the
 * collection, which is offered when there are none.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun SearchForGamesScreenView(
    state: SearchForGamesScreenState,
    onSearchQueryChange: (SearchString) -> Unit,
    onSearchQueryCleared: () -> Unit,
    onPlatformSelected: (Platform?) -> Unit,
    onCategorySelected: (GameInfo.Category?) -> Unit,
    onAddGame: (GameInfo, Platform) -> Unit,
    onAddPlatform: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = state.filtersOrNull
    Column(modifier = modifier.fillMaxSize()) {
        if (filters == null) {
            // Nothing is displayed while Loading, because reading the platforms is quick, nor in
            // PlatformsUnavailable, which is displayed by the screen, in place of this view
            if (state is SearchForGamesScreenState.NoPlatforms) {
                NoPlatforms(onAddPlatform = onAddPlatform)
            }
            return@Column
        }

        SearchBox(
            onQueryChange = onSearchQueryChange,
            onQueryCleared = onSearchQueryCleared,
            initialQuery = state.queryOrNull
        )

        SearchFilters(
            filters = filters,
            onPlatformSelected = onPlatformSelected,
            onCategorySelected = onCategorySelected
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (state) {
                is SearchForGamesScreenState.Start -> CenteredMessage(
                    text = stringResource(R.string.search_game_start_hint)
                )
                is SearchForGamesScreenState.Search ->
                    if (state.isIndicatorVisible) {
                        LabelledProgressIndicator(
                            text = stringResource(R.string.search_game_searching),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .testTag(SEARCH_INDICATOR_TAG)
                        )
                    }
                is SearchForGamesScreenState.Results -> SearchResults(
                    items = state.items,
                    onAddGame = onAddGame
                )
                // Displayed by the screen, in place of this view
                is SearchForGamesScreenState.Error -> Unit
                // Handled above, as they have no filters
                is SearchForGamesScreenState.Loading,
                is SearchForGamesScreenState.PlatformsUnavailable,
                is SearchForGamesScreenState.NoPlatforms -> Unit
            }
        }
    }
}

/**
 * The query of the state, if it has one.
 */
private val SearchForGamesScreenState.queryOrNull: SearchString?
    get() = when (this) {
        is SearchForGamesScreenState.Loading -> null
        is SearchForGamesScreenState.PlatformsUnavailable -> null
        is SearchForGamesScreenState.NoPlatforms -> null
        is SearchForGamesScreenState.Start -> null
        is SearchForGamesScreenState.Search -> query
        is SearchForGamesScreenState.Results -> query
        is SearchForGamesScreenState.Error -> query
    }

/**
 * The filters of the state, if it has them (i.e. if the search is possible).
 */
private val SearchForGamesScreenState.filtersOrNull: GameSearchFilters?
    get() = when (this) {
        is SearchForGamesScreenState.Loading -> null
        is SearchForGamesScreenState.PlatformsUnavailable -> null
        is SearchForGamesScreenState.NoPlatforms -> null
        is SearchForGamesScreenState.Start -> filters
        is SearchForGamesScreenState.Search -> filters
        is SearchForGamesScreenState.Results -> filters
        is SearchForGamesScreenState.Error -> filters
    }

/**
 * The platform and category filters, each one as a row of single selection chips.
 */
@Composable
private fun SearchFilters(
    filters: GameSearchFilters,
    onPlatformSelected: (Platform?) -> Unit,
    onCategorySelected: (GameInfo.Category?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FilterRow(
            label = stringResource(R.string.search_game_platform_filter_label),
            modifier = Modifier.testTag(PLATFORM_FILTER_TAG)
        ) {
            // null stands for all platforms, and comes first
            (listOf(null) + filters.platforms).forEach { platform ->
                val isSelected = platform == filters.selectedPlatform
                FilterChip(
                    selected = isSelected,
                    onClick = { if (!isSelected) onPlatformSelected(platform) },
                    label = {
                        Text(platform?.abbreviation?.value ?: stringResource(R.string.search_game_platform_all))
                    }
                )
            }
        }
        FilterRow(
            label = stringResource(R.string.search_game_category_filter_label),
            modifier = Modifier.testTag(CATEGORY_FILTER_TAG)
        ) {
            // null stands for any category, and comes first
            (listOf(null) + GameInfo.Category.entries).forEach { category ->
                val isSelected = category == filters.category
                FilterChip(
                    selected = isSelected,
                    onClick = { if (!isSelected) onCategorySelected(category) },
                    label = { Text(stringResource(category.chipLabelResId())) }
                )
            }
        }
    }
}

/**
 * A labelled row of filter chips, which scrolls horizontally if they do not fit.
 */
@Composable
private fun FilterRow(
    label: String,
    modifier: Modifier = Modifier,
    chips: @Composable RowScope.() -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = chips
    )
}

/**
 * The label of the category's chip, where null stands for any category.
 */
@StringRes
private fun GameInfo.Category?.chipLabelResId(): Int =
    this?.labelResId() ?: R.string.search_game_category_any

/**
 * The search results, preceded by a header. If there are no results, a message is displayed.
 */
@Composable
private fun SearchResults(
    items: List<GameSearchItemUiState>,
    onAddGame: (GameInfo, Platform) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.search_game_results_label),
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
            CenteredMessage(text = stringResource(R.string.search_no_games_found))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(GAME_LIST_TAG),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = items, key = { it.key }) { itemState ->
                    GameSearchItemView(
                        item = itemState,
                        onSelected = { onAddGame(itemState.game, itemState.platform) }
                    )
                }
            }
        }
    }
}

/**
 * The message displayed when the collection has no platforms, in the center of the available area,
 * followed by a button to add one.
 */
@Composable
private fun NoPlatforms(onAddPlatform: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.search_game_no_platforms),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddPlatform,
            modifier = Modifier.testTag(ADD_PLATFORM_BUTTON_TAG)
        ) {
            Text(stringResource(R.string.search_game_add_platform_button_label))
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

object SearchForGamesScreenViewTestTags {
    const val GAME_LIST_TAG = "SearchForGamesList"
    const val SEARCH_INDICATOR_TAG = "SearchForGamesSearchIndicator"
    const val PLATFORM_FILTER_TAG = "SearchForGamesPlatformFilter"
    const val CATEGORY_FILTER_TAG = "SearchForGamesCategoryFilter"
    const val ADD_PLATFORM_BUTTON_TAG = "SearchForGamesAddPlatformButton"
}

@Suppress("Unused")
class SearchForGamesScreenViewPreviews {

    private val query = SearchString("Final Fantasy VII")

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val pc = Platform(2L, "PC", "PC (Microsoft Windows)")
    private val switch = Platform(3L, "Switch", "Nintendo Switch")

    private val filters = GameSearchFilters(platforms = listOf(ps5, pc, switch))

    private val ff7 = GameInfo(
        1L, "Final Fantasy VII", LocalDate.of(1997, 1, 31), category = GameInfo.Category.MAIN_GAME
    )
    private val ff7Remake = GameInfo(
        2L, "Final Fantasy VII Remake", LocalDate.of(2020, 4, 10), category = GameInfo.Category.REMAKE
    )
    private val ff7Rebirth = GameInfo(
        3L, "Final Fantasy VII Rebirth", LocalDate.of(2024, 2, 29), category = GameInfo.Category.MAIN_GAME
    )

    // A game released for several of the user's platforms has one item per platform
    private val sampleItems = listOf(
        GameSearchItemUiState(ff7, switch, AddStatus.ADDED),
        GameSearchItemUiState(ff7Remake, ps5, AddStatus.ADDING),
        GameSearchItemUiState(ff7Remake, pc, AddStatus.NOT_ADDED),
        GameSearchItemUiState(ff7Rebirth, ps5, AddStatus.ADDED),
        GameSearchItemUiState(ff7Rebirth, pc, AddStatus.NOT_ADDED)
    )

    @Composable
    private fun PreviewOf(state: SearchForGamesScreenState) {
        DemoGameVaultTheme {
            SearchForGamesScreenView(
                state = state,
                onSearchQueryChange = {},
                onSearchQueryCleared = {},
                onPlatformSelected = {},
                onCategorySelected = {},
                onAddGame = { _, _ -> },
                onAddPlatform = {}
            )
        }
    }

    @Preview(showBackground = true, name = "No platforms")
    @Composable
    fun NoPlatformsPreview() = PreviewOf(SearchForGamesScreenState.NoPlatforms)

    @Preview(showBackground = true, name = "Start")
    @Composable
    fun StartPreview() = PreviewOf(SearchForGamesScreenState.Start(filters))

    @Preview(showBackground = true, name = "Search - With indicator")
    @Composable
    fun SearchPreview() =
        PreviewOf(SearchForGamesScreenState.Search(query, filters, isIndicatorVisible = true))

    @Preview(showBackground = true, name = "Results")
    @Composable
    fun ResultsPreview() = PreviewOf(SearchForGamesScreenState.Results(query, filters, sampleItems))

    @Preview(showBackground = true, name = "Results - Empty")
    @Composable
    fun EmptyResultsPreview() =
        PreviewOf(SearchForGamesScreenState.Results(query, filters, emptyList()))
}
