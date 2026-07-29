package isel.dei.pdm.puzzle

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The navigation keys for the application.
 */
@Serializable
sealed class AppNavigationKey : NavKey {
    /**
     * The start screen key.
     */
    @Serializable
    data object Start : AppNavigationKey()

    /**
     * The play screen key.
     */
    @Serializable
    data object Play : AppNavigationKey()

    /**
     * The about screen key.
     */
    @Serializable
    data object About : AppNavigationKey()
}
