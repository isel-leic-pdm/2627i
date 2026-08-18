package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val GAME_LIST_RESULTS_TAG = "GameListResults"
internal const val GAME_LIST_HEADER_TAG = "GameListHeader"
internal const val SEARCH_BAR_TAG = "SearchBar"
internal const val PLATFORM_SELECTOR_TAG = "PlatformSelector"
internal const val SEARCHING_OVERLAY_TAG = "SearchingOverlay"

/**
 * A reusable Composable that displays a list of games resulting from a search.
 * It includes a search input field, a platform selector, a "Results" header and handles interaction gating.
 *
 * @param searchQuery The current search query.
 * @param onQueryChange The callback to be invoked when the search query changes.
 * @param selectedPlatform The currently selected platform.
 * @param onPlatformClick The callback to be invoked when the platform selector is clicked.
 * @param results The list of games to display.
 * @param onGameSelected The callback to be invoked when a game is selected.
 * @param modifier The modifier to be applied to the layout.
 * @param isSearching Whether a search is currently in progress.
 * @param isClickEnabled Whether clicks on list items are enabled.
 */
@Composable
fun GameListFromSearch(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedPlatform: String,
    onPlatformClick: () -> Unit,
    results: List<Game>,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier,
    isSearching: Boolean = false,
    isClickEnabled: Boolean = true,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag(SEARCH_BAR_TAG),
            placeholder = { Text(stringResource(R.string.search_game_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        // Platform Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onPlatformClick() }
                .testTag(PLATFORM_SELECTOR_TAG),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.platform_label),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = selectedPlatform,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }

        Spacer(modifier = Modifier.size(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Results Header
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.search_results_header),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 8.dp)
                            .testTag(GAME_LIST_HEADER_TAG)
                    )
                    HorizontalDivider()
                }

                // Results List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(GAME_LIST_RESULTS_TAG)
                ) {
                    items(results) { game ->
                        GameListItem(
                            game = game,
                            onGameSelected = {
                                if (isClickEnabled) {
                                    onGameSelected(game)
                                }
                            }
                        )
                    }
                }
            }

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .testTag(SEARCHING_OVERLAY_TAG),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameListFromSearchPreview() {
    MyGameVaultTheme {
        GameListFromSearch(
            searchQuery = "",
            onQueryChange = {},
            selectedPlatform = "PS5",
            onPlatformClick = {},
            results = listOf(
                Game(
                    1,
                    "Elden Ring",
                    LocalDate.of(2022, 2, 25),
                    "cache://er_cover",
                    "cache://er_thumb"
                ),
                Game(
                    2,
                    "The Legend of Zelda: TotK",
                    LocalDate.of(2023, 5, 12),
                    null as String?,
                    null
                )
            ),
            onGameSelected = {}
        )
    }
}
