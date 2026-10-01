package pt.isel.lei.pdm.nasaapod.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.NasaApodApplication
import pt.isel.lei.pdm.nasaapod.mock.MockNasaApodService
import pt.isel.lei.pdm.nasaapod.ui.BaseActivity
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit
import pt.isel.lei.pdm.nasaapod.ui.test.ApplicationStateTestScreen
import pt.isel.lei.pdm.nasaapod.ui.test.i18nTestScreen
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

class MainActivity : BaseActivity() {

    /*
    val vm by viewModels<MainViewModel>(factoryProducer = {
        MainViewModelFactory(
            MockNasaApodService()
        )
    })*/

    /*
    val vm by viewModels<MainViewModel>(
        factoryProducer =
            {
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(MockNasaApodService()) as T
                    }
                }
            })


    val vm by viewModels<MainViewModel> {
        viewModelInit {
            MainViewModel((application as DependencyContainer).nasaApodService)
        }

    }
    */
    val vm by viewModels<MainViewModel> {
        viewModelInit {
            MainViewModel(dependencyContainer.nasaApodService)
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            //i18nTestScreen()
            /*
            ApplicationStateTestScreen(
                this.application as NasaApodApplication
            )*/
            //
            MainScreen(
                vm
            )
            // */
        }
    }
}

