package pt.isel.lei.pdm.nasaapod.ui.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@Composable
fun MainScreen2(
    vm: MainViewModel2,
    onNasaImageClicked: (ApodImage) -> Unit
) {
    ScreenBase(
        actions = NavigationActions(
            refreshAction = vm::refresh
        )
    )
    {
        Box() {
            when (val state = vm.screenState) {
                is MainScreenViewState.Idle -> {
                    IdleContent()
                }

                is MainScreenViewState.Loading -> {
                    LoadingContent()
                }

                is MainScreenViewState.Error -> {
                    ErrorContent(error = state.error, onRefresh = { vm.refresh() }
                    )
                }

                is MainScreenViewState.Content -> {
                    ApodImageView(
                        data = state.image,
                        modifier = Modifier.clickable(enabled = true, onClick = {
                            Log.d("ApodImageView", "click")
                            onNasaImageClicked(state.image)
                        })
                    )
                }
            }
        }
    }
}
