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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme

class MainActivity : ComponentActivity() {

    var counter = CounterModel(123)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        enableEdgeToEdge()
        setContent {
            CounterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column() {

                        CounterView(
                            counter = counter,
                            onIncrement = {
                                counter = counter.increment()
                            },
                            modifier = Modifier.padding(innerPadding)
                        )
                        /*
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )*/
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

    override fun onResume() {
        Log.d("MainActivity", "onResume")
        super.onResume()
    }

    override fun onRestart() {
        Log.d("MainActivity", "onRestart")
        super.onRestart()
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        super.onPause()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        super.onDestroy()
    }

}

data class CounterModel2(
    var counter: Int
)

fun CounterModel2.increment() {
    counter++
}

data class CounterModel(
    val count: Int
)
fun CounterModel.increment(): CounterModel {
    return CounterModel(count + 1)
}

@Composable
fun CounterView(
    counter: CounterModel,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(modifier = modifier) {
        Text(text = "${counter.count}")
        Button(onClick = {
            Log.d("CounterView", "up button")
            onIncrement()
        }) {

            Text("up")
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    Box()
    {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
                .background(Color.Red)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        )

        {


            Text(
                text = "Hellos $name!",
                modifier = modifier,
                fontSize = 30.sp
            )

            Button(
                modifier = Modifier.padding(12.dp),
                onClick = {
                    Log.d("Button", "onClick")
                }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Click")
                    Text(
                        text = "Me",
                        fontSize = 20.sp
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