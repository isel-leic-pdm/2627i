package isel.dei.pdm.demos.gamevault.collection.platforms

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.ADD_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.collection.platforms.PlatformCollectionScreenTestTags.SCREEN_TAG
import isel.dei.pdm.demos.gamevault.domain.PersistenceException
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay

/**
 * The composable function that defines the platform collection screen. It either displays the
 * collection view contents or an error view from which loading the collection can be retried.
 *
 * @param viewModel The view model for the screen.
 * @param onAddPlatform The callback to be invoked when the user requests to add a platform.
 * @param onBack The callback to be invoked when the back button is pressed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformCollectionScreen(
    viewModel: PlatformCollectionViewModel,
    onAddPlatform: () -> Unit,
    onBack: () -> Unit
) {
    val currentState = viewModel.state.collectAsStateWithLifecycle().value

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SCREEN_TAG),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.platform_collection_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(BACK_BUTTON_TAG)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.platform_collection_back_description)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlatform,
                modifier = Modifier.testTag(ADD_BUTTON_TAG)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.platform_collection_add_description)
                )
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()

        when (currentState) {
            is PlatformCollectionScreenState.Error -> ErrorViewOverlay(
                message = stringResource(R.string.platform_collection_error_message),
                tip = stringResource(currentState.error.tipResId()),
                buttonText = stringResource(R.string.platform_collection_retry_button_label),
                onButtonClicked = viewModel::onRetry,
                modifier = contentModifier,
                fillMaxSize = true
            )
            else -> PlatformCollectionScreenView(
                state = currentState,
                // TODO: Navigate to the platform details screen, once it exists
                onPlatformSelected = { },
                modifier = contentModifier
            )
        }
    }
}

/**
 * The tip displayed to the user for the given persistence error, hinting on what can be done about it.
 */
@StringRes
private fun PersistenceException.tipResId(): Int =
    when (this) {
        is RecoverablePersistenceException -> R.string.platform_collection_error_tip_recoverable
        is UnrecoverablePersistenceException -> R.string.platform_collection_error_tip_unrecoverable
    }

// Testing utilities

object PlatformCollectionScreenTestTags {
    const val SCREEN_TAG = "PlatformCollectionScreen"
    const val BACK_BUTTON_TAG = "PlatformCollectionBackButton"
    const val ADD_BUTTON_TAG = "PlatformCollectionAddButton"
}
