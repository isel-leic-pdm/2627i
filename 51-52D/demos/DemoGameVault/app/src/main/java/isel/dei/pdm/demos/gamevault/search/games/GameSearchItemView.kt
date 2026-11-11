package isel.dei.pdm.demos.gamevault.search.games

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.games.GameInfo
import isel.dei.pdm.demos.gamevault.ui.common.AddStatus
import isel.dei.pdm.demos.gamevault.ui.common.AddStatusIndicator
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImage
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.time.LocalDate

/**
 * Composable used to display a game on one of the user's platforms inside the list of game search
 * results. Laid out like the collection's items (see CollectionItemView): the game's thumbnail,
 * name and platform, followed by its release year, which tells apart games with similar names
 * (e.g. a game and its remake). The platform is displayed without its version, as games are
 * oblivious of versions. A trailing indicator displays the addition status.
 *
 * Absent information is omitted, and only items that are not added (nor being added) can be
 * selected.
 *
 * @param item The game search item to be displayed.
 * @param onSelected Callback to be invoked when the item is selected.
 * @param modifier The modifier to be applied to the layout.
 */
@Composable
fun GameSearchItemView(
    item: GameSearchItemUiState,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isClickable = item.addStatus == AddStatus.NOT_ADDED
    val game = item.game

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isClickable) Modifier.clickable(onClick = onSelected) else Modifier)
            .testTag(GameSearchItemViewTestTags.ITEM),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                uri = game.thumbnailUri?.toString()?.toUri(),
                contentDescription = game.name.value,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(GameSearchItemViewTestTags.THUMBNAIL),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name.value,
                    style = MaterialTheme.typography.titleMedium,
                    lineHeight = MaterialTheme.typography.titleSmall.lineHeight,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(GameSearchItemViewTestTags.NAME),
                )
                Spacer(modifier = Modifier.size(2.dp))
                Text(
                    text = item.platform.abbreviation.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(GameSearchItemViewTestTags.PLATFORM),
                )
                val releaseYear = game.releaseDate?.year
                if (releaseYear != null) {
                    Text(
                        text = releaseYear.toString(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag(GameSearchItemViewTestTags.RELEASE_YEAR)
                    )
                }
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                AddStatusIndicator(
                    status = item.addStatus,
                    addDescription = stringResource(R.string.search_game_add_description),
                    addedDescription = stringResource(R.string.search_game_added_description)
                )
            }
        }
    }
}

/**
 * The label of the category, used both to display games and to filter the search by category.
 */
@StringRes
internal fun GameInfo.Category.labelResId(): Int =
    when (this) {
        GameInfo.Category.MAIN_GAME -> R.string.search_game_category_main_game
        GameInfo.Category.DLC -> R.string.search_game_category_dlc
        GameInfo.Category.BUNDLE -> R.string.search_game_category_bundle
        GameInfo.Category.REMAKE -> R.string.search_game_category_remake
        GameInfo.Category.REMASTER -> R.string.search_game_category_remaster
    }

// Testing & preview utilities

object GameSearchItemViewTestTags {
    const val ITEM = "GameSearchItem"
    const val THUMBNAIL = "GameSearchItemThumbnail"
    const val NAME = "GameSearchItemName"
    const val PLATFORM = "GameSearchItemPlatform"
    const val RELEASE_YEAR = "GameSearchItemReleaseYear"
}

@Suppress("Unused")
class GameSearchItemViewPreviews {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(3L, "Switch", "Nintendo Switch")

    private val eldenRing = GameInfo(
        id = 1L,
        name = "Elden Ring",
        releaseDate = LocalDate.of(2022, 2, 25),
        thumbnailUri = "https://fake.url/elden-ring.png",
        category = GameInfo.Category.MAIN_GAME
    )

    @Composable
    private fun PreviewOf(item: GameSearchItemUiState) {
        DemoGameVaultTheme {
            GameSearchItemView(item = item, onSelected = {}, modifier = Modifier.padding(16.dp))
        }
    }

    @Preview(showBackground = true, name = "Not added")
    @Composable
    fun NotAddedPreview() = PreviewOf(GameSearchItemUiState(eldenRing, ps5, AddStatus.NOT_ADDED))

    @Preview(showBackground = true, name = "Adding, platform version")
    @Composable
    fun AddingPreview() = PreviewOf(
        // The version is not displayed
        GameSearchItemUiState(eldenRing, ps5.copy(version = NonBlankString("Pro")), AddStatus.ADDING)
    )

    @Preview(showBackground = true, name = "Added")
    @Composable
    fun AddedPreview() = PreviewOf(GameSearchItemUiState(eldenRing, ps5, AddStatus.ADDED))

    @Preview(showBackground = true, name = "Unknown release date, long name")
    @Composable
    fun NoReleaseDatePreview() = PreviewOf(
        GameSearchItemUiState(
            GameInfo(
                id = 2L,
                name = "The Legend of Zelda: Tears of the Kingdom – Collector's Edition",
                category = GameInfo.Category.BUNDLE,
                releaseDate = LocalDate.of(2025, 2, 25),
            ),
            switch
        )
    )

    @Preview(showBackground = true, name = "Unknown category and release date")
    @Composable
    fun NoCategoryNorReleaseDatePreview() =
        PreviewOf(GameSearchItemUiState(GameInfo(id = 3L, name = "Pong"), switch))
}
