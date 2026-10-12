package pt.isel.lei.pdm.nasaapod.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import pt.isel.lei.pdm.nasaapod.ui.main.ErrorContent
import pt.isel.lei.pdm.nasaapod.ui.main.IdleContent
import pt.isel.lei.pdm.nasaapod.ui.main.ImageContent
import pt.isel.lei.pdm.nasaapod.ui.main.LoadingContent
import pt.isel.lei.pdm.nasaapod.ui.theme.NasaApodTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenBase(
    onRefresh: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    onInfo: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    NasaApodTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = stringResource(R.string.app_name))
                    },
                    navigationIcon = {
                        if (onBack != null)
                            IconButton(onClick = { onBack() }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
                            }
                    },
                    actions = {
                        if (onRefresh != null)
                            IconButton(onClick = { onRefresh() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "")
                            }
                        if (onInfo != null)
                            IconButton(onClick = { onInfo() }) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "")
                            }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
            ) {
                content()
            }

        }
    }
}