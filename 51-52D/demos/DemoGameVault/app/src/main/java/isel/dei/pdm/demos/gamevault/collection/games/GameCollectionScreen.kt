package isel.dei.pdm.demos.gamevault.collection.games

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakePlatformCollectionRepository
import isel.dei.pdm.demos.gamevault.collection.games.GameCollectionScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.collection.games.parts.filters.activeFilters
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.home.HomeScreenSampleData
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The composable function that defines the games collection screen: the app bar (see
 * [GameCollectionTopBar]) and, depending on the state, the collection view contents (see
 * [GameCollectionScreenView]), a loading indicator, or an error view from which reading the
 * collection can be retried.
 *
 * Whether the filters sheet is open is a concern of the UI alone, so it is kept here, surviving
 * configuration changes, instead of in the view model. The filters can only be opened while
 * browsing, as the view (where the sheet is displayed) is not displayed otherwise.
 *
 * DRAFT: The callbacks of screens that do not exist yet default to no-ops.
 *
 * @param viewModel The view model for the screen.
 * @param onBack The callback to be invoked when the back button is pressed.
 * @param onGameSelected The callback to be invoked when the user selects a game of the collection.
 */
@Composable
fun GameCollectionScreen(
    viewModel: GameCollectionViewModel,
    onBack: () -> Unit,
    onGameSelected: (CollectionItem) -> Unit = { }
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    var isFiltersSheetOpen by rememberSaveable { mutableStateOf(false) }

    // The sheet is closed when the view leaves the screen (e.g. on error), so that it does not
    // reopen by itself when browsing resumes (e.g. after retrying)
    val isBrowsing = currentState is GameCollectionScreenState.Browsing
    LaunchedEffect(isBrowsing) {
        if (!isBrowsing) isFiltersSheetOpen = false
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SCREEN_TAG),
        topBar = {
            GameCollectionTopBar(
                activeFilterCount =
                    (currentState as? GameCollectionScreenState.Browsing)?.query?.activeFilters()?.size ?: 0,
                onOpenFilters = { if (isBrowsing) isFiltersSheetOpen = true },
                onBack = onBack
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentState) {
            is GameCollectionScreenState.Loading -> Box(modifier = contentModifier) {
                LabelledProgressIndicator(
                    text = stringResource(R.string.game_collection_loading),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(GameCollectionScreenViewTestTags.LOADING_INDICATOR)
                )
            }
            is GameCollectionScreenState.Browsing -> GameCollectionScreenView(
                query = currentState.query,
                sections = currentState.sections,
                platforms = currentState.platforms,
                isFiltersSheetOpen = isFiltersSheetOpen,
                onQueryChange = viewModel::onQueryChange,
                onItemSelected = onGameSelected,
                onFiltersSheetDismissed = { isFiltersSheetOpen = false },
                modifier = contentModifier
            )
            is GameCollectionScreenState.Error -> ErrorViewOverlay(
                message = stringResource(R.string.game_collection_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.game_collection_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
        }
    }
}

/**
 * The tip displayed to the user for the given persistence error, hinting on what can be done
 * about it. The tips are those of the platform collection screen, as the storage is the same.
 */
@StringRes
private fun PersistenceException.tipResId(): Int =
    when (this) {
        is RecoverablePersistenceException -> R.string.platform_collection_error_tip_recoverable
        is UnrecoverablePersistenceException -> R.string.platform_collection_error_tip_unrecoverable
    }

// Testing & preview utilities

object GameCollectionScreenTestTags {
    const val SCREEN_TAG = "GameCollectionScreen"
}

@Suppress("Unused")
class GameCollectionScreenPreviews {

    @Preview(showBackground = true, showSystemUi = true, name = "Games collection screen")
    @Composable
    fun GameCollectionScreenPreview() {
        DemoGameVaultTheme {
            GameCollectionScreen(
                viewModel = GameCollectionViewModel(
                    gameRepo = FakeGameCollectionRepository(HomeScreenSampleData.entries),
                    platformRepo = FakePlatformCollectionRepository(),
                    savedStateHandle = SavedStateHandle()
                ),
                onBack = {}
            )
        }
    }
}
