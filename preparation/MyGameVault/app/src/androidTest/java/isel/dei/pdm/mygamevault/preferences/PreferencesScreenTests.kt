package isel.dei.pdm.mygamevault.preferences

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import isel.dei.pdm.mygamevault.ui.theme.MyGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PreferencesScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun callbacks_whenTypingInFields_areInvoked() {
        // Arrange
        var capturedId = ""
        var capturedSecret = ""
        composeTestRule.setContent {
            MyGameVaultTheme {
                var state by remember { mutableStateOf<PreferencesScreenState>(PreferencesScreenState.Original()) }
                PreferencesScreenView(
                    state = state,
                    onClientIdChange = { 
                        capturedId = it
                        state = PreferencesScreenState.Modified(it, state.clientSecret)
                    },
                    onClientSecretChange = { 
                        capturedSecret = it
                        state = PreferencesScreenState.Modified(state.clientId, it)
                    },
                    onSave = {}
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).performTextInput("test-id")
        composeTestRule.onNodeWithTag(CLIENT_SECRET_INPUT_TAG).performTextInput("test-secret")

        // Assert
        assertEquals("test-id", capturedId)
        assertEquals("test-secret", capturedSecret)
    }

    @Test
    fun saveButton_whenOriginalState_isDisabled() {
        composeTestRule.setContent {
            MyGameVaultTheme {
                PreferencesScreenView(
                    state = PreferencesScreenState.Original("id", "secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun saveButton_whenModifiedAndValidState_isEnabled() {
        composeTestRule.setContent {
            MyGameVaultTheme {
                PreferencesScreenView(
                    state = PreferencesScreenState.Modified("new-id", "secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun saveButton_whenModifiedAndInvalidState_isDisabled() {
        composeTestRule.setContent {
            MyGameVaultTheme {
                PreferencesScreenView(
                    state = PreferencesScreenState.Modified("", "secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = {}
                )
            }
        }
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun onSave_whenSaveButtonClicked_isInvoked() {
        // Arrange
        var saveClicked = false
        composeTestRule.setContent {
            MyGameVaultTheme {
                PreferencesScreenView(
                    state = PreferencesScreenState.Modified("new-id", "secret"),
                    onClientIdChange = {},
                    onClientSecretChange = {},
                    onSave = { saveClicked = true }
                )
            }
        }

        // Act
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).performClick()

        // Assert
        assertTrue(saveClicked)
    }
}
