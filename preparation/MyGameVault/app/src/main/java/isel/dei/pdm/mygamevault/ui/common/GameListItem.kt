package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val GAME_LIST_ITEM_TAG = "GameListItem"
internal const val GAME_NAME_TAG = "GameName"
internal const val GAME_RELEASE_DATE_TAG = "GameReleaseDate"

/**
 * A Composable that displays a single game in a list, optimized for the "Add Game" screen.
 *
 * @param game The game to display.
 * @param onGameSelected The callback to be invoked when the game is selected.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun GameListItem(
    game: Game,
    onGameSelected: (Game) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
            .testTag(GAME_LIST_ITEM_TAG)
            .clickable { onGameSelected(game) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.thumbnailUri ?: game.coverUri,
                contentDescription = stringResource(R.string.game_cover_description, game.name.value),
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name.value,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    modifier = Modifier.testTag(GAME_NAME_TAG)
                )
                
                game.releaseDate?.let { date ->
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(R.string.released_label, date.year),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag(GAME_RELEASE_DATE_TAG)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameListItemPreview() {
    MyGameVaultTheme {
        GameListItem(
            game = Game(
                id = 1,
                name = "Elden Ring",
                releaseDate = LocalDate.of(2022, 2, 25),
                coverUri = "cache://er_cover",
                thumbnailUri = "cache://er_thumb"
            ),
            onGameSelected = {}
        )
    }
}
