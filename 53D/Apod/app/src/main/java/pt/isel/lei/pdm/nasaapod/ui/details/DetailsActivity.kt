package pt.isel.lei.pdm.nasaapod.ui.details

import android.content.Intent
import android.net.Uri
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
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val image = intent.getParcelableExtra<ApodImage?>("image")

        if (image == null) {
            finish()
            return
        }
/*
        val vm by viewModels<DetailsViewModel> {
            viewModelInit {
                DetailsViewModel(
                    image,
                    (application as DependencyContainer).favoriteRepository
                )
            }
        }
*/
        val vm2 by viewModels<DetailsViewModel2> {
            viewModelInit {
                DetailsViewModel2(
                    image,
                    (application as DependencyContainer).favoriteRepository
                )
            }
        }
        enableEdgeToEdge()
        setContent {
            /*
            DetailsScreen(
                vm,
                {
                    finish()
                },
                {
                    navigateToUrl(image)
                })
             */
            DetailsScreen2(
                vm2,
                {
                    finish()
                },
                {
                    navigateToUrl(image)
                })
        }
    }

    fun navigateToUrl(img: ApodImage) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(img.url))
        startActivity(intent)
    }
}
