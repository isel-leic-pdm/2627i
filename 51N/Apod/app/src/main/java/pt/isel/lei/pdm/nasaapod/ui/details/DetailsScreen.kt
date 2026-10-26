package pt.isel.lei.pdm.nasaapod.ui.details

import android.widget.Button
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.NavigationActions
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun DetailsScreen(
    vm: DetailsViewModel,
    backActionClicked: () -> Unit,
    infoActionClicked: () -> Unit,
) = ScreenBase(
    actions = NavigationActions(
        backAction = backActionClicked,
        infoAction = infoActionClicked
    )
) {

    val scrollState = rememberScrollState()
    val state by vm.viewState.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box() {
            ApodImageView(vm.image)

            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
            )
            {
                FavoriteButton(
                    state is DetailScreenViewState.Favorite,
                    onClick = vm::toggleFavorite
                )
                BookmarkButton(
                    state.isBookmarked,
                    onClick = vm::toggleBookmark
                )

            }

        }

        Text(text = AnnotatedString.fromHtml(vm.image.description))

    }
}


@Composable
fun BookmarkButton(
    isBookmarked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (isBookmarked)
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = "",
                tint = Color.White
            )
        else
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "",
                tint = Color.White
            )
    }
}

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick, modifier = modifier) {
        if (isFavorite)
            Icon(
                imageVector = Icons.Default.HeartBroken,
                contentDescription = "",
                tint = Color.White
            )
        else
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "",
                tint = Color.White
            )
    }
}