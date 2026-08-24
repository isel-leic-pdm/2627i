package isel.dei.pdm.mygamevault.add

import isel.dei.pdm.mygamevault.MainDispatcherRule
import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.NonBlankString
import isel.dei.pdm.mygamevault.core.SearchService
import isel.dei.pdm.mygamevault.core.ServiceUnavailableException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class AddGameViewModelTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val debounceTimeout = AddGameViewModel.SEARCH_DEBOUNCE_MS.milliseconds
    private val beyondDebounceTimeout = debounceTimeout + 100.milliseconds

    private class FakeSearchService(
        val resultsToReturn: List<Game> = emptyList(),
        val delayMs: Long = 0L,
        var errorToReturn: Throwable? = null
    ) : SearchService {
        var searchCallCount = 0
        var lastPartialName: NonBlankString? = null
        var lastPlatform: Game.Platform? = null
        var lastCategory: Game.Category? = null

        override suspend fun search(
            partialName: NonBlankString,
            platform: Game.Platform,
            category: Game.Category?
        ): Result<List<Game>> {
            searchCallCount++
            lastPartialName = partialName
            lastPlatform = platform
            lastCategory = category
            if (delayMs > 0) delay(delayMs.milliseconds)
            return if (errorToReturn != null) {
                Result.failure(errorToReturn!!)
            } else {
                Result.success(resultsToReturn)
            }
        }
    }

    @Test
    fun `initial state is Idle with empty results`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = AddGameViewModel(FakeSearchService())
            val state = viewModel.state.value as AddGameScreenState.Idle
            assertTrue(state.results.isEmpty())
            assertEquals(null, state.sourceQuery)
        }

    @Test
    fun `typing updates query and transitions to Typing state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val newQuery = "Elden"
            val sut = AddGameViewModel(FakeSearchService())

            // Act
            sut.onQueryChange(newQuery)

            // Assert
            assertEquals(newQuery, sut.query.value)
            assertTrue(sut.state.value is AddGameScreenState.Typing)
        }

    @Test
    fun `typing followed by 2s delay triggers search and transitions to Searching then Idle`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val expectedResults = listOf(
                Game(
                    id = 1,
                    name = "Elden Ring",
                    releaseDate = LocalDate.of(2022, 2, 25), null as String?, null)
            )
            val fakeService = FakeSearchService(resultsToReturn = expectedResults)
            val viewModel = AddGameViewModel(fakeService)

            // Act
            viewModel.onQueryChange("Elden")

            // Assert: Still Typing before debounce
            assertTrue(viewModel.state.value is AddGameScreenState.Typing)

            // Act: Advance time beyond debounce to trigger search
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert
            assertEquals(1, fakeService.searchCallCount)
            assertEquals("Elden", fakeService.lastPartialName?.value)
            assertTrue(viewModel.state.value is AddGameScreenState.Idle)
            val state = viewModel.state.value as AddGameScreenState.Idle
            assertEquals("Elden", state.sourceQuery)
            assertEquals(expectedResults, state.results)
        }

    @Test
    fun `clearing query transitions to Idle state and clears results after debounce`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange: Start with some results
            val initialResults =
                listOf(Game(1, "Elden Ring", null, coverUri = null as String?, null))
            val fakeService = FakeSearchService(resultsToReturn = initialResults)
            val sut = AddGameViewModel(fakeService)

            sut.onQueryChange("Elden")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertTrue(sut.state.value is AddGameScreenState.Idle)
            assertTrue(sut.state.value.results.isNotEmpty())

            // Act
            sut.onQueryChange("")

            // Assert: Still Typing before debounce
            assertTrue(sut.state.value is AddGameScreenState.Typing)

            // Act: Advance time
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert
            assertTrue(sut.state.value is AddGameScreenState.Idle)
            val state = sut.state.value as AddGameScreenState.Idle
            assertEquals(null, state.sourceQuery)
            assertTrue(
                "Results should be cleared when query is blank",
                state.results.isEmpty()
            )
            // searchCallCount should not have increased for empty query
            assertEquals(1, fakeService.searchCallCount)
        }

    @Test
    fun `whitespace query does not trigger search and clears results`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val initialResults =
                listOf(Game(1, "Elden Ring", null, coverUri = null as String?, null))
            val fakeService = FakeSearchService(resultsToReturn = initialResults)
            val viewModel = AddGameViewModel(fakeService)

            viewModel.onQueryChange("Elden")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Act
            viewModel.onQueryChange("   ")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert
            assertTrue(viewModel.state.value is AddGameScreenState.Idle)
            val state = viewModel.state.value as AddGameScreenState.Idle
            assertEquals(null, state.sourceQuery)
            assertTrue(state.results.isEmpty())
            assertEquals("Should not have searched for whitespace", 1, fakeService.searchCallCount)
        }

    @Test
    fun `rapid typing only triggers one search for the last query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val fakeService = FakeSearchService()
            val viewModel = AddGameViewModel(fakeService)

            // Act
            viewModel.onQueryChange("E")
            advanceTimeBy(debounceTimeout / 4)
            viewModel.onQueryChange("El")
            advanceTimeBy(debounceTimeout / 4)
            viewModel.onQueryChange("Elden")

            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert
            assertEquals(1, fakeService.searchCallCount)
            assertEquals("Elden", fakeService.lastPartialName?.value)
        }

    @Test
    fun `search failure transitions to Error state and keeps old results`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val initialResults = listOf(Game(1, "Old Game", null, coverUri = null as String?, null))
            val error = ServiceUnavailableException("Server down")
            val fakeService = FakeSearchService(resultsToReturn = initialResults)
            val sut = AddGameViewModel(fakeService)

            // Step 1: Get some initial successful results
            sut.onQueryChange("Old")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertEquals(initialResults, sut.state.value.results)

            // Step 2: Configure service to fail and trigger a new search
            fakeService.errorToReturn = error
            sut.onQueryChange("New")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert
            assertTrue(sut.state.value is AddGameScreenState.Error)
            val errorState = sut.state.value as AddGameScreenState.Error
            assertEquals(error, errorState.error)
            assertEquals(initialResults, errorState.results)
        }

    @Test
    fun `searching transitions through Searching state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val searchDelay = 1000.milliseconds
            val fakeService = FakeSearchService(delayMs = searchDelay.inWholeMilliseconds)
            val sut = AddGameViewModel(fakeService)

            // Act
            sut.onQueryChange("Elden")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent() // Debounce finishes, search starts

            // Assert: Search is in progress
            assertTrue(
                "State should be Searching while network call is active",
                sut.state.value is AddGameScreenState.Searching
            )

            // Act: Finish the search
            advanceTimeBy(searchDelay)
            runCurrent()

            // Assert: Final state reached
            assertTrue(sut.state.value is AddGameScreenState.Idle)
            val state = sut.state.value as AddGameScreenState.Idle
            assertEquals("Elden", state.sourceQuery)
        }

    @Test
    fun `typing after an error transitions back to Typing state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeService = FakeSearchService(errorToReturn = ServiceUnavailableException())
            val sut = AddGameViewModel(fakeService)

            // Trigger error
            sut.onQueryChange("Fail")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertTrue(sut.state.value is AddGameScreenState.Error)

            // Act: Type again
            sut.onQueryChange("Recover")

            // Assert
            assertTrue(
                "Typing after an error should transition back to Typing state",
                sut.state.value is AddGameScreenState.Typing
            )
        }

    @Test
    fun `new search cancels and supersedes previous search`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange: Service with a long delay to allow overlapping
            val fakeService = FakeSearchService(delayMs = 5000)
            val sut = AddGameViewModel(fakeService)

            // Act: Start first search
            sut.onQueryChange("First")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertEquals(1, fakeService.searchCallCount)

            // Act: Start second search while first is still "running" (delayed)
            sut.onQueryChange("Second")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert: Service called again
            assertEquals(2, fakeService.searchCallCount)

            // Act: Wait for everything to finish
            advanceUntilIdle()

            // Assert: Final query was indeed the last one
            assertEquals("Second", fakeService.lastPartialName?.value)
            assertTrue(sut.state.value is AddGameScreenState.Idle)
            val state = sut.state.value as AddGameScreenState.Idle
            assertEquals("Second", state.sourceQuery)
        }

    @Test
    fun `changing platform triggers new search with same query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeService = FakeSearchService()
            val sut = AddGameViewModel(fakeService)

            // Act: Search for Elden on PS5
            sut.onQueryChange("Elden")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertEquals(1, fakeService.searchCallCount)
            assertEquals(Game.Platform.PS5, fakeService.lastPlatform)

            // Act: Change to PC
            sut.onPlatformChange(Game.Platform.PC)
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert: Search triggered again with PC
            assertEquals(2, fakeService.searchCallCount)
            assertEquals("Elden", fakeService.lastPartialName?.value)
            assertEquals(Game.Platform.PC, fakeService.lastPlatform)
        }

    @Test
    fun `changing category triggers new search with same query`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Arrange
            val fakeService = FakeSearchService()
            val sut = AddGameViewModel(fakeService)

            // Act: Search for Elden with no category
            sut.onQueryChange("Elden")
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()
            assertEquals(1, fakeService.searchCallCount)
            assertEquals(null, fakeService.lastCategory)

            // Act: Change to Main Game
            sut.onCategoryChange(Game.Category.MAIN_GAME)
            advanceTimeBy(beyondDebounceTimeout)
            runCurrent()

            // Assert: Search triggered again with Main Game
            assertEquals(2, fakeService.searchCallCount)
            assertEquals("Elden", fakeService.lastPartialName?.value)
            assertEquals(Game.Category.MAIN_GAME, fakeService.lastCategory)
        }
}
