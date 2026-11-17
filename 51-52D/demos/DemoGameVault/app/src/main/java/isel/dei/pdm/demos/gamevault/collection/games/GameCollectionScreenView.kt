package isel.dei.pdm.demos.gamevault.collection.games

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.games.filters.ActiveFiltersRow
import isel.dei.pdm.demos.gamevault.collection.games.filters.FiltersButton
import isel.dei.pdm.demos.gamevault.collection.games.filters.FiltersSheetContent
import isel.dei.pdm.demos.gamevault.collection.games.filters.activeFilters
import isel.dei.pdm.demos.gamevault.collection.games.filters.without
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Completion
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.search.toSearchStringOrNull
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.common.SearchBox
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The app bar of the games collection screen: its title, the back navigation and the access to
 * the filters (see [FiltersButton]), whose badge tells how many are applied.
 *
 * @param activeFilterCount The number of filters currently applied.
 * @param onOpenFilters Callback invoked when the user asks for the filters.
 * @param onBack Callback invoked when the user asks to navigate back.
 * @param modifier Modifier to be applied to the app bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCollectionTopBar(
    activeFilterCount: Int,
    onOpenFilters: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(R.string.game_collection_title)) },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag(GameCollectionScreenViewTestTags.BACK_BUTTON)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.game_collection_back_description)
                )
            }
        },
        actions = {
            FiltersButton(activeFilterCount = activeFilterCount, onClick = onOpenFilters)
        },
        modifier = modifier
    )
}

