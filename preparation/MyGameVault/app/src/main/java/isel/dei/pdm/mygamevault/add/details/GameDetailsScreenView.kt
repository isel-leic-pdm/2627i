package isel.dei.pdm.mygamevault.add.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.GameDetails
import isel.dei.pdm.mygamevault.ui.common.AsyncImage
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

@Composable
fun GameDetailsScreenView(
    state: GameDetailsScreenState,
    onRetryRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        is GameDetailsScreenState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is GameDetailsScreenState.Loaded -> {
            LoadedView(state.details, modifier)
        }
        is GameDetailsScreenState.Error -> {
            // This is now handled in GameDetailsScreen.kt for consistency,
            // but we keep a fallback here just in case.
            FatalErrorView(
                message = stringResource(R.string.error_generic),
                buttonText = stringResource(R.string.retry_label),
                onButtonClicked = onRetryRequested,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadedView(
    details: GameDetails,
    modifier: Modifier = Modifier
) {
    val game = details.game
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = game.name.value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                uri = game.coverUri,
                contentDescription = stringResource(R.string.game_cover_description, game.name.value),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(3f / 4f)
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailItem(label = stringResource(R.string.details_released_label), value = game.releaseDate?.toString() ?: "N/A")
                DetailItem(label = stringResource(R.string.details_developer_label), value = details.developers.joinToString(", "))
                DetailItem(label = stringResource(R.string.details_publisher_label), value = details.publishers.joinToString(", "))
                DetailItem(label = stringResource(R.string.details_genre_label), value = details.genres.joinToString(", "))
            }
        }

        Text(
            text = stringResource(R.string.details_description_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = details.description ?: "",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GameDetailsScreenLoadedPreview() {
    MyGameVaultTheme {
        GameDetailsScreenView(
            state = GameDetailsScreenState.Loaded(
                details = GameDetails(
                    game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "https://images.igdb.com/igdb/image/upload/t_cover_big/co4jni.jpg"),
                    description = "Elden Ring is a fantasy, action-RPG adventure set within a world created by Hidetaka Miyazaki and George R.R. Martin.",
                    developers = listOf("FromSoftware"),
                    publishers = listOf("Bandai Namco Entertainment"),
                    genres = listOf("Role-playing (RPG)", "Adventure")
                )
            ),
            onRetryRequested = {}
        )
    }
}
