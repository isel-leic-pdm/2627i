package isel.dei.pdm.mygamevault.add.details

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.ports.NoConnectivityException
import isel.dei.pdm.mygamevault.ports.UnauthenticatedException
import isel.dei.pdm.mygamevault.ui.common.FatalErrorView

/**
 * Screen that displays the catalog information for a game.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(
    viewModel: GameDetailsViewModel,
    onBackRequested: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state is GameDetailsScreenState.Error) {
        val error = (state as GameDetailsScreenState.Error).error
        val errorMessage = when (error) {
            is UnauthenticatedException -> stringResource(R.string.error_storage_access)
            is NoConnectivityException -> stringResource(R.string.error_no_connectivity)
            else -> stringResource(R.string.error_storage_load_failed)
        }
        FatalErrorView(
            message = errorMessage,
            buttonText = stringResource(R.string.retry_label),
            onButtonClicked = viewModel::loadDetails
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.game_details_screen_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBackRequested) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.game_details_back)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            GameDetailsScreenView(
                state = state,
                onRetryRequested = viewModel::loadDetails,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
