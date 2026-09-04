package isel.dei.pdm.mygamevault.collection.details

import androidx.annotation.StringRes
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import java.time.Instant

/**
 * Represents the state of the Collection Entry screen.
 */
sealed interface CollectionEntryScreenState {
    /**
     * The screen is loading the game details.
     */
    data object Loading : CollectionEntryScreenState

    /**
     * The screen is displaying the game details.
     * @property entry The collection entry being displayed.
     * @property recoverableErrorMsgId An optional resource ID for a transient error.
     */
    data class Idle(
        val entry: CollectionEntry,
        @StringRes val recoverableErrorMsgId: Int? = null
    ) : CollectionEntryScreenState

    /**
     * The screen is in the logging state, meaning a play session is in progress.
     * @property entry The collection entry being displayed.
     * @property startTime The time when the play session started.
     * @property recoverableErrorMsgId An optional resource ID for a transient error.
     */
    data class Logging(
        val entry: CollectionEntry,
        val startTime: Instant,
        @StringRes val recoverableErrorMsgId: Int? = null
    ) : CollectionEntryScreenState

    /**
     * A fatal error occurred while fetching or updating the entry.
     * @property messageResId The resource ID for the error message.
     * @property canRetry Whether the operation can be retried.
     */
    data class FatalError(
        @StringRes val messageResId: Int,
        val canRetry: Boolean = false
    ) : CollectionEntryScreenState
}
