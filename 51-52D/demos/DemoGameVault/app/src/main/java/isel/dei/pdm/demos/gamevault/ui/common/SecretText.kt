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
import androidx.compose.ui.text.input.KeyboardType.Companion.Password
import androidx.compose.ui.text.input.KeyboardType.Companion.PasswordVisible
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * A specialized [OutlinedTextField] that encapsulates the logic for hiding or showing
 * sensitive text content (such as passwords or client secrets) with a visibility toggle trailing icon.
 * The text is initially hidden and the field is single line.
 *
 * @param value The initial value of the text field.
 * @param onValueChange Called when the value of the text field changes.
 * @param modifier The modifier to apply to the text field.
 * @param label The label to display for the text field.
 * @param enabled Whether the text field is enabled.
 * @param isError Whether the text field is in an error state.
 * @param supportingText The supporting text to display for the text field.
 * @param keyboardOptions The keyboard options to use. Its keyboard type and autocorrect settings
 * are always overridden, so that the keyboard treats the content as a password.
 * @param keyboardActions The actions to perform when the user interacts with the keyboard.
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
    // Deliberately not preserved on configuration changes
    var secretVisible by remember { mutableStateOf(false) }
    val hideTransformation = remember { PasswordVisualTransformation() }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        supportingText = supportingText,
        visualTransformation = if (secretVisible) VisualTransformation.None else hideTransformation,
        keyboardOptions = keyboardOptions.merge(
            other = KeyboardOptions(
                autoCorrectEnabled = false,
                keyboardType = if (secretVisible) PasswordVisible else Password
            )
        ),
        keyboardActions = keyboardActions,
        trailingIcon = {
            // The icon depicts the action performed when clicked, like its content description
            val image = if (secretVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
            val description = if (secretVisible) {
                stringResource(R.string.secret_text_hide)
            } else {
                stringResource(R.string.secret_text_show)
            }

            IconButton(onClick = { secretVisible = !secretVisible }, enabled = enabled) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}

// Testing and preview utilities

@Suppress("unused")
class SecretTextPreviews {

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
}
