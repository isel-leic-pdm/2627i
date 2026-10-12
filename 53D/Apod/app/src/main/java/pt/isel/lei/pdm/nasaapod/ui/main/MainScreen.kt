package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
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
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@Composable
fun MainScreen(vm: MainViewModel) {
    NasaApodTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
            ) {

                if (vm.error != null) {
                    ErrorContent(vm.error, { vm.refresh() })
                } else if (vm.loading) {
                    LoadingContent()
                } else if (vm.image == null) {
                    IdleContent()
                } else {
                    ImageContent(vm.image)
                }

            }

        }
    }

}
