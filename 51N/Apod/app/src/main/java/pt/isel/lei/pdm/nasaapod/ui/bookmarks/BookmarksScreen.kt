package pt.isel.lei.pdm.nasaapod.ui.bookmarks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun BookmarksScreen(
    vm: BookmarksViewModel,
    backActionClicked: () -> Unit,
    imageClicked: (ApodImage) -> Unit,
) = ScreenBase(
    actions = NavigationActions(
        backAction = backActionClicked
    )
) {
    val screenState = vm.viewState.collectAsStateWithLifecycle().value

    when (val state = screenState) {
        is BookmarksScreenState.Idle -> {
            Text(text = stringResource(R.string.no_content))
        }

        is BookmarksScreenState.Content -> {
            val images = state.images.collectAsStateWithLifecycle().value
            LazyColumn() {
                items(images) {
                    Box()
                    {
                        ApodImageView(
                            it,
                            modifier = Modifier.clickable(
                                enabled = true,
                                onClick = { imageClicked(it) })
                        )

                        IconButton(onClick = {
                            vm.deleteBookmark(it)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "",
                                tint = Color.White
                            )
                        }
                    }

                }
            }
        }
    }
}