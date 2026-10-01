package pt.isel.lei.pdm.nasaapod.ui.common

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.savedstate.SavedStateRegistryOwner

@Suppress("UNCHECKED_CAST")
fun <T> viewModelInit(block: () -> T) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return block() as T
        }
    }

fun <T : ViewModel> SavedStateRegistryOwner.viewModelInitWithSavedState(
    block: (SavedStateHandle) -> T
) = initWithSavedStateRegistryOwner(this, block)



@Suppress("UNCHECKED_CAST")
fun <T : ViewModel> initWithSavedStateRegistryOwner(
    owner: SavedStateRegistryOwner,
    block: (SavedStateHandle) -> T
): ViewModelProvider.Factory {

    return object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(
            modelClass: Class<VM>,
            extras: CreationExtras
        ): VM {
            val handle = extras.createSavedStateHandle()

            return block(handle) as VM
        }
    }
}