package pt.isel.lei.pdm.nasaapod.ui.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.core.net.toUri
import pt.isel.lei.pdm.nasaapod.DependencyContainer
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.viewModelInit
import pt.isel.lei.pdm.nasaapod.ui.main.MainViewModel2
import kotlin.getValue

class DetailsActivity : ComponentActivity() {

    companion object {
        const val IMAGE_PARAMETER = "image"
        fun navigate(ctx: Context, image: ApodImage) {
            val intent = Intent(ctx, DetailsActivity::class.java)
            intent.putExtra(IMAGE_PARAMETER, image)
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("DetailsActivity", "onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val image = intent.getParcelableExtra<ApodImage>(IMAGE_PARAMETER)
        if (image == null) {
            finish()
            return
        }

        val vm by viewModels<DetailsViewModel> {
            viewModelInit {
                DetailsViewModel(
                    image,
                    (application as DependencyContainer).favoriteRepository
                )
            }
        }
        setContent {
            DetailsScreen(
                vm = vm,
                backActionClicked = { finish() },
                infoActionClicked = {
                    navigateToImageUrl(image!!)
                }
            )
        }
    }

    fun navigateToImageUrl(image: ApodImage) {
        val intent = Intent(Intent.ACTION_VIEW, image.url.toUri())
        startActivity(intent)
    }
}
