package pt.isel.lei.pdm.counter.ui.crowdtally

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeCapacity
import pt.isel.lei.pdm.counter.domain.decrement
import pt.isel.lei.pdm.counter.domain.increment
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme


@Composable
fun CrowdTallyScreen3(vm: CrowdTallyViewModel) {
    Log.d("CrowdTallyScreen", "recomposition")



    CounterTheme() {
        Log.d("CounterTheme", "Recomposition")
        Scaffold() { innerPadding ->
            Log.d("Scaffold", "Recomposition")
            when (val state = vm.screenState) {
                is CrowdTallyScreenState.Configuration -> {

                    CrowdTallyMaxConfiguratorContent(
                        state.info,
                        { newCapacity ->
                            vm.changeToNewCapacity(newCapacity)

                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                is CrowdTallyScreenState.Counting -> {
                    Box()
                    {

                        CrowdTallyContent(
                            state = state.info,
                            increment = {
                                vm.increment()
                            },
                            decrement = {
                                vm.decrement()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )

                        Button(
                            onClick = {
                                vm.startConfiguration()
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("Edit")
                        }
                    }

                }

                is CrowdTallyScreenState.ConfigurationError -> {

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        Text(
                            text = state.error.toString(),
                            color = Color.Red,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                        )
                        Button(onClick = {
                            vm.startConfiguration()
                        }) {
                            Text("Dismiss")
                        }
                    }

                }
            }


        }
    }
}

