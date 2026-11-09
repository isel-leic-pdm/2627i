package isel.dei.pdm.demos.gamevault.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The view displayed in a region of the home screen that could not be loaded, from which loading
 * it can be retried. Unlike the full screen error view, it is compact: it only takes the region's
 * place, so that the remaining regions of the screen are still usable.
 *
 * All regions display the same message and tip, as their contents have the same origin (the
 * collection, in local storage), and so do their errors. Local reads seldom fail, and when they
 * do, it is usually because the device is low on storage space or its storage is misbehaving,
 * which waiting does not solve: the tip therefore suggests freeing up space and restarting the
 * device, should retrying not be enough.
 *
 * DRAFT: The design is still being iterated.
 *
 * @param onRetry Callback invoked when the user wants to retry loading the region.
 * @param modifier Modifier to be applied to the view's layout.
 */
@Composable
fun RegionErrorView(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // The message describes the error, so the icon is decorative
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_region_error_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.home_region_error_tip),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.home_retry_button_label))
            }
        }
    }
}

// Preview utilities

@Suppress("Unused")
class RegionErrorViewPreviews {

    @Preview(showBackground = true, name = "Region error")
    @Composable
    fun RegionErrorPreview() {
        DemoGameVaultTheme {
            RegionErrorView(onRetry = {})
        }
    }
}
