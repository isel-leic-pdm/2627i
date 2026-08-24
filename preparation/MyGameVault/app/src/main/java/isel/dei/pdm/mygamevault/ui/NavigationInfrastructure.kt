package isel.dei.pdm.mygamevault.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(private val navigationState: NavigationState) {
    fun navigate(route: AppRoute) {
        if (route in navigationState.backStacks.keys) {
            // This is a top level route, just switch to it
            navigationState.topLevelRoute = route
        } else {
            navigationState.backStacks[navigationState.topLevelRoute]?.add(route)
        }
    }

    fun goBack() {
        val currentStack = navigationState.backStacks[navigationState.topLevelRoute] ?:
            error("Stack for ${navigationState.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == navigationState.topLevelRoute) {
            if (navigationState.topLevelRoute != navigationState.startRoute) {
                navigationState.topLevelRoute = navigationState.startRoute
            }
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
fun rememberNavigationState(
    startRoute: AppRoute,
    topLevelRoutes: Set<AppRoute>
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    // Create a back stack for each top level route.
    val backStacks = topLevelRoutes.associateWith { key -> 
        rememberNavBackStack(key) 
    }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRouteState = topLevelRoute,
            backStacks = backStacks
        )
    }
}

/**
 * State holder for navigation state.
 */
class NavigationState(
    val startRoute: AppRoute,
    topLevelRouteState: MutableState<AppRoute>,
    val backStacks: Map<AppRoute, NavBackStack<NavKey>>
) {
    var topLevelRoute: AppRoute by topLevelRouteState

    /**
     * Convert the navigation state into decorated entries.
     */
    @Composable
    fun toDecoratedEntries(
        entryProvider: (AppRoute) -> NavEntry<AppRoute>
    ): List<NavEntry<NavKey>> {
        @Suppress("UNCHECKED_CAST")
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider as (NavKey) -> NavEntry<NavKey>
            )
        }

        return getTopLevelRoutesInUse().flatMap { decoratedEntries[it] ?: emptyList() }
    }

    private fun getTopLevelRoutesInUse(): List<AppRoute> =
        if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}
