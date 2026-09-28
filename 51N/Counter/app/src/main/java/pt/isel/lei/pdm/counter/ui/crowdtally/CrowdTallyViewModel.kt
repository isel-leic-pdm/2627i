package pt.isel.lei.pdm.counter.ui.crowdtally

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeCapacity
import pt.isel.lei.pdm.counter.domain.decrement
import pt.isel.lei.pdm.counter.domain.increment


sealed interface CrowdTallyScreenState {
    val info: CrowdTallyInfo

    data class Configuration(override val info: CrowdTallyInfo) : CrowdTallyScreenState
    data class Counting(override val info: CrowdTallyInfo) : CrowdTallyScreenState
    data class ConfigurationError(override val info: CrowdTallyInfo, val error: String) :
        CrowdTallyScreenState
}


class CrowdTallyViewModel : ViewModel() {

    var screenState: CrowdTallyScreenState
            by mutableStateOf(
                CrowdTallyScreenState.Counting(
                    CrowdTallyInfo(0, 10)
                )
            )

    fun changeToNewCapacity(newCapacity: Int) {
        try {
            screenState = CrowdTallyScreenState.Counting(
                screenState.info.changeCapacity(newCapacity)
            )
        } catch (e: Exception) {
            screenState = CrowdTallyScreenState.ConfigurationError(
                screenState.info, e.toString()
            )
        }
    }

    fun decrement() {
        screenState = CrowdTallyScreenState.Counting(
            screenState.info.decrement()
        )
    }

    fun increment() {
        screenState = CrowdTallyScreenState.Counting(
            screenState.info.increment()
        )
    }

    fun startConfiguration() {
        screenState = CrowdTallyScreenState.Configuration(
            screenState.info
        )
    }


    override fun onCleared() {
        super.onCleared()
    }
}
