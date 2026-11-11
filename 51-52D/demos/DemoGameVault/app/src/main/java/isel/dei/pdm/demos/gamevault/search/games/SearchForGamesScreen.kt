package isel.dei.pdm.demos.gamevault.search.games

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.games.SearchForGamesScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.search.tipResId
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay

/**
 * The composable function that defines the search games screen. It either displays the search
 * view contents or an error view from which the failed operation can be retried: searching, or
 * reading the platforms of the collection.
 *
 * Add outcomes are reported in a snackbar regardless of the state, because additions complete
 * independently of the search (e.g. an addition may complete after a new search failed).
 *
 * @param viewModel The view model for the screen.
 * @param onAddPlatform The callback to be invoked when the user requests to add a platform to the
 * collection, which is offered when there are none.
 * @param onBack The callback to be invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchForGamesScreen(
    viewModel: SearchForGamesViewModel,
    onAddPlatform: () -> Unit,
    onBack: () -> Unit
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value
    val currentAddOutcome = viewModel.addOutcome.collectAsStateWithLifecycle().value

    val snackbarHostState = remember { SnackbarHostState() }
    val addOutcomeMessage = currentAddOutcome?.let { addOutcomeMessageOf(it) }
    LaunchedEffect(currentAddOutcome) {
        if (addOutcomeMessage != null) {
            snackbarHostState.showSnackbar(addOutcomeMessage)
            viewModel.onAddOutcomeShown()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SCREEN_TAG),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_game_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(BACK_BUTTON_TAG)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_game_back_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentState) {
            is SearchForGamesScreenState.PlatformsUnavailable -> ErrorViewOverlay(
                message = stringResource(R.string.search_game_platforms_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.search_game_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
            is SearchForGamesScreenState.Error -> ErrorViewOverlay(
                message = stringResource(R.string.search_game_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.search_game_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
            else -> SearchForGamesScreenView(
                state = currentState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchQueryCleared = viewModel::onSearchQueryCleared,
                onPlatformSelected = viewModel::onPlatformSelected,
                onCategorySelected = viewModel::onCategorySelected,
                onAddGame = viewModel::onAddGame,
                onAddPlatform = onAddPlatform,
                modifier = contentModifier
            )
        }
    }
}

/**
 * The message used to report the given add outcome to the user. It names the platform, as the
 * same game may be added on several platforms.
 */
@Composable
private fun addOutcomeMessageOf(outcome: AddGameOutcome): String {
    val game = outcome.game.name.value
    val platform = outcome.platform.abbreviation.value
    return when (outcome) {
        is AddGameOutcome.Added ->
            stringResource(R.string.search_game_added_message, game, platform)
        is AddGameOutcome.Failed ->
            stringResource(R.string.search_game_add_failed_message, game, platform)
    }
}

/**
 * The tip displayed to the user when the platforms cannot be read, hinting on what can be done
 * about it. The tips are those of the platform collection screen, as the error is the same.
 */
@StringRes
private fun PersistenceException.tipResId(): Int =
    when (this) {
        is RecoverablePersistenceException -> R.string.platform_collection_error_tip_recoverable
        is UnrecoverablePersistenceException -> R.string.platform_collection_error_tip_unrecoverable
    }

// Testing utilities

object SearchForGamesScreenTestTags {
    const val SCREEN_TAG = "SearchForGamesScreen"
    const val BACK_BUTTON_TAG = "SearchForGamesBackButton"
}
