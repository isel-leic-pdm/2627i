package pt.isel.lei.pdm.nasaapod.ui.main

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun MainScreen2(
    vm: MainViewModel2,
    navigateToImage: (ApodImage) -> Unit,
    navigateToBookmarks: () -> Unit,
    navigateToInfiniteList: () -> Unit,
) {
    ScreenBase(onRefresh = {
        vm.refresh()
    }, onBookmark = navigateToBookmarks, onInfiniteList = navigateToInfiniteList) {
        when (val state = vm.screenState) {
            is MainScreenViewState.Error -> {
                ErrorContent(state.error, { vm.refresh() })
            }

            is MainScreenViewState.Idle -> {
                IdleContent()
            }

            is MainScreenViewState.Image -> {
                ImageContent(
                    state.img,
                    modifier = Modifier.clickable(
                        enabled = true,
                        onClick = {
                            Log.d("ImageContent", "Image clicked")
                            navigateToImage(state.img)
                        })
                )
            }

            is MainScreenViewState.Loading -> {
                LoadingContent()
            }
        }
    }
}