package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.seconds

class AsyncImageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loading_state_is_displayed() {
        val loadingLoader: ImageLoader = {
            delay(10.seconds)
            Result.failure(IllegalStateException())
        }

        composeTestRule.setContent {
            DemoGameVaultTheme {
                AsyncImage(
                    uri = "http://example.com/image.jpg".toUri(),
                    contentDescription = "Loading image",
                    modifier = Modifier.size(100.dp),
                    loader = loadingLoader,
                )
            }
        }

        composeTestRule
            .onNodeWithTag(AsyncImageTestTags.LOADING)
            .assertIsDisplayed()
    }

    @Test
    fun success_state_is_displayed() {
        val successLoader: ImageLoader = {
            val bitmap = createBitmap(100, 100).apply {
                eraseColor(android.graphics.Color.BLUE)
            }
            Result.success(bitmap.asImageBitmap())
        }

        composeTestRule.setContent {
            DemoGameVaultTheme {
                AsyncImage(
                    uri = "http://example.com/image.jpg".toUri(),
                    contentDescription = "Success image",
                    modifier = Modifier.size(100.dp),
                    loader = successLoader,
                )
            }
        }

        composeTestRule
            .onNodeWithTag(AsyncImageTestTags.SUCCESS)
            .assertIsDisplayed()
    }

    @Test
    fun error_state_is_displayed_on_failure() {
        val errorLoader: ImageLoader = {
            Result.failure(RuntimeException("Simulated error"))
        }

        composeTestRule.setContent {
            DemoGameVaultTheme {
                AsyncImage(
                    uri = "http://example.com/image.jpg".toUri(),
                    contentDescription = "Error image",
                    modifier = Modifier.size(100.dp),
                    loader = errorLoader,
                )
            }
        }

        composeTestRule
            .onNodeWithTag(AsyncImageTestTags.ERROR)
            .assertIsDisplayed()
    }

    @Test
    fun error_state_is_displayed_on_null_uri() {
        val loader: ImageLoader = {
            Result.success(createBitmap(100, 100).asImageBitmap())
        }

        composeTestRule.setContent {
            DemoGameVaultTheme {
                AsyncImage(
                    uri = null,
                    contentDescription = "Null URI image",
                    modifier = Modifier.size(100.dp),
                    loader = loader,
                )
            }
        }

        composeTestRule
            .onNodeWithTag(AsyncImageTestTags.ERROR)
            .assertIsDisplayed()
    }

    @Test(expected = IllegalStateException::class)
    fun throws_when_no_image_loader_is_provided_on_composition_local() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                AsyncImage(
                    uri = "http://example.com/image.jpg".toUri(),
                    contentDescription = "Error image",
                    modifier = Modifier.size(100.dp),
                )
            }
        }
    }
}
