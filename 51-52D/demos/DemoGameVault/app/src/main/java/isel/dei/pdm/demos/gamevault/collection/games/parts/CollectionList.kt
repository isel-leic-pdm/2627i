package isel.dei.pdm.demos.gamevault.collection.games.parts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.CollectionSection
import isel.dei.pdm.demos.gamevault.collection.games.shouldShowInitialsIndex
import isel.dei.pdm.demos.gamevault.collection.games.toSections
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.launch

/**
 * The listing of the collection's games, grouped in sections by initial (see [toSections]), each
 * one headed by its initial, which stays at the top while its items are scrolled (i.e. a sticky
 * header).
 *
 * Long listings also display an index of their initials along their end edge (see
 * [InitialsIndex]), to jump to the section of an initial. The index is only displayed when the
 * listing is long enough to need it (see [shouldShowInitialsIndex]). The listing always reserves
 * the index's width at its end, so that its items do not change width as the index is displayed
 * or hidden.
 *
 * Without sections, nothing is displayed: an empty collection, or a search without results, is
 * explained by the caller (see [CollectionEmptyView]).
 *
 * @param sections The sections to be displayed, in the order they are to be displayed.
 * @param onItemSelected Callback invoked when the user selects an item.
 * @param modifier Modifier to be applied to the listing.
 * @param state The state of the listing's scroll, hoisted so that the caller can, for example,
 * scroll it back to the top when the results change.
 */
@Composable
fun CollectionList(
    sections: List<CollectionSection>,
    onItemSelected: (CollectionItem) -> Unit,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState()
) {
    if (sections.isEmpty()) return

    val scope = rememberCoroutineScope()

    // The position of each section's header in the listing: each section takes one position for
    // its header, plus one per item
    val headerPositions = remember(sections) {
        sections.runningFold(0) { position, section -> position + 1 + section.items.size }
            .zip(sections) { position, section -> section.initial to position }
            .toMap()
    }

    // Whether the index is displayed depends on the listing's size in screens, which is only
    // known once it is laid out, and on whether it was already displayed (see
    // shouldShowInitialsIndex). Saved, so that the index does not blink on configuration changes.
    var isIndexShown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state, sections.size) {
        snapshotFlow { state.layoutInfo.totalItemsCount to state.layoutInfo.visibleItemsInfo.size }
            .collect { (totalItems, visibleItems) ->
                isIndexShown = shouldShowInitialsIndex(
                    totalItems = totalItems,
                    visibleItems = visibleItems,
                    initials = sections.size,
                    isShown = isIndexShown
                )
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .testTag(CollectionListTestTags.LIST),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = InitialsIndexWidth + 8.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEach { section ->
                stickyHeader(key = "initial:${section.initial}", contentType = "header") {
                    SectionHeader(initial = section.initial)
                }
                items(
                    items = section.items,
                    key = { it.gameId },
                    contentType = { "item" }
                ) { item ->
                    CollectionItemView(item = item, onSelected = { onItemSelected(item) })
                }
            }
        }

        AnimatedVisibility(
            visible = isIndexShown,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp)
        ) {
            InitialsIndex(
                initials = sections.map { it.initial },
                onInitialSelected = { initial ->
                    headerPositions[initial]?.let { position ->
                        // Instantaneous, so that dragging along the index follows the finger
                        scope.launch { state.scrollToItem(position) }
                    }
                }
            )
        }
    }
}

/**
 * The header of a section: its initial, on an opaque background, as the items scroll under it.
 */
@Composable
private fun SectionHeader(initial: Char) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CollectionListTestTags.HEADER)
    ) {
        Text(
            text = initial.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = 4.dp)
                .semantics { heading() }
        )
    }
}

/**
 * The message displayed in place of the listing when there are no games to list, in the center
 * of the available area. It tells apart an empty collection from a search or filters that match
 * no games, in which case it also offers to clear them, so that the user is never stuck on an
 * empty screen.
 *
 * @param isFiltered Whether the listing is filtered (i.e. by a search or filters), as opposed to
 * the collection being empty.
 * @param onClearFilters Callback invoked when the user asks to clear the search and filters.
 * Only offered when [isFiltered].
 * @param modifier Modifier to be applied to the layout.
 */
@Composable
fun CollectionEmptyView(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .testTag(CollectionListTestTags.EMPTY),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(if (isFiltered) R.string.collection_no_matches else R.string.collection_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (isFiltered) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.testTag(CollectionListTestTags.CLEAR_FILTERS_BUTTON)
            ) {
                Text(stringResource(R.string.collection_clear_filters_label))
            }
        }
    }
}

// Testing & preview utilities

object CollectionListTestTags {
    const val LIST = "CollectionList"
    const val HEADER = "CollectionListHeader"
    const val EMPTY = "CollectionEmptyView"
    const val CLEAR_FILTERS_BUTTON = "CollectionClearFiltersButton"
}

@Suppress("Unused")
class CollectionListPreviews {

    private val names = listOf(
        "1080° Snowboarding", "Alan Wake 2", "Astro Bot", "Baldur's Gate 3", "Bloodborne",
        "Celeste", "Control", "Cyberpunk 2077", "Dead Cells", "Death Stranding", "Elden Ring",
        "Final Fantasy VII Rebirth", "God of War", "Hades", "Hades II", "Hollow Knight",
        "Inside", "Journey", "Kena", "Limbo", "Metroid Dread", "Nier: Automata", "Ori",
        "Persona 5", "Returnal", "Sekiro", "Tetris Effect", "Uncharted 4", "Zelda"
    )

    private fun sampleItem(index: Int, name: String) = CollectionItem(
        gameId = index.toLong(),
        name = NonBlankString(name),
        releaseYear = null,
        coverUri = null,
        thumbnailUri = null,
        platformAbbreviations = listOf(NonBlankString("PS5")),
        state = PlayStatus.State.BACKLOG,
        timeSpent = PlayTime(hours = index * 3),
        isFinished = false,
        hasPlatinum = false,
        isSessionActive = false
    )

    private val items: List<CollectionItem>
        get() = names.mapIndexed(::sampleItem)

    @Preview(showBackground = true, heightDp = 640, name = "Long listing, with index")
    @Composable
    fun LongListingPreview() {
        DemoGameVaultTheme {
            CollectionList(sections = items.toSections(), onItemSelected = {})
        }
    }

    @Preview(showBackground = true, heightDp = 640, name = "Short listing, without index")
    @Composable
    fun ShortListingPreview() {
        DemoGameVaultTheme {
            CollectionList(sections = items.take(4).toSections(), onItemSelected = {})
        }
    }

    @Preview(showBackground = true, heightDp = 320, name = "Empty collection")
    @Composable
    fun EmptyCollectionPreview() {
        DemoGameVaultTheme {
            CollectionEmptyView(isFiltered = false, onClearFilters = {})
        }
    }

    @Preview(showBackground = true, heightDp = 320, name = "No matches")
    @Composable
    fun NoMatchesPreview() {
        DemoGameVaultTheme {
            CollectionEmptyView(isFiltered = true, onClearFilters = {})
        }
    }
}
