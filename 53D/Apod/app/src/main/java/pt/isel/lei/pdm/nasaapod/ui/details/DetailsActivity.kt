package pt.isel.lei.pdm.nasaapod.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.details.ui.theme.NasaApodTheme

class DetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val image = intent.getParcelableExtra<ApodImage?>("image")

        if (image == null) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            DetailsScreen(
                image,
                {
                    finish()
                },
                {
                    navigateToUrl(image)
                })
        }
    }

    fun navigateToUrl(img : ApodImage){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(img.url))
        startActivity(intent)
    }
}
