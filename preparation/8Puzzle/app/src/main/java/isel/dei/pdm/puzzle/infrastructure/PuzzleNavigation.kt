package isel.dei.pdm.puzzle.infrastructure

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * The navigation keys for the application.
 */
@Serializable
sealed class PuzzleKey : NavKey {
    /**
     * The start screen key.
     */
    @Serializable
    data object Start : PuzzleKey()

    /**
     * The play screen key.
     */
    @Serializable
    data object Play : PuzzleKey()

    /**
     * The about screen key.
     */
    @Serializable
    data object About : PuzzleKey()
}
