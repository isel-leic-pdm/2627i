package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun DetailsScreen(
    image: ApodImage,
    onBackPressed: () -> Unit,
    onInfoClicked : () -> Unit
) = ScreenBase(
    onBack = {
        onBackPressed()
    },
    onInfo = {
        onInfoClicked()
    }
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        ApodImageView(image)
        Text(text = image.description)
    }
}