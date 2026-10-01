package pt.isel.lei.pdm.nasaapod.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.domain.ApodImage
import pt.isel.lei.pdm.nasaapod.mock.MockApodImages

@Composable
fun ApodImageView(
    data: ApodImage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Box() {
            AsyncImage(
                model = data.url,
                contentDescription = "",
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                contentScale = ContentScale.FillWidth,
                placeholder = painterResource(R.drawable.ic_launcher_foreground)
            )

            Text(
                text = data.title,
                color = Color.White,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f))
                    .fillMaxWidth()
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
        ) {

            Text(
                text = data.date,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.author,
                textAlign = TextAlign.End,
            )
        }
    }
}


@Preview
@Composable
private fun ApodImageViewPreview() {
    ApodImageView(
        MockApodImages.Images[0]
    )
}