package pt.isel.lei.pdm.counter.domain

data class CounterModel(
    val count: Int

)

fun CounterModel.increment(): CounterModel {
    return CounterModel(count + 1)
}


