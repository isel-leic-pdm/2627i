package isel.dei.pdm.mygamevault.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.ports.NoConnectivityException
import isel.dei.pdm.mygamevault.ports.RateLimitExceededException
import isel.dei.pdm.mygamevault.ports.ServiceUnavailableException
import isel.dei.pdm.mygamevault.ui.common.GameListFromSearch
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val ADD_GAME_SCREEN_TAG = "AddGameScreen"

/**
 * The "Add Game" screen where users can search for games to add to their collection.
 */
@Composable
fun AddGameScreen(
    state: AddGameScreenState,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onPlatformChange: (Platform) -> Unit,
    onCategoryChange: (Game.Category?) -> Unit,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var platformMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.isKeyboardVisible) {
        if (!state.isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    val errorMessage = if (state is AddGameScreenState.Error) {
        when (state.error) {
            is NoConnectivityException -> stringResource(R.string.error_no_connectivity)
            is ServiceUnavailableException -> stringResource(R.string.error_service_unavailable)
            is RateLimitExceededException -> stringResource(R.string.error_rate_limit)
            else -> stringResource(R.string.error_generic)
        }
    } else null

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(ADD_GAME_SCREEN_TAG),
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                onGameSelected = onGameSelected,
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
                        text = { Text(platform.name.value) },
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
        AddGameScreen(
            state = AddGameScreenState.Idle(),
            searchQuery = "",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onGameSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Idle - With Results")
@Composable
fun AddGameScreenIdleResultsPreview() {
    MyGameVaultTheme {
        AddGameScreen(
            state = AddGameScreenState.Idle(sourceQuery = "Elden", results = sampleGames),
            searchQuery = "Elden",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onGameSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Typing")
@Composable
fun AddGameScreenTypingPreview() {
    MyGameVaultTheme {
        AddGameScreen(
            state = AddGameScreenState.Typing(
                results = sampleGames,
                selectedPlatform = Platforms.PS5,
                selectedCategory = null
            ),
            searchQuery = "Elden R",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onGameSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Searching")
@Composable
fun AddGameScreenSearchingPreview() {
    MyGameVaultTheme {
        AddGameScreen(
            state = AddGameScreenState.Searching(
                results = sampleGames,
                selectedPlatform = Platforms.PS5,
                selectedCategory = null
            ),
            searchQuery = "Elden Ring",
            onQueryChange = {},
            onPlatformChange = {},
            onCategoryChange = {},
            onGameSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
fun AddGameScreenErrorPreview() {
    MyGameVaultTheme {
        AddGameScreen(
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
            onGameSelected = {}
        )
    }
}
