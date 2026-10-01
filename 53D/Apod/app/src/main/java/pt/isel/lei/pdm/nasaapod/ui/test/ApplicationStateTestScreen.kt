package pt.isel.lei.pdm.nasaapod.ui.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pt.isel.lei.pdm.nasaapod.NasaApodApplication
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@Composable
fun ApplicationStateTestScreen(app: NasaApodApplication) {

    NasaApodTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding)
            ) {

                Text(app.stateThatINeverShouldUse.toString())
                Button(onClick = {
                    app.stateThatINeverShouldUse++
                }) {
                    Text(text = stringResource(R.string.app_increment))

                }
            }

        }
    }
}