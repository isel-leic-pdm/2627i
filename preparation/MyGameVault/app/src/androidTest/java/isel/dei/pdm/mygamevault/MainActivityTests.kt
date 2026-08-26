package isel.dei.pdm.mygamevault

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.mygamevault.add.ADD_GAME_SCREEN_TAG
import isel.dei.pdm.mygamevault.collection.MY_COLLECTION_SCREEN_TAG
import isel.dei.pdm.mygamevault.ports.Secrets
import isel.dei.pdm.mygamevault.preferences.PREFERENCES_SCREEN_TAG
import isel.dei.pdm.mygamevault.ui.common.SEARCH_BAR_TAG
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MainActivityTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_onLaunch_startsOnCollectionTab() {
        composeTestRule.onNodeWithTag(MY_COLLECTION_SCREEN_TAG).assertIsDisplayed()
    }

    @Test
    fun navigation_whenTabsClicked_switchesBetweenScreens() {
        // Switch to Add Game
        composeTestRule.onNodeWithText("Add Game").performClick()
        composeTestRule.onNodeWithTag(ADD_GAME_SCREEN_TAG).assertIsDisplayed()

        // Switch to Preferences
        composeTestRule.onNodeWithText("Preferences").performClick()
        composeTestRule.onNodeWithTag(PREFERENCES_SCREEN_TAG).assertIsDisplayed()

        // Switch back to Collection
        composeTestRule.onNodeWithText("Collection").performClick()
        composeTestRule.onNodeWithTag(MY_COLLECTION_SCREEN_TAG).assertIsDisplayed()
    }

    @Test
    fun search_whenQueryEnteredOnAddGameTab_displaysResults() {
        // Navigate to Add Game screen
        composeTestRule.onNodeWithText("Add Game").performClick()

        // Act: Type a query
        composeTestRule.onNodeWithTag(SEARCH_BAR_TAG).performTextInput("Elden")

        // Wait for debounce (2s) and search delay (0.5s)
        composeTestRule.waitUntil(timeoutMillis = 7000) {
            composeTestRule.onAllNodesWithText("Elden Ring").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert: Result is displayed
        composeTestRule.onNodeWithText("Elden Ring").assertIsDisplayed()
    }

    @Test
    fun preferences_whenSaved_persistAcrossRecreation() = runBlocking {
        // Arrange
        val app = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as DependenciesContainer
        val repo = app.secretsRepository
        val expectedSecrets = Secrets("persistent-id", "persistent-secret")

        // Act
        val result = repo.saveSecrets(expectedSecrets)
        assertTrue(result.isSuccess)
        
        // Recreate activity
        composeTestRule.activityRule.scenario.recreate()

        // Assert: Secrets are still there
        val savedSecrets = repo.secrets.first()
        assertEquals(expectedSecrets, savedSecrets)
    }
}
