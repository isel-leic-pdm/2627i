package isel.dei.pdm.mygamevault.add

import isel.dei.pdm.mygamevault.core.Game
import isel.dei.pdm.mygamevault.core.SearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddGameViewModelTests {

    private val testDispatcher = StandardTestDispatcher()
    
    private inner class FakeSearchService : SearchService {
        var searchCallCount = 0
        var lastQuery = ""
        var resultsToReturn = emptyList<Game>()
        
        override suspend fun search(query: String): List<Game> {
            searchCallCount++
            lastQuery = query
            return resultsToReturn
        }
    }

    private lateinit var fakeService: FakeSearchService
    private lateinit var viewModel: AddGameViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeService = FakeSearchService()
        viewModel = AddGameViewModel(fakeService)
    }

    @Test
    fun `initial state is Idle with empty results`() = runTest {
        assertTrue(viewModel.state.value is AddGameScreenState.Idle)
        assertTrue(viewModel.state.value.results.isEmpty())
    }

    @Test
    fun `typing updates query and transitions to Typing state`() = runTest {
        // Act
        viewModel.onQueryChange("Elden")

        // Assert
        assertEquals("Elden", viewModel.query.value)
        assertTrue(viewModel.state.value is AddGameScreenState.Typing)
    }

    @Test
    fun `typing followed by 2s delay triggers search and transitions to Searching then Idle`() = runTest(testDispatcher) {
        // Arrange
        val expectedResults = listOf(Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), null as String?, null))
        fakeService.resultsToReturn = expectedResults

        // Act
        viewModel.onQueryChange("Elden")
        
        // Assert: Still Typing before debounce
        assertTrue(viewModel.state.value is AddGameScreenState.Typing)
        
        // Act: Advance time by 2.1s to trigger debounce and search
        advanceTimeBy(2100)
        runCurrent()
        
        // Assert
        assertEquals(1, fakeService.searchCallCount)
        assertEquals("Elden", fakeService.lastQuery)
        assertTrue(viewModel.state.value is AddGameScreenState.Idle)
        assertEquals(expectedResults, viewModel.state.value.results)
    }

    @Test
    fun `clearing query transitions to Idle state and clears results after debounce`() = runTest(testDispatcher) {
        // Arrange: Start with some results
        fakeService.resultsToReturn = listOf(Game(1, "Elden Ring", null, coverUri = null as String?, null))
        viewModel.onQueryChange("Elden")
        advanceTimeBy(2100)
        runCurrent()
        assertTrue(viewModel.state.value is AddGameScreenState.Idle)
        assertTrue(viewModel.state.value.results.isNotEmpty())

        // Act
        viewModel.onQueryChange("")
        
        // Assert: Still Typing before debounce
        assertTrue(viewModel.state.value is AddGameScreenState.Typing)
        
        // Act: Advance time
        advanceTimeBy(2100)
        runCurrent()
        
        // Assert
        assertTrue(viewModel.state.value is AddGameScreenState.Idle)
        assertTrue("Results should be cleared when query is blank", viewModel.state.value.results.isEmpty())
        // searchCallCount should not have increased for empty query
        assertEquals(1, fakeService.searchCallCount)
    }
}
