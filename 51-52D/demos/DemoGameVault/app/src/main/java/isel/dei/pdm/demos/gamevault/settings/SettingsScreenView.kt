package isel.dei.pdm.demos.gamevault.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.SAVE_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.SETTINGS_SCREEN_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.WORKING_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlay
import isel.dei.pdm.demos.gamevault.ui.common.LabelledProgressIndicator
import isel.dei.pdm.demos.gamevault.ui.common.SecretText
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenView(
    state: SettingsScreenState,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onSave: () -> Unit,
    onSaveErrorDismissed: () -> Unit,
    onSaveConfirmationShown: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    BackHandler(enabled = !state.isBackEnabled) {}

    val focusManager = LocalFocusManager.current
    val isSaving = state is SettingsScreenState.Saving
    LaunchedEffect(isSaving) {
        if (isSaving) focusManager.clearFocus()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val saveConfirmationMessage = stringResource(R.string.settings_save_confirmation)
    val currentOnSaveConfirmationShown by rememberUpdatedState(onSaveConfirmationShown)
    val showSaveConfirmation = state is SettingsScreenState.Original && state.showSaveConfirmation
    LaunchedEffect(showSaveConfirmation) {
        if (showSaveConfirmation) {
            snackbarHostState.showSnackbar(saveConfirmationMessage)
            currentOnSaveConfirmationShown()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag(SETTINGS_SCREEN_TAG),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = state.isBackEnabled,
                        modifier = Modifier.testTag(BACK_BUTTON_TAG)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_description)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state) {
                is SettingsScreenState.LoadError -> ErrorViewOverlay(
                    message = stringResource(R.string.settings_load_error_message),
                    tip = stringResource(R.string.settings_load_error_tip),
                    buttonText = stringResource(R.string.settings_load_error_button_label),
                    onButtonClicked = onBack,
                    fillMaxSize = true
                )
                is SettingsScreenState.SaveError -> ErrorViewOverlay(
                    message = stringResource(R.string.settings_save_error_message),
                    tip = stringResource(R.string.settings_save_error_tip),
                    buttonText = stringResource(R.string.settings_save_error_button_label),
                    onButtonClicked = onSaveErrorDismissed,
                    fillMaxSize = true
                )
                // There are no values to display while loading
                is SettingsScreenState.Loading -> Unit
                // Original, Modified and Saving (SaveError is handled above)
                is SettingsScreenState.WithValues -> SettingsForm(
                    state = state,
                    onClientIdChange = onClientIdChange,
                    onClientSecretChange = onClientSecretChange,
                    onSave = onSave
                )
            }

            val isLoadingIndicatorVisible =
                state is SettingsScreenState.Loading && state.isIndicatorVisible
            
            if (isLoadingIndicatorVisible || isSaving) {
                LabelledProgressIndicator(
                    text = if (isLoadingIndicatorVisible) {
                        stringResource(R.string.settings_loading)
                    } else {
                        stringResource(R.string.settings_saving)
                    },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag(WORKING_INDICATOR_TAG)
                )
            }
        }
    }
}

/**
 * The form used to edit the settings.
 */
@Composable
private fun SettingsForm(
    state: SettingsScreenState.WithValues,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.size(8.dp))

        OutlinedTextField(
            value = state.clientId,
            onValueChange = onClientIdChange,
            label = { Text(stringResource(R.string.settings_client_id_label)) },
            modifier = Modifier.fillMaxWidth().testTag(CLIENT_ID_INPUT_TAG),
            enabled = state.isInputEnabled,
            isError = state.isValidationFeedbackVisible && !state.isClientIdValid,
            supportingText = if (state.isValidationFeedbackVisible && !state.isClientIdValid) {
                { Text(stringResource(R.string.settings_error_blank)) }
            } else null
        )

        Spacer(modifier = Modifier.size(16.dp))

        SecretText(
            value = state.clientSecret,
            onValueChange = onClientSecretChange,
            label = { Text(stringResource(R.string.settings_client_secret_label)) },
            modifier = Modifier.fillMaxWidth().testTag(CLIENT_SECRET_INPUT_TAG),
            enabled = state.isInputEnabled,
            isError = state.isValidationFeedbackVisible && !state.isClientSecretValid,
            supportingText = if (state.isValidationFeedbackVisible && !state.isClientSecretValid) {
                { Text(stringResource(R.string.settings_error_blank)) }
            } else null
        )

        Spacer(modifier = Modifier.size(32.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().testTag(SAVE_BUTTON_TAG),
            enabled = state.isSaveEnabled
        ) {
            Text(stringResource(R.string.settings_save_button_label))
        }
    }
}

// Testing & preview utilities

object SettingsScreenViewTestTags {
    const val SETTINGS_SCREEN_TAG = "SettingsScreen"
    const val CLIENT_ID_INPUT_TAG = "ClientIdInput"
    const val CLIENT_SECRET_INPUT_TAG = "ClientSecretInput"
    const val SAVE_BUTTON_TAG = "SaveButton"
    const val WORKING_INDICATOR_TAG = "WorkingIndicator"
    const val BACK_BUTTON_TAG = "BackButton"
}

@Suppress("Unused")
class SettingsScreenViewPreviews {

    @Preview(showBackground = true, name = "Loading")
    @Composable
    fun SettingsScreenLoadingPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.Loading(isIndicatorVisible = true),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Original")
    @Composable
    fun SettingsScreenOriginalPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.Original("my-client-id", "my-client-secret"),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Modified")
    @Composable
    fun SettingsScreenModifiedPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.Modified("new-client-id", "my-client-secret"),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Modified - Invalid")
    @Composable
    fun SettingsScreenModifiedInvalidPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.Modified("", "my-client-secret"),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Saving")
    @Composable
    fun SettingsScreenSavingPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.Saving("my-client-id", "my-client-secret"),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Load Error")
    @Composable
    fun SettingsScreenLoadErrorPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.LoadError(UnrecoverablePersistenceException()),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Save Error")
    @Composable
    fun SettingsScreenSaveErrorPreview() {
        DemoGameVaultTheme {
            SettingsScreenView(
                state = SettingsScreenState.SaveError(
                    error = UnrecoverablePersistenceException(),
                    clientId = "my-client-id",
                    clientSecret = "my-client-secret"
                ),
                onClientIdChange = {},
                onClientSecretChange = {},
                onSave = {},
                onSaveErrorDismissed = {},
                onSaveConfirmationShown = {},
                onBack = {}
            )
        }
    }
}
