package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.toItem
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import java.time.LocalDate

/**
 * The maximum font scale used in the text displayed on top of the covers. Together with the
 * maximum number of lines, it bounds the area of the cover taken by the text, even when the user
 * chooses a very large font size. The full information is available in the game's screen.
 */
const val MAX_OVERLAY_FONT_SCALE = 1.3f

/**
 * The carousel's height, unless one is specified through its modifier. Exposed so that the
 * contents displayed in its place (e.g. while loading) can take the same space, preventing the
 * layout from shifting when they are replaced by the carousel.
 */
val DEFAULT_CAROUSEL_HEIGHT: Dp = 220.dp

/**
 * A carousel of collection items, each displayed as the game's cover with its name, platform
 * and play time on top of it. Uses Material 3's multi-browse carousel: the items shrink and are
 * masked (clipped) as they scroll towards the carousel's edges.
 *
 * @param items The collection items to be displayed.
 * @param onItemSelected Callback invoked when the user selects an item.
 * @param modifier Modifier to be applied to the carousel's layout. Its height defines the
 * height of the items.
 * @param contentPadding The padding around the carousel's items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCarousel(
    items: List<CollectionItem>,
    onItemSelected: (CollectionItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.size },
        preferredItemWidth = 160.dp,
        itemSpacing = 8.dp,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .height(DEFAULT_CAROUSEL_HEIGHT)
            .testTag(GameCarouselTestTags.CAROUSEL)
    ) { index ->
        val item = items[index]
        GameCarouselItem(item = item, onSelected = { onItemSelected(item) })
    }
}

/**
 * An item of the [GameCarousel], displaying a collection item.
 * PLACEHOLDER: The cover is a placeholder box, to be replaced by the game's cover image.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarouselItemScope.GameCarouselItem(item: CollectionItem, onSelected: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .maskClip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onSelected)
            .testTag(GameCarouselTestTags.ITEM)
    ) {
        Icon(
            imageVector = Icons.Filled.SportsEsports,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // The text's font scale is capped, so that the text never takes over the cover
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = density.fontScale.coerceAtMost(MAX_OVERLAY_FONT_SCALE)
            )
        ) {
            // A scrim makes the text readable on top of any cover. The text fades out as the item
            // shrinks, so that it is not displayed clipped in the smaller items.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .graphicsLayer {
                        val info = carouselItemDrawInfo
                        val visibleFraction = if (info.maxSize > 0f) info.size / info.maxSize else 1f
                        // Hidden below 60% of the full size, fully visible at the full size
                        alpha = ((visibleFraction - 0.6f) / 0.4f).coerceIn(0f, 1f)
                    }
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                    .padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 12.dp)
            ) {
                Text(
                    text = item.name.value,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(GameCarouselTestTags.TITLE)
                )
                Text(
                    text = stringResource(
                        R.string.game_card_platform_and_play_time,
                        item.platformAbbreviation.value,
                        item.timeSpent.hours,
                        item.timeSpent.minutes
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag(GameCarouselTestTags.DETAILS)
                )
            }
        }
    }
}

// Testing & preview utilities

object GameCarouselTestTags {
    const val CAROUSEL = "GameCarousel"
    const val ITEM = "GameCarouselItem"
    const val TITLE = "GameCarouselItemTitle"
    const val DETAILS = "GameCarouselItemDetails"
}

@Suppress("Unused")
class GameCarouselPreviews {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")

    private val sampleItems = listOf(
        CollectionEntry(
            game = Game(1L, "Elden Ring", LocalDate.of(2022, 2, 25), coverUri = null),
            platform = ps5,
            playStatus = PlayStatus(PlayTime(42, 10), PlayStatus.State.PLAYING)
        ),
        CollectionEntry(
            game = Game(2L, "The Legend of Zelda: Tears of the Kingdom", LocalDate.of(2023, 5, 12), coverUri = null),
            platform = switch,
            playStatus = PlayStatus(PlayTime(87, 45), PlayStatus.State.PLAYING)
        ),
        CollectionEntry(
            game = Game(3L, "Hades II", LocalDate.of(2025, 9, 25), coverUri = null),
            platform = ps5,
            playStatus = PlayStatus(PlayTime(12, 5), PlayStatus.State.PLAYING)
        )
    ).map { it.toItem() }

    @Preview(showBackground = true, name = "Several entries")
    @Composable
    fun SeveralEntriesPreview() {
        DemoGameVaultTheme {
            GameCarousel(items = sampleItems, onItemSelected = {})
        }
    }

    @Preview(showBackground = true, name = "Single entry")
    @Composable
    fun SingleEntryPreview() {
        DemoGameVaultTheme {
            GameCarousel(items = sampleItems.take(1), onItemSelected = {})
        }
    }
}
