package isel.dei.pdm.mygamevault.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.ports.ServiceUnavailableException
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val ADD_GAME_SCREEN_TAG = "AddGameScreen"
internal const val GAME_LIST_RESULTS_TAG = "GameListResults"
internal const val GAME_LIST_HEADER_TAG = "GameListHeader"
internal const val SEARCH_BAR_TAG = "SearchBar"
internal const val PLATFORM_SELECTOR_TAG = "PlatformSelector"
internal const val CATEGORY_SELECTOR_TAG = "CategorySelector"
internal const val SEARCHING_OVERLAY_TAG = "SearchingOverlay"
internal const val GAME_LIST_ITEM_TAG = "GameListItem"
internal const val GAME_NAME_TAG = "GameName"
internal const val GAME_RELEASE_DATE_TAG = "GameReleaseDate"
internal const val ADD_GAME_BUTTON_TAG = "AddGameButton"
internal const val VIEW_DETAILS_BUTTON_TAG = "ViewDetailsButton"

@Composable
fun AddGameScreenView(
    state: AddGameScreenState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onPlatformChange: (Platform) -> Unit,
    onCategoryChange: (Game.Category?) -> Unit,
    onAddRequested: (Game) -> Unit,
    onDetailsRequested: (Game) -> Unit,
    onRecoverableErrorConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    val recoverableErrorMsgId = state.recoverableErrorMsgId
    val transientErrorMessage = recoverableErrorMsgId?.let { stringResource(it) } ?: ""
    val transientErrorHint = stringResource(R.string.error_storage_transient_hint)

    LaunchedEffect(recoverableErrorMsgId) {
        if (recoverableErrorMsgId != null) {
            val result = snackbarHostState.showSnackbar(
                message = "$transientErrorMessage $transientErrorHint",
                actionLabel = "OK",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                onRecoverableErrorConsumed()
            }
        }
    }

    LaunchedEffect(state.isKeyboardVisible) {
        if (!state.isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(ADD_GAME_SCREEN_TAG),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            GameListFromSearch(
                searchQuery = searchQuery,
                onQueryChange = onQueryChange,
                selectedPlatform = state.selectedPlatform,
                onPlatformClick = { platformMenuExpanded = true },
                selectedCategory = state.selectedCategory,
                onCategoryClick = { categoryMenuExpanded = true },
                results = state.results,
                onAddRequested = onAddRequested,
                onDetailsRequested = onDetailsRequested,
                isSearching = state is AddGameScreenState.Searching,
                isClickEnabled = state.isClickDetectionEnabled,
                modifier = Modifier.fillMaxSize()
            )

            // Platform Dropdown
            DropdownMenu(
                expanded = platformMenuExpanded,
                onDismissRequest = { platformMenuExpanded = false }
            ) {
                Platforms.all.forEach { platform ->
                    DropdownMenuItem(
                        text = { Text(platform.name()) },
                        onClick = {
                            onPlatformChange(platform)
                            platformMenuExpanded = false
                        }
                    )
                }
            }

            // Category Dropdown
            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.search_category_all)) },
                    onClick = {
                        onCategoryChange(null)
                        categoryMenuExpanded = false
                    }
                )
                Game.Category.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategoryChange(category)
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * A Composable that displays a list of games resulting from a search.
 */
@Composable
private fun GameListFromSearch(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedPlatform: Platform,
    onPlatformClick: () -> Unit,
    selectedCategory: Game.Category?,
    onCategoryClick: () -> Unit,
    results: List<Game>,
    onAddRequested: (Game) -> Unit,
    onDetailsRequested: (Game) -> Unit,
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag(SEARCH_BAR_TAG),
            placeholder = { Text(stringResource(R.string.search_game_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium,
            singleLine = true
        )

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Platform Selector
            FilterChoice(
                label = stringResource(R.string.search_platform_label),
                value = selectedPlatform.name(),
                onClick = onPlatformClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag(PLATFORM_SELECTOR_TAG)
            )

            // Category Selector
            FilterChoice(
                label = stringResource(R.string.search_category_label),
                value = selectedCategory?.name ?: stringResource(R.string.search_category_all),
                onClick = onCategoryClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag(CATEGORY_SELECTOR_TAG)
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

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
                            platform = selectedPlatform,
                            onAddRequested = {
                                if (isClickEnabled) {
                                    onAddRequested(game)
                                }
                            },
                            onDetailsRequested = {
                                if (isClickEnabled) {
                                    onDetailsRequested(game)
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

@Composable
private fun FilterChoice(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
    }
}

private val sampleGames = listOf(
    Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co4jni.jpg"),
    Game(2, "The Legend of Zelda: Tears of the Kingdom", LocalDate.of(2023, 5, 12), "https://images.igdb.com/igdb/image/upload/t_cover_big/co5v6v.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co5v6v.jpg"),
    Game(3, "Baldur's Gate 3", LocalDate.of(2023, 8, 3), "https://images.igdb.com/igdb/image/upload/t_cover_big/co674h.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co674h.jpg"),
    Game(4, "Cyberpunk 2077", LocalDate.of(2020, 12, 10), "https://images.igdb.com/igdb/image/upload/t_cover_big/co2mjt.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co2mjt.jpg"),
    Game(5, "Hades", LocalDate.of(2020, 9, 17), "https://images.igdb.com/igdb/image/upload/t_cover_big/co26v5.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co26v5.jpg")
)

@Preview(showBackground = true, name = "Idle - Initial")
@Composable
fun AddGameScreenIdleInitialPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Idle(),
            searchQuery = "",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle - With Results")
@Composable
fun AddGameScreenIdleResultsPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Idle(sourceQuery = "Elden", results = sampleGames),
            searchQuery = "Elden",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Typing")
@Composable
fun AddGameScreenTypingPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Typing(
                results = sampleGames,
                selectedPlatform = Platforms.PS5,
                selectedCategory = null
            ),
            searchQuery = "Elden R",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Searching")
@Composable
fun AddGameScreenSearchingPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Searching(
                results = sampleGames,
                selectedPlatform = Platforms.PS5,
                selectedCategory = null
            ),
            searchQuery = "Elden Ring",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun AddGameScreenErrorPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Error(
                error = ServiceUnavailableException("Server is down"),
                previousResults = sampleGames,
                selectedPlatform = Platforms.PS5,
                selectedCategory = null
            ),
            searchQuery = "Elden Ring",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}

@Preview(showBackground = true, name = "Recoverable Error")
@Composable
fun AddGameScreenRecoverableErrorPreview() {
    MyGameVaultTheme {
        AddGameScreenView(
            state = AddGameScreenState.Idle(
                sourceQuery = "Elden",
                results = sampleGames,
                recoverableErrorMsgId = R.string.error_no_connectivity
            ),
            searchQuery = "Elden",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onAddRequested = {},
            onDetailsRequested = {},
            onRecoverableErrorConsumed = {}
        )
    }
}
