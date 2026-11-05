package isel.dei.pdm.demos.gamevault.search.platforms

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
import isel.dei.pdm.demos.gamevault.domain.search.NoConnectivityException
import isel.dei.pdm.demos.gamevault.domain.search.RateLimitExceededException
import isel.dei.pdm.demos.gamevault.domain.search.SearchServiceException
import isel.dei.pdm.demos.gamevault.domain.search.ServiceUnavailableException
import isel.dei.pdm.demos.gamevault.domain.search.UnauthenticatedException
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.search.platforms.SearchForPlatformScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay

/**
 * The composable function that defines the search platforms screen. It either displays the search
 * view contents or an error view from which the search can be retried.
 *
 * Add outcomes are reported in a snackbar regardless of the state, because additions complete
 * independently of the search (e.g. an addition may complete after a new search failed).
 *
 * @param viewModel The view model for the screen.
 * @param onBack The callback to be invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchForPlatformScreen(
    viewModel: SearchForPlatformViewModel,
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
                title = { Text(stringResource(R.string.search_platform_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(BACK_BUTTON_TAG)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.search_platform_back_description)
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
            is SearchForPlatformScreenState.Error -> ErrorViewOverlay(
                message = stringResource(R.string.search_platform_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.search_platform_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
            else -> SearchForPlatformScreenView(
                state = currentState,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onSearchQueryCleared = viewModel::onSearchQueryCleared,
                onAddPlatform = viewModel::onAddPlatform,
                modifier = contentModifier
            )
        }
    }
}

/**
 * The message used to report the given add outcome to the user.
 */
@Composable
private fun addOutcomeMessageOf(outcome: AddPlatformOutcome): String =
    when (outcome) {
        is AddPlatformOutcome.Added ->
            stringResource(R.string.search_platform_added_message, outcome.platform.displayName)
        is AddPlatformOutcome.Failed ->
            stringResource(R.string.search_platform_add_failed_message, outcome.platform.displayName)
    }

/**
 * The name used to refer to the platform in messages, which includes its version, if any.
 */
private val PlatformInfo.displayName: String
    get() = version?.let { "${name.value} ${it.value}" } ?: name.value

/**
 * The tip displayed to the user for the given search error, hinting on what can be done about it.
 */
@StringRes
private fun SearchServiceException.tipResId(): Int =
    when (this) {
        is NoConnectivityException -> R.string.search_error_tip_no_connectivity
        is UnauthenticatedException -> R.string.search_error_tip_unauthenticated
        is ServiceUnavailableException -> R.string.search_error_tip_service_unavailable
        is RateLimitExceededException -> R.string.search_error_tip_rate_limit_exceeded
        is UnexpectedServiceException -> R.string.search_error_tip_unexpected
    }

// Testing utilities

object SearchForPlatformScreenTestTags {
    const val SCREEN_TAG = "SearchForPlatformScreen"
    const val BACK_BUTTON_TAG = "SearchForPlatformBackButton"
}
