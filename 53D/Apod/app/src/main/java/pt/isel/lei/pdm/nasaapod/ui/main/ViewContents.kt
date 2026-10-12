package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView

@Composable
fun ImageContent(
    image: ApodImage?,
    modifier: Modifier = Modifier
) {
    ApodImageView(
        image!!,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
fun BoxScope.IdleContent() {
    Text(
        text = stringResource(R.string.app_no_image),
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center,
        fontSize = 24.sp
    )
}

@Composable
fun BoxScope.LoadingContent() {
    Text(
        text = stringResource(R.string.app_loading),
        modifier = Modifier.align(Alignment.Center),
        textAlign = TextAlign.Center,
        fontSize = 24.sp
    )
}

@Composable
fun BoxScope.ErrorContent(
    error: Exception?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = error.toString(),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
        Button(onClick = {
            onClick()
        }) {
            Text(text = stringResource(R.string.app_refresh))
        }

    }
}