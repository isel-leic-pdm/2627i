package isel.dei.pdm.mygamevault.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import isel.dei.pdm.mygamevault.add.AddGameScreen
import isel.dei.pdm.mygamevault.add.AddGameViewModel
import isel.dei.pdm.mygamevault.collection.MyCollectionScreen
import isel.dei.pdm.mygamevault.collection.MyCollectionViewModel
import isel.dei.pdm.mygamevault.preferences.PreferencesScreen
import isel.dei.pdm.mygamevault.preferences.PreferencesViewModel

private data class NavigationItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String
)

private val navigationItems = listOf(
    NavigationItem(AppRoute.AddGame, Icons.Default.Add, "Add Game"),
    NavigationItem(AppRoute.MyCollection, Icons.AutoMirrored.Filled.LibraryBooks, "Collection"),
    NavigationItem(AppRoute.Preferences, Icons.Default.Settings, "Preferences")
)

@Composable
fun AppScaffold(
    addGameViewModel: AddGameViewModel,
    preferencesViewModel: PreferencesViewModel,
    myCollectionViewModel: MyCollectionViewModel,
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = AppRoute.MyCollection,
        topLevelRoutes = navigationItems.map { it.route }.toSet()
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<AppRoute.MyCollection> {
            val state by myCollectionViewModel.state.collectAsStateWithLifecycle()
            MyCollectionScreen(
                state = state,
                onEntrySelected = { entry ->
                    navigator.navigate(AppRoute.GameDetails(entry.game.id))
                },
                onFilterChange = myCollectionViewModel::onFilterChange
            )
        }
        entry<AppRoute.AddGame> {
            val state by addGameViewModel.state.collectAsStateWithLifecycle()
            val query by addGameViewModel.query.collectAsStateWithLifecycle()
            AddGameScreen(
                state = state,
                searchQuery = query,
                onQueryChange = addGameViewModel::onQueryChange,
                onPlatformChange = addGameViewModel::onPlatformChange,
                onCategoryChange = addGameViewModel::onCategoryChange,
                onAddRequested = { game ->
                    addGameViewModel.addGame(game, state.selectedPlatform)
                    navigator.navigate(AppRoute.MyCollection)
                },
                onDetailsRequested = { game ->
                    navigator.navigate(AppRoute.GameDetails(game.id))
                }
            )
        }
        entry<AppRoute.Preferences> {
            val state by preferencesViewModel.state.collectAsStateWithLifecycle()
            PreferencesScreen(
                state = state,
                onClientIdChange = preferencesViewModel::onClientIdChange,
                onClientSecretChange = preferencesViewModel::onClientSecretChange,
                onSave = preferencesViewModel::onSave
            )
        }
        entry<AppRoute.GameDetails> { key ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Game Details for ID: ${key.gameId}",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                navigationItems.forEach { item ->
                    val isSelected = navigationState.topLevelRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navigator.navigate(item.route) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavDisplay(
                entries = navigationState.toDecoratedEntries(entryProvider),
                onBack = { navigator.goBack() }
            )
        }
    }
}
