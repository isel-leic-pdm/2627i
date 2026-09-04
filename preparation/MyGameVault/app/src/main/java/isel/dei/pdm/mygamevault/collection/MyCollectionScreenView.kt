package isel.dei.pdm.mygamevault.collection

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import java.time.LocalDate

internal const val MY_COLLECTION_SCREEN_TAG = "MyCollectionScreen"
internal const val COLLECTION_LIST_TAG = "CollectionList"
internal const val COLLECTION_FILTERS_TAG = "CollectionFilters"
internal const val COLLECTION_LOADING_TAG = "CollectionLoading"

@Composable
fun MyCollectionScreenView(
    state: MyCollectionScreenState,
    onEntrySelected: (CollectionEntry) -> Unit,
    onFilterChange: (CollectionFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize().testTag(MY_COLLECTION_SCREEN_TAG),
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.my_collection_header),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 16.dp)
                    )
                    HorizontalDivider()
                }

                // Filter Chips
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalFadingEdge()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag(COLLECTION_FILTERS_TAG),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CollectionFilter.entries.forEach { filter ->
                            val (icon, labelRes) = when (filter) {
                                CollectionFilter.LATEST -> Icons.Default.History to R.string.collection_filter_latest
                                CollectionFilter.PLAYING -> Icons.Default.VideogameAsset to R.string.status_playing
                                CollectionFilter.FINISHED -> Icons.Default.CheckCircle to R.string.status_finished
                                CollectionFilter.PLATINUM -> Icons.Default.EmojiEvents to R.string.status_platinum
                                CollectionFilter.BACKLOG -> Icons.Default.Inventory2 to R.string.status_backlog
                            }
                            FilterChip(
                                selected = state.filter == filter,
                                onClick = { onFilterChange(filter) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(labelRes),
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(labelRes))
                                }
                            )
                        }
                    }
                }

                if (state.entries.isEmpty() && state is MyCollectionScreenState.Idle) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.collection_empty_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(COLLECTION_LIST_TAG)
                    ) {
                        items(state.entries) { entry ->
                            CollectionEntryItem(
                                entry = entry,
                                onClick = { onEntrySelected(entry) }
                            )
                        }
                    }
                }
            }

            if (state is MyCollectionScreenState.Loading) {
                CircularProgressIndicator(modifier = Modifier.testTag(COLLECTION_LOADING_TAG))
            }
        }
    }
}

/**
 * A modifier that adds a fading edge to the horizontal ends of a scrollable component.
 */
private fun Modifier.horizontalFadingEdge() = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadeWidth = 32.dp.toPx()
        
        // Right fade
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = size.width,
                endX = size.width - fadeWidth
            ),
            blendMode = BlendMode.DstIn
        )
        
        // Left fade
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black),
                startX = 0f,
                endX = fadeWidth
            ),
            blendMode = BlendMode.DstIn
        )
    }

@Preview(showBackground = true)
@Composable
fun MyCollectionScreenPreview() {
    val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        addedAt = LocalDate.now()
    )
    MyGameVaultTheme {
        MyCollectionScreenView(
            state = MyCollectionScreenState.Idle(listOf(sampleEntry)),
            onEntrySelected = {},
            onFilterChange = {}
        )
    }
}
