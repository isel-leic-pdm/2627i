package pt.isel.lei.pdm.counter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("MainActivity", "onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var state = CounterInfo(123)
        setContent {
            CounterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    /*
                     Greeting(
                         name = "Android",
                         modifier = Modifier.padding(innerPadding)
                     )*/
                    Column(modifier = Modifier.padding(innerPadding)) {
                        CounterView(state, {
                            state = state.increment()
                        })
                    }
                }
            }
        }
    }

    override fun onStart() {
        Log.d("MainActivity", "onStart")
        super.onStart()
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop")
        super.onStop()
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        super.onPause()
    }

    override fun onRestart() {
        Log.d("MainActivity", "onRestart")
        super.onRestart()
    }

    override fun onResume() {
        Log.d("MainActivity", "onResume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        super.onDestroy()
    }
}

data class CounterInfo2(
    var counter: Int
) {
    fun incremenmt() {
        counter++
    }
}

data class CounterInfo(
    val counter: Int
) {

}

fun CounterInfo.increment(): CounterInfo {
    return CounterInfo(counter + 1)
}


@Composable
fun CounterView(
    number: CounterInfo,
    onIncrement: () -> Unit
) {
    Row() {
        Text(
            text = "${number.counter}",
            fontSize = 20.sp
        )
        Button(onClick = {
            Log.d("CounterViewButton", "Clicked")
            onIncrement()
        }) {
            Text("Increment")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    Box()
    {
        Row(
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .background(Color.Red)
                .align(Alignment.Center)
        ) { }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Hello $name!",
                modifier = modifier.padding(12.dp),
                fontSize = 30.sp
            )

            Button(
                onClick = {
                    Log.d("Button", "Click")
                }) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Click",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Me",
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CounterTheme {
        Greeting("Android")
    }
}