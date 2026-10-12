package pt.isel.lei.pdm.nasaapod.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

open class ViewModelBase : ViewModel() {
    var loading: Boolean by mutableStateOf(false)
        private set

    var error: Exception? by mutableStateOf(null)
        private set

    protected fun safeCall(func: suspend ()->Unit){
        loading = true
        error = null

        viewModelScope.launch {
            try {
                func()
            } catch (e: Exception) {
                error = e
            }
            finally {
                loading = false
            }
        }
    }
}