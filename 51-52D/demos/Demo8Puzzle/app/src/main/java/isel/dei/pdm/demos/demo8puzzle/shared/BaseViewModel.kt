package isel.dei.pdm.demos.demo8puzzle.shared

import androidx.lifecycle.ViewModel

/**
 * Base class for all view models in the app. It is used to log the lifecycle of the view models.
 */
abstract class BaseViewModel : ViewModel() {

    init {
        log("init() on instance ${hashCode()}")
    }

    /**
     * Called when the view model is no longer used and will be destroyed.
     */
    override fun onCleared() {
        log("onCleared() on instance ${hashCode()}")
    }
}