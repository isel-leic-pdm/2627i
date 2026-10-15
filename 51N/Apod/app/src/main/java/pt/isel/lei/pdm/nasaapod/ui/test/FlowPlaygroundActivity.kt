package pt.isel.lei.pdm.nasaapod.ui.test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit
import pt.isel.lei.pdm.nasaapod.ui.test.ui.theme.NasaApodTheme
import java.util.concurrent.Flow

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
            ScreenBase(actions = NavigationActions(onBackAction = { finish() })) {
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
                        val state = viewModel.state.collectAsState().value
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

