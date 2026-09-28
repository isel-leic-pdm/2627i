package pt.isel.lei.pdm.counter.ui.crowdtally

import android.R
import android.util.Log
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.domain.CrowdTallyInfo
import pt.isel.lei.pdm.counter.domain.changeCapacity
import pt.isel.lei.pdm.counter.domain.decrement
import pt.isel.lei.pdm.counter.domain.increment
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme



@Composable
fun CrowdTallyScreen() {
    Log.d("CrowdTallyScreen", "recomposition")

    var state by rememberSaveable { mutableStateOf(CrowdTallyInfo(0, 10)) }
    var isEdit by rememberSaveable { mutableStateOf(false) }
    var error: String? by rememberSaveable { mutableStateOf(null) }
    CounterTheme() {
        Log.d("CounterTheme", "Recomposition")
        Scaffold() { innerPadding ->
            Log.d("Scaffold", "Recomposition")


            if (isEdit) {
                CrowdTallyMaxConfiguratorContent(
                    state,
                    {
                        try {
                            state = state.changeCapacity(it)
                            isEdit = false
                        } catch (e: Exception) {
                            error = e.toString()
                        }
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                Box()
                {
                    CrowdTallyContent(
                        state = state,
                        increment = { state = state.increment() },
                        decrement = { state = state.decrement() },
                        modifier = Modifier.padding(innerPadding)
                    )

                    Button(
                        onClick = {
                            isEdit = true
                        },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("Edit")
                    }
                }

            }

            if (error != null) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = .5f))


                ) {
                    Text(
                        text = error.toString(),
                        color = Color.Red,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = {
                        error = null
                    }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

