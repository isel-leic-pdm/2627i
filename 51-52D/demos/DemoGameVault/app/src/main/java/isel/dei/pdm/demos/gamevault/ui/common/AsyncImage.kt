package isel.dei.pdm.demos.gamevault.ui.common

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImageTestTags.ASYNC_IMAGE
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImageTestTags.ERROR
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImageTestTags.LOADING
import isel.dei.pdm.demos.gamevault.ui.common.AsyncImageTestTags.SUCCESS
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

/**
 * A Composable that displays an image loaded asynchronously from a [Uri].
 *
 * @param uri The [Uri] to load.
 * @param contentDescription The accessibility description for the image.
 * @param modifier The modifier to be applied to the layout.
 * @param contentScale The strategy for scaling the image.
 * @param loader The [ImageLoader] to be used to load the image.
 */
@Composable
fun AsyncImage(
    uri: Uri?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    loader: ImageLoader = LocalImageLoader.current,
) {
    val state by produceState<AsyncImageState>(AsyncImageState.Loading, uri) {
        value = AsyncImageState.Loading
        value = if (uri != null) {
            loader(uri).fold(
                onSuccess = { AsyncImageState.Success(it) },
                onFailure = { AsyncImageState.Error(it) }
            )
        } else {
            AsyncImageState.Error(IllegalArgumentException("URI is null"))
        }
    }

    Box(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = 0.3f))
            .testTag(ASYNC_IMAGE),
        contentAlignment = Alignment.Center
    ) {
        when (val observedState = state) {
            is AsyncImageState.Loading -> CircularProgressIndicator(
                modifier = Modifier.size(24.dp).testTag(LOADING),
                strokeWidth = 2.dp
            )

            is AsyncImageState.Error -> Text(
                text = stringResource(R.string.image_not_available),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                modifier = Modifier.padding(4.dp).testTag(ERROR)
            )

            is AsyncImageState.Success -> Image(
                bitmap = observedState.bitmap,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize().testTag(SUCCESS),
                contentScale = contentScale
            )
        }
    }
}

/**
 * Type alias for a function that loads an image from a URI.
 */
typealias ImageLoader = suspend (Uri) -> Result<ImageBitmap>

/**
 * Represents the state of an asynchronous image load.
 */
sealed class AsyncImageState {
    object Loading : AsyncImageState()
    data class Success(val bitmap: ImageBitmap) : AsyncImageState()
    data class Error(val error: Throwable) : AsyncImageState()
}

/**
 * The CompositionLocal used to provide an [ImageLoader] to the UI tree.
 */
val LocalImageLoader = staticCompositionLocalOf<ImageLoader> {
    { throw IllegalStateException("No ImageLoader provided") }
}

// Testing & preview utilities

object AsyncImageTestTags {
    const val ASYNC_IMAGE = "AsyncImage"
    const val LOADING = "AsyncImageLoading"
    const val ERROR = "AsyncImageError"
    const val SUCCESS = "AsyncImageSuccess"
}


@Preview(showBackground = true, name = "Loading State")
@Composable
fun AsyncImageLoadingPreview() {

    val loadingLoader: ImageLoader = {
        delay(10.seconds) // Long duration delay for preview
        Result.failure(IllegalStateException())
    }

    DemoGameVaultTheme {
        AsyncImage(
            uri = "http://example.com/image.jpg".toUri(),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            loader = loadingLoader
        )
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
fun AsyncImageSuccessPreview() {

    val successLoader: ImageLoader = {
        val bitmap = createBitmap(100, 100).apply {
            eraseColor(android.graphics.Color.BLUE)
        }
        Result.success(bitmap.asImageBitmap())
    }

    DemoGameVaultTheme {
        AsyncImage(
            uri = "http://example.com/image.jpg".toUri(),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            loader = successLoader
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun AsyncImageErrorPreview() {

    val errorLoader: ImageLoader = {
        Result.failure(RuntimeException("Simulated error"))
    }

    DemoGameVaultTheme {
        AsyncImage(
            uri = "http://example.com/image.jpg".toUri(),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            loader = errorLoader
        )
    }
}
