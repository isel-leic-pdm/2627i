package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import isel.dei.pdm.mygamevault.R

/**
 * A placeholder for a custom asynchronous image loader.
 * This is intended to be implemented as a demo during lectures.
 *
 * @param model The image data to load (e.g., a URL string).
 * @param contentDescription The accessibility description for the image.
 * @param modifier The modifier to be applied to the layout.
 * @param contentScale The strategy for scaling the image.
 */
@Composable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    // Initial placeholder implementation
    Box(
        modifier = modifier.background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.image_placeholder_label),
            style = MaterialTheme.typography.labelSmall,
            color = Color.DarkGray
        )
    }
}
