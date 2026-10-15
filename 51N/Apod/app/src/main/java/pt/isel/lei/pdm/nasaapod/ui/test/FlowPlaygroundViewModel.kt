package pt.isel.lei.pdm.nasaapod.ui.test

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

const val TAG: String = "FLOW"

class FlowPlaygroundViewModel(
) : ViewModel() {


    private val simpleFlow = flow<Int> {
        Log.d(TAG, "going to start the flow")
        repeat(3) { v: Int ->
            delay(1000)
            emit(v)
        }
    }

    fun simpleFlow() {
        viewModelScope.launch {
            delay(2000)
            Log.d(TAG, "simple flow start")

            simpleFlow.collect {
                Log.d(TAG, "collected $it")
            }

            Log.d(TAG, "simple flow end")
        }
    }

    fun simpleFlowTwoSubscribers() {
        viewModelScope.launch {
            simpleFlow.collect {
                Log.d(TAG, "sub1: collected $it")
            }
        }
        viewModelScope.launch {
            delay(2000)
            simpleFlow.collect {
                Log.d(TAG, "sub2: collected $it")
            }
        }
    }

    fun simpleFlowExtended() {

        //builder
        val f = flow {
            repeat(3)
            { v ->
                delay(500)
                emit(v)
            }
            throw Exception("Error omfg")
        }
            //intermediate operations
            .onStart {
                Log.d(TAG, "onStart -1")
                emit(-1)
            }
            .onEach { value ->
                Log.d(TAG, "onEach $value")
            }
            .onCompletion {
                Log.d(TAG, "onCompletion $it")
            }
            .catch {
                Log.d(TAG, "catch error")
                emit(-1000)
            }

        viewModelScope.launch {
            //terminal operations
            /*
                        f.collect { value ->
                            Log.d(TAG, "collect $value")

                        }

                        val flowFirstValue = f.first()
                        Log.d(TAG, "flow first value:  $flowFirstValue")

                        val flowNrOfInts = f.count()

                        Log.d(TAG, "flow count values:  $flowNrOfInts")

             */
            f.filter { it % 2 == 0 }
                .map { it * 2 }
                .collect {
                    Log.d(TAG, "value after filter and map  $it")
                }

        }

    }

    fun flowCreation() {
        val list = listOf(1, 2, 3, 4)

        viewModelScope.launch {
            val f = list.asFlow();
            f.collect {
                Log.d(TAG, "list value $it")
            }
        }

        val function = suspend {
            delay(5)
            list
        }

        val listFlow = function.asFlow()

        val intFlow = flow {
            val list = function()
            list.forEach {
                delay(1000)
                emit(it)
            }
            emit(5)
        }

        viewModelScope.launch {
            listFlow.collect {
                Log.d(TAG, "listflow count ${it.count()}")

            }

            intFlow.collect {
                Log.d(TAG, "list turned into into flow: $it")

            }
        }
    }

    fun hotFlow() {

        val mutableStateFlow = MutableStateFlow<Int>(-3)
        viewModelScope.launch {
            mutableStateFlow.emit(-2)
            mutableStateFlow.emit(-1)
            //never returns!!!
            mutableStateFlow.collect {
                Log.d(TAG, "hot flow value: $it")
            }
            Log.d(TAG, "hahahah it did!")
        }

        viewModelScope.launch {
            delay(1000)
            mutableStateFlow.emit(123)

            //second subscriber
            viewModelScope.launch {
                //never returns!!!
                mutableStateFlow.collect {
                    Log.d(TAG, "hot flow 2 value: $it")
                }
            }

            delay(1000)
            mutableStateFlow.value = 321


            delay(1000)
            mutableStateFlow.value = 456


        }
    }


    private val mutableStateFlow = MutableStateFlow(-1)

    val viewFlow: StateFlow<Int>
        get() = mutableStateFlow


    fun hotFlowInUi() {
        viewModelScope.launch {
            repeat(5)
            {
                delay(1000)
                mutableStateFlow.emit(it)
            }
        }
    }

}