/**
 * The view used to browse the games collection, below the app bar (see [GameCollectionTopBar]):
 * - The search box, always displayed, to search the games by name.
 * - The filters applied, as removable chips (see [ActiveFiltersRow]), only when there are any.
 * - The games that match the [query], grouped by initial (see [CollectionList]), or a message
 * explaining why there are none (see [CollectionEmptyView]).
 * - The sheet with all the filters (see [FiltersSheetContent]), when [isFiltersSheetOpen].
 *
 * Every change to the query (i.e. searching, adding or removing filters, clearing them all) is
 * emitted as the whole new query, through [onQueryChange], and applied immediately (i.e. there is
 * no confirmation). The view displays whatever [query] it is given, so it is up to the caller to
 * keep it.
 *
 * Clearing all the filters also clears the search, so the search box is then recreated empty, as
 * it only takes the query it displays when it enters the composition (see [SearchBox]).
 *
 * @param query The query whose games are displayed.
 * @param sections The games that match the [query], grouped by initial, or null while they are
 * being loaded.
 * @param platforms The platforms offered in the filters sheet (see [FiltersSheetContent]).
 * @param isFiltersSheetOpen Whether the sheet with all the filters is displayed.
 * @param onQueryChange Callback invoked with the new query when the user changes it.
 * @param onItemSelected Callback invoked when the user selects a game.
 * @param onFiltersSheetDismissed Callback invoked when the user dismisses the filters sheet.
 * @param modifier Modifier to be applied to the view's layout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameCollectionScreenView(
    query: CollectionQuery,
    sections: List<CollectionSection>?,
    platforms: List<Platform>,
    isFiltersSheetOpen: Boolean,
    onQueryChange: (CollectionQuery) -> Unit,
    onItemSelected: (CollectionItem) -> Unit,
    onFiltersSheetDismissed: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Incremented whenever the search is cleared from outside the search box, so that the box is
    // recreated empty. Once incremented, the box no longer takes the query's search, which was
    // only meant for its first appearance (e.g. when arriving at the screen).
    var searchBoxGeneration by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        key(searchBoxGeneration) {
            SearchBox(
                onQueryChange = { onQueryChange(query.copy(nameContains = NonBlankString(it.value))) },
                onQueryCleared = { onQueryChange(query.copy(nameContains = null)) },
                initialQuery =
                    if (searchBoxGeneration == 0) query.nameContains?.value?.toSearchStringOrNull()
                    else null
            )
        }

        ActiveFiltersRow(
            filters = query.activeFilters(),
            onRemoveFilter = { onQueryChange(query.without(it)) },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                sections == null -> LabelledProgressIndicator(
                    text = stringResource(R.string.game_collection_loading),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(GameCollectionScreenViewTestTags.LOADING_INDICATOR)
                )
                sections.isEmpty() -> CollectionEmptyView(
                    isFiltered = query != CollectionQuery(),
                    onClearFilters = {
                        if (query.nameContains != null) searchBoxGeneration++
                        onQueryChange(CollectionQuery())
                    }
                )
                else -> CollectionList(sections = sections, onItemSelected = onItemSelected)
            }
        }
    }

    if (isFiltersSheetOpen) {
        ModalBottomSheet(onDismissRequest = onFiltersSheetDismissed) {
            FiltersSheetContent(
                state = query.state,
                completion = query.completion,
                platform = query.platform,
                platforms = platforms,
                onStateSelected = { onQueryChange(query.copy(state = it)) },
                onCompletionSelected = { onQueryChange(query.copy(completion = it)) },
                onPlatformSelected = { onQueryChange(query.copy(platform = it)) }
            )
        }
    }
}

// Testing & preview utilities

object GameCollectionScreenViewTestTags {
    const val BACK_BUTTON = "GameCollectionBackButton"
    const val LOADING_INDICATOR = "GameCollectionLoadingIndicator"
}

@Suppress("Unused")
class GameCollectionScreenViewPreviews {

    private val platforms = listOf(
        Platform(1L, "PS5", "PlayStation 5"),
        Platform(2L, "Switch", "Nintendo Switch"),
        Platform(3L, "PC", "PC (Microsoft Windows)")
    )

    private val names = listOf(
        "1080° Snowboarding", "Alan Wake 2", "Astro Bot", "Baldur's Gate 3", "Bloodborne",
        "Celeste", "Control", "Cyberpunk 2077", "Dead Cells", "Death Stranding", "Elden Ring",
        "Final Fantasy VII Rebirth", "God of War", "Hades", "Hades II", "Hollow Knight",
        "Inside", "Journey", "Kena", "Limbo", "Metroid Dread", "Nier: Automata", "Ori",
        "Persona 5", "Returnal", "Sekiro", "Tetris Effect", "Uncharted 4", "Zelda"
    )

    // States compatible with being finished, so that the samples are consistent
    private val sampleStates = listOf(
        PlayStatus.State.BACKLOG,
        PlayStatus.State.PLAYING,
        PlayStatus.State.PAUSED,
        PlayStatus.State.DROPPED
    )

    private fun sampleItem(index: Int, name: String) = CollectionItem(
        gameId = index.toLong(),
        name = NonBlankString(name),
        releaseYear = null,
        coverUri = null,
        thumbnailUri = null,
        platformAbbreviations = listOf(NonBlankString("PS5")),
        state = sampleStates[index % sampleStates.size],
        timeSpent = PlayTime(hours = index * 3),
        isFinished = index % 3 == 0,
        hasPlatinum = index % 6 == 0,
        isSessionActive = false
    )

    private val sections: List<CollectionSection>
        get() = names.mapIndexed(::sampleItem).toSections()

    /**
     * The view as displayed by the screen, below its app bar. The filters sheet is not previewed,
     * as it is displayed in a window of its own (see FiltersSheetContentPreviews for its contents).
     */
    @Composable
    private fun PreviewOf(
        query: CollectionQuery = CollectionQuery(),
        sections: List<CollectionSection>? = this.sections
    ) {
        DemoGameVaultTheme {
            Scaffold(
                topBar = {
                    GameCollectionTopBar(
                        activeFilterCount = query.activeFilters().size,
                        onOpenFilters = {},
                        onBack = {}
                    )
                }
            ) { innerPadding ->
                GameCollectionScreenView(
                    query = query,
                    sections = sections,
                    platforms = platforms,
                    isFiltersSheetOpen = false,
                    onQueryChange = {},
                    onItemSelected = {},
                    onFiltersSheetDismissed = {},
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    @Preview(showBackground = true, showSystemUi = true, name = "Browse all")
    @Composable
    fun BrowseAllPreview() = PreviewOf()

    @Preview(showBackground = true, showSystemUi = true, name = "Filtered (e.g. from a shortcut)")
    @Composable
    fun FilteredPreview() = PreviewOf(
        query = CollectionQuery(
            state = PlayStatus.State.PLAYING,
            completion = Completion.FINISHED,
            platform = platforms[0]
        ),
        // Replays of finished games, as the filters require
        sections = names.take(5).mapIndexed { index, name ->
            sampleItem(index, name).copy(state = PlayStatus.State.PLAYING, isFinished = true)
        }.toSections()
    )

    @Preview(showBackground = true, showSystemUi = true, name = "Loading")
    @Composable
    fun LoadingPreview() = PreviewOf(sections = null)

    @Preview(showBackground = true, showSystemUi = true, name = "No matches")
    @Composable
    fun NoMatchesPreview() = PreviewOf(
        query = CollectionQuery(completion = Completion.PLATINUM),
        sections = emptyList()
    )

    @Preview(showBackground = true, showSystemUi = true, name = "Empty collection")
    @Composable
    fun EmptyCollectionPreview() = PreviewOf(sections = emptyList())
}
