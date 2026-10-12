package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun DetailsScreen(
    img: ApodImage,
    backActionClicked: () -> Unit,
    infoActionClicked: () -> Unit,
) = ScreenBase(
    actions = NavigationActions(
        backAction = backActionClicked,
        infoAction = infoActionClicked
    )
) {
    val scrollState = rememberScrollState()
    Column(

        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ApodImageView(img)
        Text(text = img.description)
    }
}