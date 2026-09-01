package isel.dei.pdm.mygamevault.ui.common

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.core.graphics.createBitmap
import isel.dei.pdm.mygamevault.domain.Uri
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class AsyncImageTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun asyncImage_whenLoading_displaysLoadingIndicator() {
        // Arrange
        val loader: ImageLoader = {
            delay(5000.milliseconds)
            Result.failure(IllegalStateException())
        }
        val uri = Uri("https://example.com/loading")

        // Act
        composeTestRule.setContent {
            AsyncImage(uri = uri, contentDescription = null, loader = loader)
        }

        // Assert
        composeTestRule.onNodeWithTag(ASYNC_IMAGE_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun asyncImage_whenSuccess_displaysImage() {
        // Arrange
        val bitmap = createBitmap(10, 10).asImageBitmap()
        val loader: ImageLoader = {
            Result.success(bitmap)
        }
        val uri = Uri("https://example.com/success")

        // Act
        composeTestRule.setContent {
            AsyncImage(uri = uri, contentDescription = null, loader = loader)
        }

        // Assert
        composeTestRule.onNodeWithTag(ASYNC_IMAGE_SUCCESS_TAG).assertIsDisplayed()
    }

    @Test
    fun asyncImage_whenFailure_displaysErrorMessage() {
        // Arrange
        val loader: ImageLoader = {
            Result.failure(RuntimeException("Fail"))
        }
        val uri = Uri("https://example.com/fail")

        // Act
        composeTestRule.setContent {
            AsyncImage(uri = uri, contentDescription = null, loader = loader)
        }

        // Assert
        composeTestRule.onNodeWithTag(ASYNC_IMAGE_ERROR_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Image not available").assertIsDisplayed()
    }
}
