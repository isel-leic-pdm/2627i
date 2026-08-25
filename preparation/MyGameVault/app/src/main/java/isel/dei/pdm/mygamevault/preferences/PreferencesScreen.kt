package isel.dei.pdm.mygamevault.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.ports.StorageAccessException
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

internal const val PREFERENCES_SCREEN_TAG = "PreferencesScreen"
internal const val CLIENT_ID_INPUT_TAG = "ClientIdInput"
internal const val CLIENT_SECRET_INPUT_TAG = "ClientSecretInput"
internal const val SAVE_BUTTON_TAG = "SaveButton"
internal const val LOADING_INDICATOR_TAG = "LoadingIndicator"

/**
 * Screen for editing the application preferences (IGDB API keys).
 */
@Composable
fun PreferencesScreen(
    state: PreferencesScreenState,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var secretVisible by remember { mutableStateOf(false) }

    val errorMessage = if (state is PreferencesScreenState.Error) {
        when (state.error) {
            is StorageAccessException -> stringResource(R.string.error_storage_access)
            else -> stringResource(R.string.error_generic)
        }
    } else null

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(PREFERENCES_SCREEN_TAG),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (state is PreferencesScreenState.Loading) {
                CircularProgressIndicator(modifier = Modifier.testTag(LOADING_INDICATOR_TAG))
            } else {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.preferences_title),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Spacer(modifier = Modifier.size(24.dp))

                    OutlinedTextField(
                        value = state.clientId,
                        onValueChange = onClientIdChange,
                        label = { Text(stringResource(R.string.preferences_client_id_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CLIENT_ID_INPUT_TAG),
                        enabled = state.isInputEnabled,
                        isError = !state.isClientIdValid,
                        supportingText = if (!state.isClientIdValid) {
                            { Text(stringResource(R.string.preferences_error_blank)) }
                        } else null
                    )

                    Spacer(modifier = Modifier.size(16.dp))

                    OutlinedTextField(
                        value = state.clientSecret,
                        onValueChange = onClientSecretChange,
                        label = { Text(stringResource(R.string.preferences_client_secret_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CLIENT_SECRET_INPUT_TAG),
                        enabled = state.isInputEnabled,
                        isError = !state.isClientSecretValid,
                        supportingText = if (!state.isClientSecretValid) {
                            { Text(stringResource(R.string.preferences_error_blank)) }
                        } else null,
                        visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (secretVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (secretVisible) {
                                stringResource(R.string.preferences_hide_secret)
                            } else {
                                stringResource(R.string.preferences_show_secret)
                            }

                            IconButton(onClick = { secretVisible = !secretVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.size(32.dp))

                    if (state is PreferencesScreenState.Saving) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = onSave,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(SAVE_BUTTON_TAG),
                            enabled = state.isSaveEnabled
                        ) {
                            Text(stringResource(R.string.preferences_save_button_label))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun PreferencesScreenLoadingPreview() {
    MyGameVaultTheme {
        PreferencesScreen(
            state = PreferencesScreenState.Loading,
            onClientIdChange = {},
            onClientSecretChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Original")
@Composable
fun PreferencesScreenOriginalPreview() {
    MyGameVaultTheme {
        PreferencesScreen(
            state = PreferencesScreenState.Original("my-client-id", "my-client-secret"),
            onClientIdChange = {},
            onClientSecretChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Modified")
@Composable
fun PreferencesScreenModifiedPreview() {
    MyGameVaultTheme {
        PreferencesScreen(
            state = PreferencesScreenState.Modified("new-client-id", "my-client-secret"),
            onClientIdChange = {},
            onClientSecretChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, name = "Saving")
@Composable
fun PreferencesScreenSavingPreview() {
    MyGameVaultTheme {
        PreferencesScreen(
            state = PreferencesScreenState.Saving("my-client-id", "my-client-secret"),
            onClientIdChange = {},
            onClientSecretChange = {},
            onSave = {}
        )
    }
}
