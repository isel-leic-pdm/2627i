package pt.isel.lei.pdm.counter.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrowdTallyInfo(
    val count: Int,
    val capacity: Int,
) : Parcelable {
    init {
        require(capacity >= 0) {
            "capacity can't be negative"
        }
    }

    val canDecrement
        get() = count > 0
    val canIncrement
        get() = capacity > count
}

fun CrowdTallyInfo.increment() =
    if (canIncrement)
        this.copy(count = count + 1)
    else
        this

fun CrowdTallyInfo.decrement() =
    if (canDecrement)
        this.copy(count = count - 1)
    else this

fun CrowdTallyInfo.changeCapacity(newCap: Int) =
    this.copy(capacity = newCap, count = Math.min(newCap, count))



