package isel.dei.pdm.mygamevault.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable
    data object MyCollection : AppRoute
    
    @Serializable
    data object AddGame : AppRoute
    
    @Serializable
    data object Preferences : AppRoute

    @Serializable
    data class GameDetails(val gameId: Long) : AppRoute
}
