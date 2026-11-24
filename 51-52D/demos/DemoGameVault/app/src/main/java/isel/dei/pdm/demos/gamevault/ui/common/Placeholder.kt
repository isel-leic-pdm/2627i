package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * A placeholder for content that is still being loaded (e.g. an image or a chip), displayed as a
 * muted shape that takes the size given by the [modifier]. It is merely decorative: it has no
 * semantics, so it is ignored by accessibility services.
 *
 * @param modifier Modifier to be applied to the placeholder, which determines its size.
 * @param shape The placeholder's shape.
 */
@Composable
fun Placeholder(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

/**
 * A placeholder for a line of text that is still being loaded. It takes the height of a line of
 * text with the given [style], so that the layout does not change when the text replaces it,
 * whatever the user's font scale, while the muted bar is a bit thinner, as glyphs are.
 *
 * @param style The style of the text that will replace the placeholder.
 * @param width The placeholder's width, an approximation of the text's.
 * @param modifier Modifier to be applied to the placeholder.
 */
@Composable
fun TextPlaceholder(
    style: TextStyle,
    width: Dp,
    modifier: Modifier = Modifier
) {
    val lineHeight = with(LocalDensity.current) { style.lineHeight.toDp() }
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .width(width)
            .height(lineHeight)
    ) {
        Placeholder(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(TEXT_PLACEHOLDER_HEIGHT_FRACTION)
        )
    }
}

/**
 * The fraction of a line's height taken by the placeholder of its text.
 */
private const val TEXT_PLACEHOLDER_HEIGHT_FRACTION = 0.7f

// Testing & preview utilities

@Suppress("Unused")
class PlaceholderPreviews {

    @Preview(showBackground = true)
    @Composable
    fun PlaceholdersPreview() {
        DemoGameVaultTheme {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                TextPlaceholder(style = MaterialTheme.typography.headlineLarge, width = 200.dp)
                TextPlaceholder(style = MaterialTheme.typography.bodyLarge, width = 120.dp)
                Placeholder(modifier = Modifier.size(width = 96.dp, height = 32.dp), shape = CircleShape)
            }
        }
    }
}
