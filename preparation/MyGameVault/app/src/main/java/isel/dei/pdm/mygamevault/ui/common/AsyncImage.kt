package isel.dei.pdm.mygamevault.ui.common

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import isel.dei.pdm.mygamevault.R
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

import androidx.compose.ui.platform.testTag

/**
 * Test tags for the [AsyncImage] composable.
 */
const val ASYNC_IMAGE_TAG = "AsyncImage"
const val ASYNC_IMAGE_LOADING_TAG = "AsyncImageLoading"
const val ASYNC_IMAGE_ERROR_TAG = "AsyncImageError"
const val ASYNC_IMAGE_SUCCESS_TAG = "AsyncImageSuccess"

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
            .testTag(ASYNC_IMAGE_TAG),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            is AsyncImageState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .testTag(ASYNC_IMAGE_LOADING_TAG),
                strokeWidth = 2.dp
            )
            is AsyncImageState.Error -> Text(
                text = stringResource(R.string.image_not_available),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp,
                modifier = Modifier
                    .padding(4.dp)
                    .testTag(ASYNC_IMAGE_ERROR_TAG)
            )
            is AsyncImageState.Success -> Image(
                bitmap = s.bitmap,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(ASYNC_IMAGE_SUCCESS_TAG),
                contentScale = contentScale
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
fun AsyncImageLoadingPreview() {
    val loadingLoader: ImageLoader = {
        delay(10.seconds) // Infinite delay for preview
        Result.failure(IllegalStateException())
    }
    MyGameVaultTheme {
        AsyncImage(
            uri = Uri("http://example.com/image.jpg"),
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
    MyGameVaultTheme {
        AsyncImage(
            uri = Uri("http://example.com/image.jpg"),
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
    MyGameVaultTheme {
        AsyncImage(
            uri = Uri("http://example.com/image.jpg"),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            loader = errorLoader
        )
    }
}
