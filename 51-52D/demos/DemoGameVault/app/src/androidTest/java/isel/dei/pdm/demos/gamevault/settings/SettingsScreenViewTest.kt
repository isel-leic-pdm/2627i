package isel.dei.pdm.demos.gamevault.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.domain.UnrecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.BACK_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.CLIENT_ID_INPUT_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.CLIENT_SECRET_INPUT_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.SAVE_BUTTON_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.SETTINGS_SCREEN_TAG
import isel.dei.pdm.demos.gamevault.settings.SettingsScreenViewTestTags.WORKING_INDICATOR_TAG
import isel.dei.pdm.demos.gamevault.ui.common.ErrorViewOverlayTestTags
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsScreenViewTest {

    private companion object {
        const val CLIENT_ID = "client-123"
        const val CLIENT_SECRET = "secret-456"

        /**
         * Longer than the duration of a short snackbar.
         */
        const val SNACKBAR_DISMISS_TIME_MS = 10_000L
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Displays the settings screen view for the given state. The state is read during
     * composition, so tests can change it to drive the screen through state transitions.
     */
    private fun setScreenContent(
        state: MutableState<SettingsScreenState>,
        onClientIdChange: (String) -> Unit = {},
        onClientSecretChange: (String) -> Unit = {},
        onSave: () -> Unit = {},
        onSaveErrorDismissed: () -> Unit = {},
        onSaveConfirmationShown: () -> Unit = {},
        onBack: () -> Unit = {},
        modifier: Modifier = Modifier
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SettingsScreenView(
                    state = state.value,
                    onClientIdChange = onClientIdChange,
                    onClientSecretChange = onClientSecretChange,
                    onSave = onSave,
                    onSaveErrorDismissed = onSaveErrorDismissed,
                    onSaveConfirmationShown = onSaveConfirmationShown,
                    onBack = onBack,
                    modifier = modifier
                )
            }
        }
    }

    /**
     * Displays the settings screen view for the given (fixed) state.
     */
    private fun setScreenContent(
        state: SettingsScreenState,
        onClientIdChange: (String) -> Unit = {},
        onClientSecretChange: (String) -> Unit = {},
        onSave: () -> Unit = {},
        onSaveErrorDismissed: () -> Unit = {},
        onSaveConfirmationShown: () -> Unit = {},
        onBack: () -> Unit = {}
    ) = setScreenContent(
        state = mutableStateOf(state),
        onClientIdChange = onClientIdChange,
        onClientSecretChange = onClientSecretChange,
        onSave = onSave,
        onSaveErrorDismissed = onSaveErrorDismissed,
        onSaveConfirmationShown = onSaveConfirmationShown,
        onBack = onBack
    )

    @Test
    fun settings_screen_is_displayed() {
        setScreenContent(SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET))

        composeTestRule.onNodeWithTag(SETTINGS_SCREEN_TAG).assertIsDisplayed()
    }

    // Loading

    @Test
    fun loading_state_without_indicator_displays_neither_form_nor_indicator() {
        setScreenContent(SettingsScreenState.Loading(isIndicatorVisible = false))

        composeTestRule.onNodeWithTag(WORKING_INDICATOR_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun loading_state_with_indicator_displays_indicator_but_not_form() {
        setScreenContent(SettingsScreenState.Loading(isIndicatorVisible = true))

        composeTestRule.onNodeWithTag(WORKING_INDICATOR_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_loading))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsNotEnabled()
    }

    // Original and Modified

    @Test
    fun original_state_displays_values_and_disables_save_button() {
        setScreenContent(SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET))

        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithTag(CLIENT_SECRET_INPUT_TAG).assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText(CLIENT_ID).assertIsDisplayed()
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun original_state_with_empty_values_does_not_display_validation_errors() {
        setScreenContent(SettingsScreenState.Original("", ""))

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_error_blank))
            .assertDoesNotExist()
    }

    @Test
    fun modified_state_enables_save_button() {
        setScreenContent(SettingsScreenState.Modified("new-client-id", "new-client-secret"))

        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun modified_state_with_blank_field_displays_validation_error_and_disables_save_button() {
        setScreenContent(SettingsScreenState.Modified("", "new-client-secret"))

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_error_blank))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun entering_client_id_triggers_onClientIdChange() {
        var capturedClientId = ""
        setScreenContent(
            state = SettingsScreenState.Original("", ""),
            onClientIdChange = { capturedClientId = it }
        )

        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).performTextInput("updated-id")

        assertEquals("updated-id", capturedClientId)
    }

    @Test
    fun entering_client_secret_triggers_onClientSecretChange() {
        var capturedClientSecret = ""
        setScreenContent(
            state = SettingsScreenState.Original("", ""),
            onClientSecretChange = { capturedClientSecret = it }
        )

        composeTestRule.onNodeWithTag(CLIENT_SECRET_INPUT_TAG).performTextInput("updated-secret")

        assertEquals("updated-secret", capturedClientSecret)
    }

    @Test
    fun clicking_save_button_triggers_onSave() {
        var saveClicked = false
        setScreenContent(
            state = SettingsScreenState.Modified(CLIENT_ID, CLIENT_SECRET),
            onSave = { saveClicked = true }
        )

        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).performClick()

        assertTrue(saveClicked)
    }

    @Test
    fun clicking_back_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(
            state = SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET),
            onBack = { backClicked = true }
        )

        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).performClick()

        assertTrue(backClicked)
    }

    // Saving

    @Test
    fun saving_state_displays_working_indicator_and_disables_ui() {
        setScreenContent(SettingsScreenState.Saving(CLIENT_ID, CLIENT_SECRET))

        composeTestRule.onNodeWithTag(WORKING_INDICATOR_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_saving))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(CLIENT_SECRET_INPUT_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(SAVE_BUTTON_TAG).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsNotEnabled()
    }

    @Test
    fun entering_saving_state_clears_focus() {
        val state = mutableStateOf<SettingsScreenState>(
            SettingsScreenState.Modified(CLIENT_ID, CLIENT_SECRET)
        )
        // The focus state of the whole screen (i.e. if any of its components has the focus)
        var screenHasFocus = false
        setScreenContent(
            state = state,
            modifier = Modifier.onFocusChanged { screenHasFocus = it.hasFocus }
        )
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).performClick().assertIsFocused()
        assertTrue(screenHasFocus)

        state.value = SettingsScreenState.Saving(CLIENT_ID, CLIENT_SECRET)
        composeTestRule.waitForIdle()

        assertFalse(screenHasFocus)
    }

    @Test
    fun entering_original_state_while_editing_keeps_focus() {
        val state = mutableStateOf<SettingsScreenState>(
            SettingsScreenState.Modified("client-12", CLIENT_SECRET)
        )
        setScreenContent(state)
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).performClick().assertIsFocused()

        state.value = SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET)

        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertIsFocused()
    }

    // Save confirmation

    @Test
    fun original_state_with_save_confirmation_displays_snackbar() {
        composeTestRule.mainClock.autoAdvance = false
        setScreenContent(
            SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET, showSaveConfirmation = true)
        )

        composeTestRule.mainClock.advanceTimeBy(500)

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_confirmation))
            .assertIsDisplayed()
    }

    @Test
    fun original_state_without_save_confirmation_does_not_display_snackbar() {
        setScreenContent(SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET))

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_confirmation))
            .assertDoesNotExist()
    }

    @Test
    fun dismissed_save_confirmation_triggers_onSaveConfirmationShown() {
        composeTestRule.mainClock.autoAdvance = false
        var confirmationShown = false
        setScreenContent(
            state = SettingsScreenState.Original(CLIENT_ID, CLIENT_SECRET, showSaveConfirmation = true),
            onSaveConfirmationShown = { confirmationShown = true }
        )
        composeTestRule.mainClock.advanceTimeBy(500)
        assertFalse(confirmationShown)

        composeTestRule.mainClock.advanceTimeBy(SNACKBAR_DISMISS_TIME_MS)

        assertTrue(confirmationShown)
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_confirmation))
            .assertDoesNotExist()
    }

    // Errors

    @Test
    fun load_error_state_displays_error_view_instead_of_form() {
        setScreenContent(SettingsScreenState.LoadError(UnrecoverablePersistenceException()))

        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_load_error_message))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_load_error_tip))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun clicking_load_error_button_triggers_onBack() {
        var backClicked = false
        setScreenContent(
            state = SettingsScreenState.LoadError(UnrecoverablePersistenceException()),
            onBack = { backClicked = true }
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_load_error_button_label))
            .performClick()

        assertTrue(backClicked)
    }

    @Test
    fun save_error_state_displays_error_view_instead_of_form() {
        setScreenContent(
            SettingsScreenState.SaveError(UnrecoverablePersistenceException(), CLIENT_ID, CLIENT_SECRET)
        )

        composeTestRule.onNodeWithTag(ErrorViewOverlayTestTags.VIEW_TAG).assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_error_message))
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_error_tip))
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(CLIENT_ID_INPUT_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(BACK_BUTTON_TAG).assertIsEnabled()
    }

    @Test
    fun clicking_save_error_button_triggers_onSaveErrorDismissed() {
        var errorDismissed = false
        var backClicked = false
        setScreenContent(
            state = SettingsScreenState.SaveError(
                UnrecoverablePersistenceException(), CLIENT_ID, CLIENT_SECRET
            ),
            onSaveErrorDismissed = { errorDismissed = true },
            onBack = { backClicked = true }
        )

        composeTestRule
            .onNodeWithText(context.getString(R.string.settings_save_error_button_label))
            .performClick()

        assertTrue(errorDismissed)
        assertFalse(backClicked)
    }
}
