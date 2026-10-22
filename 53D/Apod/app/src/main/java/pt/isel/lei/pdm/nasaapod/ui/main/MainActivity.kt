package pt.isel.lei.pdm.nasaapod.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.BaseActivity
import pt.isel.lei.pdm.nasaapod.ui.bookmarks.BookmarksActivity
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit
import pt.isel.lei.pdm.nasaapod.ui.details.DetailsActivity

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

    val vm by viewModels<MainViewModel> {
        viewModelInit {
            MainViewModel(dependencyContainer.nasaApodService)
        }

    }
     */
    val vm2 by viewModels<MainViewModel2> {
        viewModelInit {
            MainViewModel2(dependencyContainer.nasaApodService)
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
            /*
            MainScreen(
                vm
            )*/
            MainScreen2(
                vm = vm2,
                navigateToImage = { DetailsActivity.navigate(this, it) },
                navigateToBookmarks = { BookmarksActivity.navigate(this) }
            )
            // */
        }
    }


    fun navigateToUrl(img: ApodImage) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(img.url))
        startActivity(intent)
    }
}

