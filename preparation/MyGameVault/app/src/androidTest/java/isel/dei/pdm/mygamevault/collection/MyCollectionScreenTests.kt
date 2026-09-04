package isel.dei.pdm.mygamevault.collection

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.setTestContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class MyCollectionScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleEntry = CollectionEntry(
        game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), "cache://er", null),
        platform = Platforms.PS5,
        playStatus = PlayStatus(state = PlayStatus.State.PLAYING),
        addedAt = LocalDate.now()
    )

    @Test
    fun myCollectionScreen_whenLoading_displaysLoadingIndicator() {
        // Act
        composeTestRule.setTestContent {
            MyCollectionScreenView(
                state = MyCollectionScreenState.Loading(),
                onEntrySelected = {},
                onFilterChange = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(COLLECTION_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun myCollectionScreen_whenIdleAndNotEmpty_displaysList() {
        // Act
        composeTestRule.setTestContent {
            MyCollectionScreenView(
                state = MyCollectionScreenState.Idle(listOf(sampleEntry)),
                onEntrySelected = {},
                onFilterChange = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithTag(COLLECTION_LIST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Elden Ring").assertIsDisplayed()
    }

    @Test
    fun myCollectionScreen_whenIdleAndEmpty_displaysEmptyMessage() {
        // Act
        composeTestRule.setTestContent {
            MyCollectionScreenView(
                state = MyCollectionScreenState.Idle(emptyList()),
                onEntrySelected = {},
                onFilterChange = {}
            )
        }

        // Assert
        composeTestRule.onNodeWithText("Nothing to see here.", substring = true).assertIsDisplayed()
    }

    @Test
    fun myCollectionScreen_whenFilterClicked_invokesOnFilterChange() {
        // Arrange
        var selectedFilter: CollectionFilter? = null
        composeTestRule.setTestContent {
            MyCollectionScreenView(
                state = MyCollectionScreenState.Idle(emptyList()),
                onEntrySelected = {},
                onFilterChange = { selectedFilter = it }
            )
        }

        // Act
        composeTestRule.onNodeWithText("Playing").performClick()

        // Assert
        assertEquals(CollectionFilter.PLAYING, selectedFilter)
    }

    @Test
    fun myCollectionScreen_whenEntryClicked_invokesOnEntrySelected() {
        // Arrange
        var selectedEntry: CollectionEntry? = null
        composeTestRule.setTestContent {
            MyCollectionScreenView(
                state = MyCollectionScreenState.Idle(listOf(sampleEntry)),
                onEntrySelected = { selectedEntry = it },
                onFilterChange = {}
            )
        }

        // Act
        composeTestRule.onNodeWithText("Elden Ring").performClick()

        // Assert
        assertEquals(sampleEntry, selectedEntry)
    }
}
