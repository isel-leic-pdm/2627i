package isel.dei.pdm.mygamevault.ui.common

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme

/**
 * A full-screen view that displays a fatal error message and a button to return or retry.
 *
 * @param message The error message to display.
 * @param buttonText The text to display on the action button.
 * @param onButtonClicked The callback to invoke when the button is clicked.
 * @param modifier The modifier to apply to the layout.
 */
@Composable
fun FatalErrorView(
    message: String,
    buttonText: String,
    onButtonClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(onClick = onButtonClicked) {
            Text(text = buttonText)
        }
    }
}

@Preview(showBackground = true, name = "Fatal Error - Back")
@Composable
fun FatalErrorViewBackPreview() {
    MyGameVaultTheme {
        FatalErrorView(
            message = "A fatal error occurred while accessing the database. Please try again later.",
            buttonText = "Back to Collection",
            onButtonClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Fatal Error - Retry")
@Composable
fun FatalErrorViewRetryPreview() {
    MyGameVaultTheme {
        FatalErrorView(
            message = "Could not load data. Please check your connection and try again.",
            buttonText = "Retry",
            onButtonClicked = {}
        )
    }
}
