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
import kotlinx.coroutines.newSingleThreadContext
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeMax
import pt.isel.lei.pdm.counter.domain.decrement
import pt.isel.lei.pdm.counter.domain.increment
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme


@Composable
fun CrowdTallyScreen3(viewModel: CrowdTallyViewModel) {

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {

            when (val curr = viewModel.screenState) {
                is CrowdTallyViewState.Counting -> {

                    Box(modifier = Modifier.fillMaxSize()) {
                        CrowdTallyView(
                            state = curr.info,
                            increment = {
                                viewModel.increment()
                                Log.d("CrowdTallyScreen", "Increment ${curr.info.counter}")
                            },
                            decrement = {
                                viewModel.decrement()
                                Log.d("CrowdTallyScreen", "Decrement ${curr.info.counter}")
                            }
                        )
                        Button(
                            onClick = {
                                viewModel.startEditor()
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text("Edit")
                        }
                    }
                }

                is CrowdTallyViewState.Editor -> {
                    Box()
                    {
                        CrowdTallyMaxEditor(
                            arg = curr.info,
                            onNewMax = {
                                viewModel.changeMax(it)
                            }
                        )
                        if (curr.errorMsg.isNotEmpty())
                            Text(text = curr.errorMsg, color = Color.Red)
                    }
                }
            }


        }
    }
}

