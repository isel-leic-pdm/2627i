package isel.dei.pdm.demos.gamevault.images

import androidx.compose.ui.graphics.ImageBitmap
import isel.dei.pdm.demos.gamevault.domain.Uri

/**
 * Type alias for a function that loads an image from a URI, to be displayed. It uses the domain's
 * [Uri], so that the domain's URIs (e.g. a game's cover) are displayed as they are.
 *
 * Unlike [ImageSource] and [ImageStore], which deal with the images' encoded content, it is the
 * abstraction used to display images, so it produces decoded images. It depends on Compose's
 * [ImageBitmap], which is also available in Compose Multiplatform, so it would not prevent the
 * abstraction from being moved to a shared module.
 */
typealias ImageLoader = suspend (Uri) -> Result<ImageBitmap>
