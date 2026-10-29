package isel.dei.pdm.demos.gamevault.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenViewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settings_screen_is_displayed() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Original("", ""),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SETTINGS_SCREEN_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun loading_state_displays_working_indicator_and_disables_ui() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Loading,
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.WORKING_INDICATOR_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_loading))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.BACK_BUTTON_TAG)
            .assertIsNotEnabled()
    }

    @Test
    fun saving_state_displays_working_indicator_label_and_disables_ui() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Saving("client-123", "secret-456"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.WORKING_INDICATOR_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_saving))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.BACK_BUTTON_TAG)
            .assertIsNotEnabled()
    }

    @Test
    fun original_state_displays_values_and_disables_save_button() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Original("client-123", "secret-456"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .assertIsNotEnabled()
    }

    @Test
    fun modified_state_enables_save_button() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Modified("new-client-id", "new-client-secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .assertIsEnabled()
    }

    @Test
    fun modified_state_with_blank_field_disables_save_button() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Modified("", "new-client-secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .assertIsNotEnabled()
    }

    @Test
    fun entering_client_id_triggers_onClientIdChange() {
        var capturedClientId = ""
        val expectedId = "updated-id"

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Original("", ""),
                    onClientIdChange = { capturedClientId = it },
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG)
            .performTextInput(expectedId)

        assertEquals(expectedId, capturedClientId)
    }

    @Test
    fun entering_client_secret_triggers_onClientSecretChange() {
        var capturedClientSecret = ""
        val expectedSecret = "updated-secret"
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Original("", ""),
                    onClientIdChange = {},
                    onClientSecretChange = { capturedClientSecret = it },
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG)
            .performTextInput(expectedSecret)

        assertEquals(expectedSecret, capturedClientSecret)
    }

    @Test
    fun clicking_save_button_triggers_onSave() {
        var saveClicked = false

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Modified("client-123", "secret-456"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = { saveClicked = true },
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.SAVE_BUTTON_TAG)
            .performClick()

        assertTrue(saveClicked)
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false

        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Original("", ""),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = { backClicked = true },
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.BACK_BUTTON_TAG)
            .performClick()

        assertTrue(backClicked)
    }

    @Test
    fun saving_state_disables_back_button() {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = SettingsScreenState.Saving("client-123", "secret-456"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {},
                    onBack = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(SettingsScreenViewTestTags.BACK_BUTTON_TAG)
            .assertIsNotEnabled()
    }
}
