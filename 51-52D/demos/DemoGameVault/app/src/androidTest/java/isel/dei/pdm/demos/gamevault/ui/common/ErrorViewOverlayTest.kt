package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags.TIP_TAG
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags.VIEW_TAG
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ErrorViewOverlayTest {

    private companion object {
        const val MESSAGE = "Something went wrong."
        const val BUTTON_TEXT = "Retry"
        const val TIP = "Try again later."
        val CONTAINER_WIDTH = 360.dp
        val CONTAINER_HEIGHT = 600.dp
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun error_view_overlay_displays_message_and_button() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ErrorViewOverlay(
                    message = MESSAGE,
                    buttonText = BUTTON_TEXT,
                    onButtonClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(VIEW_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText(MESSAGE).assertIsDisplayed()
        composeTestRule.onNodeWithText(BUTTON_TEXT).assertIsDisplayed()
    }

    @Test
    fun error_view_overlay_displays_tip_when_provided() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ErrorViewOverlay(
                    message = MESSAGE,
                    buttonText = BUTTON_TEXT,
                    onButtonClicked = {},
                    tip = TIP
                )
            }
        }

        composeTestRule.onNodeWithTag(TIP_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText(TIP).assertIsDisplayed()
    }

    @Test
    fun error_view_overlay_does_not_display_tip_by_default() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ErrorViewOverlay(
                    message = MESSAGE,
                    buttonText = BUTTON_TEXT,
                    onButtonClicked = {}
                )
            }
        }

        composeTestRule.onNodeWithTag(TIP_TAG).assertDoesNotExist()
    }

    @Test
    fun clicking_button_triggers_onButtonClicked() {
        var buttonClicked = false
        composeTestRule.setContent {
            DemoGameVaultTheme {
                ErrorViewOverlay(
                    message = MESSAGE,
                    buttonText = BUTTON_TEXT,
                    onButtonClicked = { buttonClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText(BUTTON_TEXT).performClick()

        assertTrue(buttonClicked)
    }

    @Test
    fun error_view_overlay_by_default_occupies_only_the_area_required_by_its_content() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                Box(modifier = Modifier.size(CONTAINER_WIDTH, CONTAINER_HEIGHT)) {
                    ErrorViewOverlay(
                        message = MESSAGE,
                        buttonText = BUTTON_TEXT,
                        onButtonClicked = {}
                    )
                }
            }
        }

        val bounds = composeTestRule.onNodeWithTag(VIEW_TAG).getUnclippedBoundsInRoot()
        assertTrue(bounds.width < CONTAINER_WIDTH)
        assertTrue(bounds.height < CONTAINER_HEIGHT)
    }

    @Test
    fun error_view_overlay_when_fill_max_size_occupies_the_whole_available_area() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                Box(modifier = Modifier.size(CONTAINER_WIDTH, CONTAINER_HEIGHT)) {
                    ErrorViewOverlay(
                        message = MESSAGE,
                        buttonText = BUTTON_TEXT,
                        onButtonClicked = {},
                        fillMaxSize = true
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(VIEW_TAG)
            .assertWidthIsEqualTo(CONTAINER_WIDTH)
            .assertHeightIsEqualTo(CONTAINER_HEIGHT)
    }
}
