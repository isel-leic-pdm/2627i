package pt.isel.lei.pdm.counter.ui.crowdtally

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeMax
import pt.isel.lei.pdm.counter.domain.decrement
import pt.isel.lei.pdm.counter.domain.increment

sealed interface CrowdTallyViewState {
    val info: CrowdTallyInfo

    data class Counting(override val info: CrowdTallyInfo) : CrowdTallyViewState

    data class Editor(override val info: CrowdTallyInfo, val errorMsg: String = "") :
        CrowdTallyViewState
}


class CrowdTallyViewModel : ViewModel() {

    var screenState: CrowdTallyViewState by mutableStateOf(
        CrowdTallyViewState.Counting(
            CrowdTallyInfo(0, 10)
        )
    )

    fun increment() {
        screenState = CrowdTallyViewState.Counting(
            screenState.info.increment()
        )
    }

    fun decrement() {
        screenState = CrowdTallyViewState.Counting(
            screenState.info.decrement()
        )
    }

    fun startEditor() {
        screenState = CrowdTallyViewState.Editor(
            screenState.info
        )
    }

    fun changeMax(newMax: Int) {
        try {

            screenState = CrowdTallyViewState.Counting(screenState.info.changeMax(newMax))
        } catch (e: Exception) {
            screenState = CrowdTallyViewState.Editor(
                screenState.info,
                e.toString()
            )
        }
    }
}