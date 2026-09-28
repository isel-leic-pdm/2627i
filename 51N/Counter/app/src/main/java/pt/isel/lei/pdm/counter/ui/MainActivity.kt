package pt.isel.lei.pdm.counter.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pt.isel.lei.pdm.counter.domain.CounterModel
import pt.isel.lei.pdm.counter.domain.increment
import pt.isel.lei.pdm.counter.ui.counter.CounterScreen
import pt.isel.lei.pdm.counter.ui.counter.CounterView
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen2
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyScreen3
import pt.isel.lei.pdm.counter.ui.crowdtally.CrowdTallyViewModel
import pt.isel.lei.pdm.counter.ui.theme.CounterTheme


class MainActivity : LoggingActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        val vm by viewModels<CrowdTallyViewModel>()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            //CounterScreen()
            //CrowdTallyScreen()
            CrowdTallyScreen3(vm)
        }
    }

}