package pt.isel.lei.pdm.nasaapod.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import pt.isel.lei.pdm.nasaapod.R
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@Composable
fun ScreenBase(
    actions: NavigationActions = NavigationActions(),
    content: @Composable () -> Unit
) {
    NasaApodTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { CustomAppTopBar(actions) }

        )

        { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}


data class NavigationActions(
    val backAction: (() -> Unit)? = null,
    val infoAction: (() -> Unit)? = null,
    val refreshAction: (() -> Unit)? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppTopBar(actions: NavigationActions) {
    TopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            if (actions.backAction != null)
                IconButton(onClick = actions.backAction) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                }
        },
        actions = {
            if (actions.refreshAction != null)

                IconButton(onClick = actions.refreshAction) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "")
                }
            if (actions.infoAction != null)
                IconButton(onClick = actions.infoAction) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "")
                }
        }


    )
}
