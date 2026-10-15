package pt.isel.lei.pdm.nasaapod.ui.details

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val isFavorite =
        vm.viewState.collectAsStateWithLifecycle().value is DetailScreenViewState.Favorite

    Column(
        modifier = Modifier.verticalScroll(scrollState)
    ) {
        Box() {
            ApodImageView(vm.image)
            FavoriteButton(
                isFav = isFavorite, onClick = {
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