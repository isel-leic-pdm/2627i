package pt.isel.lei.pdm.nasaapod.ui.bookmarks

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun BookmarksScreen(
    vm: BookmarksViewModel,
    backButtonClicked: (() -> Unit)?
) = ScreenBase(
    onBack = backButtonClicked
)
{
    val screenState = vm.viewState.collectAsStateWithLifecycle().value
    when (val state = screenState) {
        is BookmarksScreenState.Idle -> {
            Text(
                text = stringResource(R.string.app_no_image),
                fontSize = 24.sp
            )
        }

        is BookmarksScreenState.Content -> {
            val images = state.images.collectAsStateWithLifecycle().value

            //EagerContent(images, vm)
            LazyContent(images, vm)

        }
    }

}


@Composable
private fun LazyContent(
    images: List<ApodImage>,
    vm: BookmarksViewModel
) {
    LazyColumn() {
        items(images) { image ->
            Box() {
                ApodImageView(image)
                IconButton(onClick = {
                    vm.removeBookmark(image)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EagerContent(
    images: List<ApodImage>,
    vm: BookmarksViewModel
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        images.forEach { image ->
            Box() {
                ApodImageView(image)
                IconButton(onClick = {
                    vm.removeBookmark(image)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        }
    }
}