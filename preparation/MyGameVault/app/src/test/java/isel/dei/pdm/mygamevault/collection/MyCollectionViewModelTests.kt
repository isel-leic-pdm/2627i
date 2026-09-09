package isel.dei.pdm.mygamevault.collection

import isel.dei.pdm.mygamevault.MainDispatcherRule
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.Platforms
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.ports.CollectionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class MyCollectionViewModelTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val testGame = Game(1, "Test Game", null, null as String?)
    private val testEntry = CollectionEntry(testGame, Platforms.PS5)

    private class FakeRepository : CollectionRepository {
        val flow = MutableStateFlow<List<CollectionEntry>>(emptyList())
        var lastSearchStates: Set<PlayStatus.State>? = null
        var lastTop: Int = 0
        var getLatestCalled = false

        // Use a wrapper flow that we can suspend if needed
        var shouldSuspend = false
        override fun getLatest(skip: Int, top: Int): Flow<List<CollectionEntry>> = flow {
            getLatestCalled = true
            lastTop = top
            if (shouldSuspend) delay(1000.milliseconds)
            flow.collect { emit(it) }
        }
        
        override suspend fun save(entry: CollectionEntry) { /* unused */ }
        override suspend fun delete(gameId: Long, platform: Platform) { /* unused */ }
        override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? = null
        override fun getActiveSession(): Flow<CollectionEntry?> = flowOf(null)
        override suspend fun startSession(gameId: Long, platformId: Long) {}
        override suspend fun stopSession() {}
        override fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> = flow
        override fun searchByName(partialName: String, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = flow
        override fun searchByPlatforms(platforms: Set<Platform>, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = flow
        override fun searchByStates(states: Set<PlayStatus.State>, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = flow {
            lastSearchStates = states
            lastTop = top
            flow.collect { emit(it) }
        }
    }

    @Test
    fun `initialization does not crash`() = runTest {
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        assertNotNull(sut.state)
    }

    @Test
    fun `changing filter triggers new search with correct states and resets pagination`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        assertTrue(repository.getLatestCalled)

        // Provide 20 items to enable pagination
        repository.flow.value = List(20) { testEntry }
        runCurrent()
        assertTrue(sut.state.value.hasMore)

        // Simulate a page load
        sut.onLoadNextPage()
        runCurrent()
        assertEquals(40, repository.lastTop)

        // Act
        sut.onFilterChange(CollectionFilter.PLAYING)
        runCurrent()

        // Assert
        assertEquals(setOf(PlayStatus.State.PLAYING), repository.lastSearchStates)
        assertEquals(CollectionFilter.PLAYING, sut.state.value.filter)
        assertEquals(20, repository.lastTop) // Resetted to PAGE_SIZE
    }

    @Test
    fun `changing filter to Finished triggers search with correct states`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()

        // Act
        sut.onFilterChange(CollectionFilter.FINISHED)
        runCurrent()

        // Assert
        assertEquals(
            setOf(PlayStatus.State.FINISHED, PlayStatus.State.PLATINUM),
            repository.lastSearchStates
        )
    }

    @Test
    fun `changing filter to Platinum triggers search with correct states`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()

        // Act
        sut.onFilterChange(CollectionFilter.PLATINUM)
        runCurrent()

        // Assert
        assertEquals(setOf(PlayStatus.State.PLATINUM), repository.lastSearchStates)
    }

    @Test
    fun `starts by fetching data and transitions to Loading`() = runTest {
        val repository = FakeRepository()
        repository.shouldSuspend = true
        val sut = MyCollectionViewModel(repository)
        
        // init launches a coroutine that sets state to Loading
        runCurrent()
        assertTrue(sut.state.value is MyCollectionScreenState.Loading)
    }

    @Test
    fun `emits Idle with entries when repository provides data`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        val entries = listOf(testEntry)
        
        // Act
        repository.flow.value = entries
        runCurrent()
        
        // Assert
        val state = sut.state.first { it is MyCollectionScreenState.Idle }
        assertTrue(state is MyCollectionScreenState.Idle)
        assertEquals(entries, state.entries)
    }

    @Test
    fun `onLoadNextPage increments requested top and updates state`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        
        // Initially 20 items, and repo returns 20 items
        val items = List(20) { testEntry }
        repository.flow.value = items
        runCurrent()
        assertTrue(sut.state.value.hasMore)
        assertEquals(20, repository.lastTop)

        // Act
        sut.onLoadNextPage()
        runCurrent()

        // Assert
        assertEquals(40, repository.lastTop)
        assertTrue(sut.state.value is MyCollectionScreenState.Idle)
    }

    @Test
    fun `isLoadingMore is true when fetching more data`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        
        // Initial load
        repository.flow.value = List(20) { testEntry }
        runCurrent()
        
        // Act: Request more
        repository.shouldSuspend = true
        sut.onLoadNextPage()
        runCurrent()

        // Assert: Loading more
        val state = sut.state.value
        assertTrue(state is MyCollectionScreenState.Loading)
        assertTrue(state.isLoadingMore)
        assertEquals(20, state.entries.size) // Shows old entries while loading
    }

    @Test
    fun `onLoadNextPage does nothing if no more items`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        
        // Repo returns only 5 items (less than PAGE_SIZE)
        repository.flow.value = List(5) { testEntry }
        runCurrent()
        assertTrue(!sut.state.value.hasMore)
        assertEquals(20, repository.lastTop)

        // Act
        sut.onLoadNextPage()
        runCurrent()

        // Assert: Top should still be 20
        assertEquals(20, repository.lastTop)
    }

    @Test
    fun `onFilterResetRequested resets only once for same id`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        
        // Change filter to Playing
        sut.onFilterChange(CollectionFilter.PLAYING)
        runCurrent()
        assertEquals(CollectionFilter.PLAYING, sut.state.value.filter)

        // Act: Reset with ID 1
        sut.onFilterResetRequested("id-1")
        runCurrent()
        assertEquals(CollectionFilter.LATEST, sut.state.value.filter)

        // Act: Change filter again
        sut.onFilterChange(CollectionFilter.FINISHED)
        runCurrent()
        assertEquals(CollectionFilter.FINISHED, sut.state.value.filter)

        // Act: Try reset with SAME ID 1
        sut.onFilterResetRequested("id-1")
        runCurrent()

        // Assert: Filter should NOT have reset
        assertEquals(CollectionFilter.FINISHED, sut.state.value.filter)
    }

    @Test
    fun `onFilterResetRequested resets again for different id`() = runTest {
        // Arrange
        val repository = FakeRepository()
        val sut = MyCollectionViewModel(repository)
        runCurrent()
        
        sut.onFilterResetRequested("id-1")
        runCurrent()
        
        sut.onFilterChange(CollectionFilter.PLAYING)
        runCurrent()

        // Act: Reset with DIFFERENT ID 2
        sut.onFilterResetRequested("id-2")
        runCurrent()

        // Assert: Resetted
        assertEquals(CollectionFilter.LATEST, sut.state.value.filter)
    }

    @Test
    fun `emits Idle with error when repository fails`() = runTest {
        // Arrange
        val repository = object : CollectionRepository {
            override fun getLatest(skip: Int, top: Int): Flow<List<CollectionEntry>> = flow {
                throw IllegalStateException("Test error")
            }
            override suspend fun save(entry: CollectionEntry) {}
            override suspend fun delete(gameId: Long, platform: Platform) {}
            override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? = null
            override fun getActiveSession(): Flow<CollectionEntry?> = flowOf(null)
            override suspend fun startSession(gameId: Long, platformId: Long) {}
            override suspend fun stopSession() {}
            override fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByName(partialName: String, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByPlatforms(platforms: Set<Platform>, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByStates(states: Set<PlayStatus.State>, orderBy: CollectionRepository.OrderBy, skip: Int, top: Int): Flow<List<CollectionEntry>> = getLatest()
        }
        val sut = MyCollectionViewModel(repository)
        
        // Act & Assert
        val state = sut.state.first { it is MyCollectionScreenState.Idle && it.error != null }
        assertTrue(state is MyCollectionScreenState.Idle)
        assertEquals("Test error", state.error?.message)
    }
}
