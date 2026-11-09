package isel.dei.pdm.demos.gamevault.collection.platforms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.EMPTY_MESSAGE_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.LOADING_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenViewTestTags.PLATFORM_LIST_TAG
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.common.PlatformListItem
import isel.dei.pdm.demos.gamevault.ui.common.toListItemModel
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The view used to display the platforms in the user's collection.
 *
 * @param state The current state of the screen.
 * @param onPlatformSelected Callback invoked when the user selects a platform of the collection.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun PlatformCollectionScreenView(
    state: PlatformCollectionScreenState,
    onPlatformSelected: (Platform) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (state) {
            is PlatformCollectionScreenState.Loading -> LabelledProgressIndicator(
                text = stringResource(R.string.platform_collection_loading),
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag(LOADING_INDICATOR_TAG)
            )
            is PlatformCollectionScreenState.Results ->
                if (state.platforms.isEmpty()) {
                    CenteredMessage(
                        text = stringResource(R.string.platform_collection_empty),
                        modifier = Modifier.testTag(EMPTY_MESSAGE_TAG)
                    )
                } else {
                    PlatformList(
                        platforms = state.platforms,
                        onPlatformSelected = onPlatformSelected
                    )
                }
            // Displayed by the screen, in place of this view
            is PlatformCollectionScreenState.Error -> Unit
        }
    }
}

/**
 * The list of platforms in the collection.
 */
@Composable
private fun PlatformList(
    platforms: List<Platform>,
    onPlatformSelected: (Platform) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag(PLATFORM_LIST_TAG),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = platforms, key = { it.id }) { platform ->
            PlatformListItem(
                item = platform.toListItemModel(),
                onSelected = { onPlatformSelected(platform) }
            )
        }
    }
}

/**
 * A message displayed in the center of the available area.
 */
@Composable
private fun CenteredMessage(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Testing & preview utilities

object PlatformCollectionScreenViewTestTags {
    const val PLATFORM_LIST_TAG = "PlatformCollectionList"
    const val LOADING_INDICATOR_TAG = "PlatformCollectionLoadingIndicator"
    const val EMPTY_MESSAGE_TAG = "PlatformCollectionEmptyMessage"
}

@Suppress("Unused")
class PlatformCollectionScreenViewPreviews {

    private val samplePlatforms = listOf(
        Platform(1L, "PS5", "PlayStation 5", "https://fake.url/ps5.png", 2021, "Pro"),
        Platform(2L, "Switch", "Nintendo Switch", "https://fake.url/switch.png", 2018),
        Platform(3L, "PC", "PC (Microsoft Windows)")
    )

    @Composable
    private fun PreviewOf(state: PlatformCollectionScreenState) {
        DemoGameVaultTheme {
            PlatformCollectionScreenView(state = state, onPlatformSelected = {})
        }
    }

    @Preview(showBackground = true, name = "Loading")
    @Composable
    fun LoadingPreview() = PreviewOf(PlatformCollectionScreenState.Loading)

    @Preview(showBackground = true, name = "Results")
    @Composable
    fun ResultsPreview() = PreviewOf(PlatformCollectionScreenState.Results(samplePlatforms))

    @Preview(showBackground = true, name = "Results - Empty")
    @Composable
    fun EmptyResultsPreview() = PreviewOf(PlatformCollectionScreenState.Results(emptyList()))
}
