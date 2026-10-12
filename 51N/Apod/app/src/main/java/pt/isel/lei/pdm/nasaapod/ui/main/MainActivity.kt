package pt.isel.lei.pdm.nasaapod.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.text.LinkAnnotation
import androidx.core.net.toUri
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.ActivityBase
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit
import pt.isel.lei.pdm.nasaapod.ui.details.DetailsActivity

class MainActivity : ActivityBase() {

    /*
    val vm by viewModels<MainViewModel>(factoryProducer = {
        MainViewModelFactory(MockNasaApodService())
    })

    val vm by viewModels<MainViewModel>(factoryProducer = {
        object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(MockNasaApodService()) as T
            }
        }
    })
*/
    val vm by viewModels<MainViewModel> {
        viewModelInit {
            MainViewModel((application as DependencyContainer).nasaService)
        }
    }

    val vm2 by viewModels<MainViewModel2> {
        viewModelInit {
            MainViewModel2((application as DependencyContainer).nasaService)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            //LangTestScreen()
            //
            //MainScreen(
            //    vm
            //)
            MainScreen2(
                vm = vm2,
                onNasaImageClicked = ::navigateToDetails
            )

            // */
        }
    }



    fun navigateToDetails(image: ApodImage) {
        DetailsActivity.navigate(this, image)
    }
}

