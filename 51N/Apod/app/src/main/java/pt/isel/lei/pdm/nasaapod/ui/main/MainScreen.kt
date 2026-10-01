package pt.isel.lei.pdm.nasaapod.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                ApodImageView(
                    vm.todayImage,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }

}