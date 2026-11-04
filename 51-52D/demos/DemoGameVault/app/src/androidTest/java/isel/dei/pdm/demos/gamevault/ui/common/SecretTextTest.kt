package isel.dei.pdm.demos.gamevault.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * The default mask character used by PasswordVisualTransformation.
 */
private const val PASSWORD_MASK = '•'

class SecretTextTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "SecretTextInput"
    private val secret = "my-secret-value"

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val showDescription = context.getString(R.string.secret_text_show)
    private val hideDescription = context.getString(R.string.secret_text_hide)

    /*
     * The checks use the displayed text (EditableText) instead of the Password semantics property
     * because, in Compose foundation 1.12.1, the latter is not removed once the text field has been
     * rendered as a password (CoreTextFieldSemanticsModifierNode does not update isPassword).
     */

    /**
     * Matches nodes whose content is hidden, i.e. displayed as mask characters.
     */
    private val isHidden = SemanticsMatcher.expectValue(
        SemanticsProperties.EditableText,
        AnnotatedString(PASSWORD_MASK.toString().repeat(secret.length))
    )

    /**
     * Matches nodes whose content is visible, i.e. displayed as is.
     */
    private val isVisible =
        SemanticsMatcher.expectValue(SemanticsProperties.EditableText, AnnotatedString(secret))

    private fun setSecretTextContent(
        value: String = secret,
        onValueChange: (String) -> Unit = {},
        enabled: Boolean = true
    ) {
        composeTestRule.setContent {
            DemoGameVaultTheme {
                SecretText(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    modifier = Modifier.testTag(testTag)
                )
            }
        }
    }

    @Test
    fun secret_text_is_displayed() {
        setSecretTextContent()

        composeTestRule
            .onNodeWithTag(testTag)
            .assertIsDisplayed()
    }

    @Test
    fun secret_is_initially_hidden() {
        setSecretTextContent()

        composeTestRule.onNodeWithTag(testTag).assert(isHidden)
    }

    @Test
    fun show_toggle_button_is_displayed_initially() {
        setSecretTextContent()

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun clicking_toggle_button_shows_secret() {
        setSecretTextContent()

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(hideDescription)
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag(testTag).assert(isVisible)
    }

    @Test
    fun clicking_toggle_button_twice_hides_secret() {
        setSecretTextContent()

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(hideDescription)
            .performClick()

        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag).assert(isHidden)
    }

    @Test
    fun toggle_button_is_disabled_when_secret_text_is_disabled() {
        setSecretTextContent(enabled = false)

        composeTestRule.onNodeWithTag(testTag).assertIsNotEnabled()
        composeTestRule
            .onNodeWithContentDescription(showDescription)
            .assertIsNotEnabled()
            .performClick()

        composeTestRule.onNodeWithTag(testTag).assert(isHidden)
    }

    @Test
    fun secret_text_triggers_onValueChange() {
        var value = ""
        setSecretTextContent(value = value, onValueChange = { value = it })

        composeTestRule
            .onNodeWithTag(testTag)
            .performTextInput("new-secret")

        assertEquals("new-secret", value)
    }
}
