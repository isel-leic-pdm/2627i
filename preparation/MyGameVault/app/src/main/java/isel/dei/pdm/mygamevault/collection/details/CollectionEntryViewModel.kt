package isel.dei.pdm.mygamevault.collection.details

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import isel.dei.pdm.mygamevault.MyGameVaultApplication
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.PlayTime
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import isel.dei.pdm.mygamevault.ports.RecoverablePersistenceException
import isel.dei.pdm.mygamevault.ports.UnrecoverablePersistenceException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Game Details screen.
 * It manages the screen's state machine, handling data fetching, status updates,
 * and play time logging.
 */
class CollectionEntryViewModel(
    private val repository: CollectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow<CollectionEntryScreenState>(CollectionEntryScreenState.Loading)
    val state: StateFlow<CollectionEntryScreenState> = _state.asStateFlow()

    /**
     * Fetches the details for the given game and platform.
     * Transitions the state to Loading and then to Idle upon success.
     */
    fun fetchEntryDetails(gameId: Long, platformId: Long) {
        Log.d(TAG, "fetchEntryDetails: gameId = $gameId, platformId = $platformId")
        _state.value = CollectionEntryScreenState.Loading
        viewModelScope.launch {
            val platform = Platforms.all.find { it.id == platformId }
            if (platform == null) {
                Log.e(TAG, "fetchEntryDetails: platform with id $platformId not found")
                return@launch
            }
            try {
                val entry = repository.get(gameId, platform)
                if (entry != null) {
                    _state.value = if (entry.sessionStartTime != null) {
                        CollectionEntryScreenState.Logging(entry)
                    } else {
                        CollectionEntryScreenState.Idle(entry)
                    }
                } else {
                    Log.e(TAG, "fetchEntryDetails: entry not found")
                    _state.value = CollectionEntryScreenState.FatalError(
                        messageResId = R.string.error_generic,
                        canRetry = false
                    )
                }
            } catch (e: UnrecoverablePersistenceException) {
                Log.e(TAG, "fetchEntryDetails: fatal error", e)
                _state.value = CollectionEntryScreenState.FatalError(
                    messageResId = R.string.error_storage_access,
                    canRetry = false
                )
            } catch (e: RecoverablePersistenceException) {
                Log.e(TAG, "fetchEntryDetails: recoverable error", e)
                _state.value = CollectionEntryScreenState.FatalError(
                    messageResId = R.string.error_storage_load_failed,
                    canRetry = true
                )
            } catch (e: Exception) {
                Log.e(TAG, "fetchEntryDetails: unexpected error", e)
                _state.value = CollectionEntryScreenState.FatalError(
                    messageResId = R.string.error_generic,
                    canRetry = false
                )
            }
        }
    }

    /**
     * Updates the playing status of the game.
     * If the status is set to FINISHED or PLATINUM and completed runs is 0,
     * it automatically increments the runs to 1 to satisfy domain invariants.
     */
    fun updateStatus(newState: PlayStatus.State) {
        val currentState = _state.value
        val entry = when (currentState) {
            is CollectionEntryScreenState.Idle -> currentState.entry
            is CollectionEntryScreenState.Logging -> currentState.entry
            else -> null
        }

        entry?.let {
            viewModelScope.launch {
                try {
                    if (currentState is CollectionEntryScreenState.Logging) {
                        repository.stopSession()
                    }
                    val currentEntry = repository.get(it.game.id, it.platform) ?: it
                    saveEntry(currentEntry.updateStatus(newState))
                } catch (e: Exception) {
                    Log.e(TAG, "updateStatus: error", e)
                }
            }
        }
    }

    /**
     * Removes the game from the user's collection.
     */
    fun removeGame(onSuccess: () -> Unit) {
        val entry = when (val s = _state.value) {
            is CollectionEntryScreenState.Idle -> s.entry
            is CollectionEntryScreenState.Logging -> s.entry
            else -> null
        }
        
        if (entry != null) {
            viewModelScope.launch {
                try {
                    repository.delete(entry.game.id, entry.platform)
                    onSuccess()
                } catch (e: UnrecoverablePersistenceException) {
                    Log.e(TAG, "removeGame: fatal error", e)
                    _state.value = CollectionEntryScreenState.FatalError(R.string.error_storage_access)
                } catch (e: RecoverablePersistenceException) {
                    Log.e(TAG, "removeGame: recoverable error", e)
                    val current = _state.value
                    if (current is CollectionEntryScreenState.Idle) {
                        _state.value = current.copy(recoverableErrorMsgId = R.string.error_storage_delete_failed)
                    } else if (current is CollectionEntryScreenState.Logging) {
                        _state.value = current.copy(recoverableErrorMsgId = R.string.error_storage_delete_failed)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "removeGame: unexpected error", e)
                    _state.value = CollectionEntryScreenState.FatalError(R.string.error_generic)
                }
            }
        }
    }

    /**
     * Starts logging play time.
     * Transitions from Idle to Logging if the current status is PLAYING.
     */
    fun startLogging() {
        val currentState = _state.value
        if (currentState is CollectionEntryScreenState.Idle) {
            Log.d(TAG, "startLogging: starting session")
            val entry = currentState.entry
            viewModelScope.launch {
                try {
                    if (entry.playStatus.state != PlayStatus.State.PLAYING) {
                        repository.save(entry.updateStatus(PlayStatus.State.PLAYING))
                    }
                    repository.startSession(entry.game.id, entry.platform.id)
                    fetchEntryDetails(entry.game.id, entry.platform.id)
                } catch (e: Exception) {
                    Log.e(TAG, "startLogging: error", e)
                }
            }
        }
    }

    /**
     * Stops logging play time.
     * Calculates the elapsed duration, adds it to the entry's play time, and saves it.
     * Transitions from Logging back to Idle.
     */
    fun stopLogging() {
        val currentState = _state.value
        if (currentState is CollectionEntryScreenState.Logging) {
            Log.d(TAG, "stopLogging: stopping session")
            val entry = currentState.entry
            viewModelScope.launch {
                try {
                    repository.stopSession()
                    fetchEntryDetails(entry.game.id, entry.platform.id)
                } catch (e: Exception) {
                    Log.e(TAG, "stopLogging: error", e)
                }
            }
        }
    }

    /**
     * Updates the number of hours spent playing the game.
     * Minutes are reset to 0 when hours are manually edited.
     */
    fun updateHours(hours: Long) {
        val currentState = _state.value
        if (currentState is CollectionEntryScreenState.Idle) {
            val entry = currentState.entry
            val updatedStatus = entry.playStatus.copy(
                timeSpent = PlayTime(hours = hours.toInt(), minutes = 0)
            )
            saveEntry(entry.copy(playStatus = updatedStatus))
        }
    }

    /**
     * Adds a new completed run to the play status.
     */
    fun incrementRuns() {
        val currentState = _state.value
        if (currentState is CollectionEntryScreenState.Idle) {
            saveEntry(currentState.entry.addRun())
        }
    }

    private fun saveEntry(entry: CollectionEntry) {
        viewModelScope.launch {
            try {
                repository.save(entry)
                val current = _state.value
                if (current is CollectionEntryScreenState.Idle && current.entry.game.id == entry.game.id) {
                    _state.value = CollectionEntryScreenState.Idle(entry)
                } else if (current is CollectionEntryScreenState.Logging && current.entry.game.id == entry.game.id) {
                    _state.value = current.copy(entry = entry)
                }
            } catch (e: RecoverablePersistenceException) {
                Log.e(TAG, "saveEntry: recoverable error", e)
                val current = _state.value
                if (current is CollectionEntryScreenState.Idle) {
                    _state.value = current.copy(recoverableErrorMsgId = R.string.error_storage_save_failed)
                } else if (current is CollectionEntryScreenState.Logging) {
                    _state.value = current.copy(recoverableErrorMsgId = R.string.error_storage_save_failed)
                }
            } catch (e: UnrecoverablePersistenceException) {
                Log.e(TAG, "saveEntry: fatal error", e)
                _state.value = CollectionEntryScreenState.FatalError(R.string.error_storage_access)
            } catch (e: Exception) {
                Log.e(TAG, "saveEntry: unexpected error", e)
                _state.value = CollectionEntryScreenState.FatalError(R.string.error_generic)
            }
        }
    }

    /**
     * Clears the current recoverable error.
     */
    fun onRecoverableErrorConsumed() {
        val current = _state.value
        if (current is CollectionEntryScreenState.Idle) {
            _state.value = current.copy(recoverableErrorMsgId = null)
        } else if (current is CollectionEntryScreenState.Logging) {
            _state.value = current.copy(recoverableErrorMsgId = null)
        }
    }

    companion object {
        val TAG: String = MyGameVaultApplication.buildTag("CollectionEntryViewModel")

        fun factory(repository: CollectionRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CollectionEntryViewModel(repository) as T
            }
        }
    }
}
