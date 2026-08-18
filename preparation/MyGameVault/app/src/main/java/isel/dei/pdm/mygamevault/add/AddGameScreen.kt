package isel.dei.pdm.mygamevault.add

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.mygamevault.core.Game
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
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isKeyboardVisible) {
        if (!state.isKeyboardVisible) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(ADD_GAME_SCREEN_TAG)
    ) { innerPadding ->
        GameListFromSearch(
            searchQuery = searchQuery,
            onQueryChange = onQueryChange,
            selectedPlatform = "PS5", // Placeholder
            onPlatformClick = { }, // Placeholder
            results = state.results,
            onGameSelected = onGameSelected,
            isSearching = state is AddGameScreenState.Searching,
            isClickEnabled = state.isClickDetectionEnabled,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

private val sampleGames = listOf(
    Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co4jni.jpg"),
    Game(2, "The Legend of Zelda: Tears of the Kingdom", LocalDate.of(2023, 5, 12), "https://images.igdb.com/igdb/image/upload/t_cover_big/co5v6v.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co5v6v.jpg"),
    Game(3, "Baldur's Gate 3", LocalDate.of(2023, 8, 3), "https://images.igdb.com/igdb/image/upload/t_cover_big/co674h.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co674h.jpg"),
    Game(4, "Cyberpunk 2077", LocalDate.of(2020, 12, 10), "https://images.igdb.com/igdb/image/upload/t_cover_big/co2mjt.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co2mjt.jpg"),
    Game(5, "Hades", LocalDate.of(2020, 9, 17), "https://images.igdb.com/igdb/image/upload/t_cover_big/co26v5.jpg", "https://images.igdb.com/igdb/image/upload/t_thumb/co26v5.jpg")
)

@Preview(showBackground = true)
@Composable
fun AddGameScreenPreview() {
    MyGameVaultTheme {
        AddGameScreen(
            state = AddGameScreenState.Idle(sampleGames),
            searchQuery = "",
            onQueryChange = {},
            onGameSelected = {}
        )
    }
}
