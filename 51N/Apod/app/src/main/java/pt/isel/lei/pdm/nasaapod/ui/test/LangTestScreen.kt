package pt.isel.lei.pdm.nasaapod.ui.test

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.setAppLocale
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@Composable
fun LangTestScreen() {
    val ctx = LocalContext.current
    NasaApodTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
            ) {

                Text(text = stringResource(R.string.lang))
                Button(onClick = {
                    ctx.setAppLocale("pt")
                }) {
                    Text(text = stringResource(R.string.lang_pt))
                    Text(text = stringResource(R.string.xpto))
                }

                Button(onClick = {
                    ctx.setAppLocale("en")
                }) {
                    Text(text = stringResource(R.string.lang_en))
                }
            }

        }
    }
}