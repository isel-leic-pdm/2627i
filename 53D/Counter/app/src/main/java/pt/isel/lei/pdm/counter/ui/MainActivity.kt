package pt.isel.lei.pdm.counter.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.counter.domain.CounterInfo
import pt.isel.lei.pdm.counter.domain.increment
import pt.isel.lei.pdm.counter.ui.counter.CounterScreen
import pt.isel.lei.pdm.counter.ui.counter.CounterView
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen2
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen3
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyViewModel

class MainActivity : LoggingActivity() {
    val vm by viewModels<CrowdTallyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            //CrowdTallyScreen()
            CrowdTallyScreen3(vm)
        }
    }
}


