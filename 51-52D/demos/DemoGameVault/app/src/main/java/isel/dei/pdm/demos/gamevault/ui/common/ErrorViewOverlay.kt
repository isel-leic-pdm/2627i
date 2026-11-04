package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags.TIP_TAG
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags.VIEW_TAG
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * An overlay view that displays an error message, an optional tip and a button.
 *
 * @param message The error message to display.
 * @param buttonText The text to display on the action button.
 * @param onButtonClicked The callback to invoke when the button is clicked.
 * @param modifier The modifier to apply to the layout.
 * @param tip An optional hint on what the user can do about the error, displayed below the
 * message with less emphasis. If null, no tip is displayed.
 * @param fillMaxSize Whether the view should occupy the whole available area. If false, the view
 * only occupies the area required by its content.
 */
@Composable
fun ErrorViewOverlay(
    message: String,
    buttonText: String,
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier,
    tip: String? = null,
    fillMaxSize: Boolean = false
) {
    Column(
        modifier = modifier
            .then(if (fillMaxSize) Modifier.fillMaxSize() else Modifier)
            .testTag(VIEW_TAG)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (tip != null) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = tip,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(TIP_TAG)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onButtonClicked) {
            Text(text = buttonText)
        }
    }
}

// Testing & preview utilities

object ErrorViewOverlayTestTags {
    const val VIEW_TAG = "ErrorViewOverlay"
    const val TIP_TAG = "ErrorViewOverlayTip"
}

@Suppress("unused")
class ErrorViewOverlayPreviews {
    @Preview(showBackground = true, name = "Error - Retry")
    @Composable
    fun ErrorViewOverlayRetryPreview() {
        DemoGameVaultTheme {
            ErrorViewOverlay(
                message = "Something went wrong.",
                buttonText = "Retry",
                onButtonClicked = {}
            )
        }
    }

    @Preview(showBackground = true, name = "Error - Retry with tip")
    @Composable
    fun ErrorViewOverlayWithTipPreview() {
        DemoGameVaultTheme {
            ErrorViewOverlay(
                message = "Something went wrong.",
                buttonText = "Retry",
                onButtonClicked = {},
                tip = "Make sure the device has enough free storage space and try again."
            )
        }
    }

    @Preview(showBackground = true, name = "Error - Retry (fill max size)")
    @Composable
    fun ErrorViewOverlayFillMaxSizePreview() {
        DemoGameVaultTheme {
            ErrorViewOverlay(
                message = "Something went wrong.",
                buttonText = "Retry",
                onButtonClicked = {},
                fillMaxSize = true
            )
        }
    }
}
