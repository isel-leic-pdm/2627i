package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pt.isel.lei.pdm.nasaapod.ui.common.ApodImageView
import pt.isel.lei.pdm.nasaapod.ui.common.ScreenBase

@Composable
fun DetailsScreen(
    vm: DetailsViewModel,
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

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        Box() {
            ApodImageView(vm.image)
            FavoriteButton(
                isFav = vm.isFavorite.collectAsStateWithLifecycle().value,
                onClick = {
                    vm.toggleFavorite()
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )
        }
        Text(text = vm.image.description)
    }
}


@Composable
fun FavoriteButton(
    isFav: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onClick) {
        if (isFav)
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