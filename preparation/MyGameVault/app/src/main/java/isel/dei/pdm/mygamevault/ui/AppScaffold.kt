package isel.dei.pdm.mygamevault.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import isel.dei.pdm.mygamevault.add.AddGameScreen
import isel.dei.pdm.mygamevault.add.AddGameViewModel
import isel.dei.pdm.mygamevault.add.details.GameDetailsScreen
import isel.dei.pdm.mygamevault.collection.MyCollectionScreen
import isel.dei.pdm.mygamevault.collection.MyCollectionViewModel
import isel.dei.pdm.mygamevault.collection.details.CollectionEntryScreen
import isel.dei.pdm.mygamevault.collection.details.CollectionEntryViewModel
import isel.dei.pdm.mygamevault.preferences.PreferencesScreen
import isel.dei.pdm.mygamevault.preferences.PreferencesViewModel

private data class NavigationItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String,
)

private val navigationItems = listOf(
    NavigationItem(AppRoute.AddGame, Icons.Default.Add, "Add Game"),
    NavigationItem(AppRoute.MyCollection, Icons.AutoMirrored.Filled.LibraryBooks, "Collection"),
    NavigationItem(AppRoute.Preferences, Icons.Default.Settings, "Preferences"),
)

@Composable
fun AppScaffold(
    dependencies: isel.dei.pdm.mygamevault.DependenciesContainer,
    modifier: Modifier = Modifier
) {
    val navigationState = rememberNavigationState(
        startRoute = AppRoute.MyCollection,
        topLevelRoutes = navigationItems.asSequence().map { it.route }.toSet()
    )
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<AppRoute.MyCollection> {
            val viewModel: MyCollectionViewModel = viewModel(
                factory = MyCollectionViewModel.factory(dependencies.collectionRepository)
            )
            MyCollectionScreen(
                viewModel = viewModel,
                onEntrySelected = { entry ->
                    navigator.navigate(AppRoute.EntryDetails(entry.game.id, entry.platform.id))
                }
            )
        }
        entry<AppRoute.AddGame> {
            val viewModel: AddGameViewModel = viewModel(
                factory = AddGameViewModel.factory(dependencies.searchService, dependencies.collectionRepository)
            )
            AddGameScreen(
                viewModel = viewModel,
                onAddRequested = { game ->
                    viewModel.addGame(game, viewModel.selectedPlatform.value)
                    navigator.navigate(AppRoute.MyCollection)
                },
                onDetailsRequested = { game ->
                    navigator.navigate(AppRoute.GameDetails(game.id))
                }
            )
        }
        entry<AppRoute.Preferences> {
            val viewModel: PreferencesViewModel = viewModel(
                factory = PreferencesViewModel.factory(dependencies.secretsRepository)
            )
            PreferencesScreen(viewModel = viewModel)
        }
        entry<AppRoute.EntryDetails> { key ->
            val viewModel: CollectionEntryViewModel = viewModel(
                factory = CollectionEntryViewModel.factory(dependencies.collectionRepository)
            )
            CollectionEntryScreen(
                viewModel = viewModel,
                gameId = key.gameId,
                platformId = key.platformId,
                onBackRequested = { navigator.goBack() }
            )
        }
        entry<AppRoute.GameDetails> { key ->
            GameDetailsScreen(
                gameId = key.gameId,
                onBackRequested = { navigator.goBack() }
            )
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
