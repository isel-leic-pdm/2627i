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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        // Use a wrapper flow that we can suspend if needed
        var shouldSuspend = false
        override fun getLatest(limit: Int): Flow<List<CollectionEntry>> = flow {
            if (shouldSuspend) delay(1000.milliseconds)
            flow.collect { emit(it) }
        }
        
        override suspend fun save(entry: CollectionEntry) { /* unused */ }
        override suspend fun delete(gameId: Long, platform: Platform) { /* unused */ }
        override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? = null
        override fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> = flow
        override fun searchByName(partialName: String, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = flow
        override fun searchByPlatforms(platforms: Set<Platform>, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = flow
        override fun searchByStates(states: Set<PlayStatus.State>, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = flow
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
    fun `emits Idle with error when repository fails`() = runTest {
        // Arrange
        val repository = object : CollectionRepository {
            override fun getLatest(limit: Int): Flow<List<CollectionEntry>> = flow {
                throw IllegalStateException("Test error")
            }
            override suspend fun save(entry: CollectionEntry) {}
            override suspend fun delete(gameId: Long, platform: Platform) {}
            override suspend fun get(gameId: Long, platform: Platform): CollectionEntry? = null
            override fun getCurrentlyPlaying(): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByName(partialName: String, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByPlatforms(platforms: Set<Platform>, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = getLatest()
            override fun searchByStates(states: Set<PlayStatus.State>, orderBy: CollectionRepository.OrderBy, limit: Int): Flow<List<CollectionEntry>> = getLatest()
        }
        val sut = MyCollectionViewModel(repository)
        
        // Act & Assert
        val state = sut.state.first { it is MyCollectionScreenState.Idle && it.error != null }
        assertTrue(state is MyCollectionScreenState.Idle)
        assertEquals("Test error", state.error?.message)
    }
}
