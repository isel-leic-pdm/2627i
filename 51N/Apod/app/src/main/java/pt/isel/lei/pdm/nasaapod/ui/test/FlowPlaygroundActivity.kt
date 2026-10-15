package pt.isel.lei.pdm.nasaapod.ui.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class FlowPlaygroundActivity : ComponentActivity() {

    val viewModel by viewModels<FlowPlaygroundViewModel> {
        viewModelInit {
            FlowPlaygroundViewModel()
        }
    }

    companion object {
        fun navigate(ctx: Context) {
            val intent = Intent(ctx, FlowPlaygroundActivity::class.java)
            ctx.startActivity(intent)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            var bindFlow by remember { mutableStateOf(false) }
            ScreenBase(){
                Column(
                ) {
                    TextButton("Simple Flow", viewModel::simpleFlow)
                    TextButton("Double Subs Flow", viewModel::simpleFlowTwoSubscribers)
                    TextButton("Simple extended", viewModel::simpleFlowExtended)
                    TextButton("Flow Creation", viewModel::flowCreation)
                    TextButton("Hot flow", viewModel::hotFlow)
                    TextButton("Hot flow bind", {
                        viewModel.hotFlowInUi()
                        bindFlow = true
                    })

                    if (bindFlow) {
                        val state = viewModel.viewFlow.collectAsState().value
                        Text("Flow current value is $state", fontSize = 20.sp)
                    }


                }
            }
        }
    }
}

@Composable
fun TextButton(
    str: String,
    click: () -> Unit
) {
    Button(onClick = click) {
        Text(str)
    }
}

