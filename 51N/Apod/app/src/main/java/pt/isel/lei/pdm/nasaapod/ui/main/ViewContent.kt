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
fun ImageContent(todayImage: ApodImage?) {
    ApodImageView(
        todayImage!!,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BoxScope.IdleContent() {
    Text(
        text = stringResource(R.string.no_content),
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.Center),
        fontSize = 25.sp
    )
}

@Composable
fun BoxScope.LoadingContent() {
    Text(
        text = stringResource(R.string.loading),
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.Center),
        fontSize = 25.sp
    )
}

@Composable
fun BoxScope.ErrorContent(
    error: Exception?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error.toString(),
            textAlign = TextAlign.Center,
            fontSize = 25.sp
        )

        Button(onClick = { onRefresh() }) {
            Text(text = stringResource(R.string.refresh))
        }
    }
}