package isel.dei.pdm.mygamevault.add.details

import isel.dei.pdm.mygamevault.MainDispatcherRule
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.GameDetails
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.ports.NoConnectivityException
import isel.dei.pdm.mygamevault.ports.SearchService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailsViewModelTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private class FakeSearchService(
        val detailsToReturn: GameDetails? = null,
        var errorToReturn: Throwable? = null
    ) : SearchService {
        override suspend fun search(
            partialName: NonBlankString,
            platform: Platform,
            category: Game.Category?
        ): Result<List<Game>> = Result.success(emptyList())

        override suspend fun fetchGameDetails(gameId: Long): Result<GameDetails?> {
            return if (errorToReturn != null) {
                Result.failure(errorToReturn!!)
            } else {
                Result.success(detailsToReturn)
            }
        }
    }

    @Test
    fun `initial state is Loading and then transitions to Loaded when successful`() = runTest {
        val game = Game(1, "Elden Ring", LocalDate.of(2022, 2, 25), null as String?)
        val details = GameDetails(game, "Epic", emptyList(), emptyList(), emptyList())
        val sut = GameDetailsViewModel(1, FakeSearchService(detailsToReturn = details))

        // Assert initial
        assertTrue(sut.state.value is GameDetailsScreenState.Loading)

        runCurrent()

        // Assert final
        assertTrue(sut.state.value is GameDetailsScreenState.Loaded)
        assertEquals(details, (sut.state.value as GameDetailsScreenState.Loaded).details)
    }

    @Test
    fun `transitions to Error state when fetching fails`() = runTest {
        val error = NoConnectivityException("No internet")
        val sut = GameDetailsViewModel(1, FakeSearchService(errorToReturn = error))

        runCurrent()

        // Assert
        assertTrue(sut.state.value is GameDetailsScreenState.Error)
        assertEquals(error, (sut.state.value as GameDetailsScreenState.Error).error)
    }

    @Test
    fun `transitions to Error state when game is not found`() = runTest {
        val sut = GameDetailsViewModel(1, FakeSearchService(detailsToReturn = null))

        runCurrent()

        // Assert
        assertTrue(sut.state.value is GameDetailsScreenState.Error)
        assertEquals("Game not found", (sut.state.value as GameDetailsScreenState.Error).error.message)
    }
}
