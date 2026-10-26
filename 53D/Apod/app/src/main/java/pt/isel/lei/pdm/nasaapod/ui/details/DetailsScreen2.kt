package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun DetailsScreen2(
    vm: DetailsViewModel2,
    onBackPressed: () -> Unit,
    onInfoClicked: () -> Unit
) = ScreenBase(
    onBack = {
        onBackPressed()
    },
    onInfo = {
        onInfoClicked()
    }
) {
    val scrollState = rememberScrollState()
    val viewState = vm.viewState.collectAsStateWithLifecycle().value
    val isFavorite = viewState is DetailScreenViewState.Favorite

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        Box() {
            ApodImageView(vm.image)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
            {

                FavoriteButton(
                    isFav = isFavorite,
                    onClick = {
                        vm.toggleFavorite()
                    },
                )

                BookmarkButton(
                    isBookmarked = viewState.isBookmarked,
                    onClick = {
                        vm.toggleBookmark()
                    },
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
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = "",
                tint = Color.White
            )
        else
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = "",
                tint = Color.White
            )
    }
}