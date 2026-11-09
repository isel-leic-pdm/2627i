package isel.dei.pdm.demos.gamevault.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The composable function that defines the home screen: the app bar, with access to the settings
 * and to secondary features, and the home view contents.
 *
 * DRAFT: The callbacks of screens that do not exist yet default to no-ops.
 *
 * @param playing The items of the collection entries currently being played.
 * @param stats The summary of the user's accomplishments.
 * @param onOpenSettings The callback to be invoked when the user requests the settings.
 * @param onOpenPlatforms The callback to be invoked when the user requests the platforms collection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    playing: List<CollectionItem>,
    stats: HomeStats,
    onOpenSettings: () -> Unit,
    onOpenPlatforms: () -> Unit,
    onAddGame: () -> Unit = { },
    onGameSelected: (CollectionItem) -> Unit = { },
    onFilterSelected: (CollectionFilter) -> Unit = { },
    onBrowseAll: () -> Unit = { }
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.home_settings_description)
                        )
                    }
                    OverflowMenu(onOpenPlatforms = onOpenPlatforms)
                }
            )
        }
    ) { innerPadding ->
        HomeScreenView(
            playing = playing,
            stats = stats,
            onGameSelected = onGameSelected,
            onFilterSelected = onFilterSelected,
            onBrowseAll = onBrowseAll,
            onAddGame = onAddGame,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * The app bar's overflow menu, with access to secondary features.
 */
@Composable
private fun OverflowMenu(onOpenPlatforms: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.home_more_options_description)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_platforms_menu_label)) },
                onClick = {
                    expanded = false
                    onOpenPlatforms()
                }
            )
        }
    }
}

// Preview utilities

@Suppress("Unused")
class HomeScreenPreviews {

    @Preview(showBackground = true, showSystemUi = true, name = "Home screen")
    @Composable
    fun HomeScreenPreview() {
        DemoGameVaultTheme {
            HomeScreen(
                playing = HomeScreenSampleData.playing,
                stats = HomeScreenSampleData.stats,
                onOpenSettings = {},
                onOpenPlatforms = {}
            )
        }
    }
}
