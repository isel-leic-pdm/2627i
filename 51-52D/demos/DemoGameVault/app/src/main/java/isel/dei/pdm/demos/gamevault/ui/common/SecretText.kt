package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * A specialized [OutlinedTextField] that encapsulates the logic for hiding or showing
 * sensitive text content (such as passwords or client secrets) with a visibility toggle trailing icon.
 */
@Composable
fun SecretText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    var secretVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        visualTransformation = if (secretVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = {
            val image = if (secretVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (secretVisible) {
                stringResource(R.string.secret_text_hide)
            } else {
                stringResource(R.string.secret_text_show)
            }

            IconButton(onClick = { secretVisible = !secretVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}

@Preview
@Composable
fun SecretTextPreview() {
    DemoGameVaultTheme {
        SecretText(
            value = "my-secret-text",
            onValueChange = { },
        )
    }
}
