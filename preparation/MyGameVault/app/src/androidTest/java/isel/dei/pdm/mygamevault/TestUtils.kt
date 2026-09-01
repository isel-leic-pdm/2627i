package isel.dei.pdm.mygamevault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.core.graphics.createBitmap
import isel.dei.pdm.mygamevault.ui.common.ImageLoader
import isel.dei.pdm.mygamevault.ui.common.LocalImageLoader

/**
 * A fake [ImageLoader] that always returns a 1x1 bitmap.
 */
val FakeImageLoader: ImageLoader = {
    Result.success(createBitmap(1, 1).asImageBitmap())
}

/**
 * Sets the content of the test rule, wrapping it in a [CompositionLocalProvider]
 * with a [FakeImageLoader].
 */
fun ComposeContentTestRule.setTestContent(
    imageLoader: ImageLoader = FakeImageLoader,
    content: @Composable () -> Unit
) {
    setContent {
        CompositionLocalProvider(LocalImageLoader provides imageLoader) {
            content()
        }
    }
}
