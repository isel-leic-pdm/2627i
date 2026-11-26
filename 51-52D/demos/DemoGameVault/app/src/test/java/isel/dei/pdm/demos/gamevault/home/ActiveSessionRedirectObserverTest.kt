package isel.dei.pdm.demos.gamevault.home

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import isel.dei.pdm.demos.gamevault.MainDispatcherRule
import isel.dei.pdm.demos.gamevault.adapters.fakes.FakeGameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.RecoverablePersistenceException
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameCollectionRepository
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ActiveSessionRedirectObserverTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val entry = CollectionEntry(game = Game(1L, "Elden Ring"), platforms = setOf(ps5))
    private val entryInSession = entry.startSession(Instant.parse("2026-11-25T21:00:00Z"))

    private val redirects = mutableListOf<CollectionEntry>()

    /**
     * The lifecycle of the home screen, created but not yet visible, with the observer.
     */
    private fun homeScreen(repository: GameCollectionRepository) = TestLifecycleOwner(
        initialState = Lifecycle.State.CREATED,
        coroutineDispatcher = UnconfinedTestDispatcher()
    ).apply {
        lifecycle.addObserver(ActiveSessionRedirectObserver(repository) { redirects.add(it) })
    }

    @Test
    fun the_user_is_redirected_to_the_active_session_once_the_screen_is_visible() {
        val screen = homeScreen(FakeGameCollectionRepository(listOf(entryInSession)))

        screen.currentState = Lifecycle.State.RESUMED

        assertEquals(listOf(entryInSession), redirects)
    }

    @Test
    fun without_an_active_session_the_user_is_not_redirected() {
        val screen = homeScreen(FakeGameCollectionRepository(listOf(entry)))

        screen.currentState = Lifecycle.State.RESUMED

        assertEquals(emptyList(), redirects)
    }

    @Test
    fun the_user_is_redirected_each_time_the_screen_becomes_visible() {
        // e.g. the user returned to the home screen, which is not possible during a session, but
        // the session is still displayed
        val screen = homeScreen(FakeGameCollectionRepository(listOf(entryInSession)))
        screen.currentState = Lifecycle.State.RESUMED
        screen.currentState = Lifecycle.State.CREATED

        screen.currentState = Lifecycle.State.STARTED

        assertEquals(listOf(entryInSession, entryInSession), redirects)
    }

    @Test
    fun failure_to_read_the_active_session_does_not_redirect_the_user() {
        val screen = homeScreen(FailingRepository())

        screen.currentState = Lifecycle.State.RESUMED

        assertEquals(emptyList(), redirects)
    }
}

/**
 * A repository whose active session cannot be read. Otherwise, it behaves as an empty
 * [FakeGameCollectionRepository].
 */
private class FailingRepository(
    private val delegate: FakeGameCollectionRepository = FakeGameCollectionRepository()
) : GameCollectionRepository by delegate {

    override fun observeActiveSession(): Flow<CollectionEntry?> = flow {
        throw RecoverablePersistenceException("Simulated failure")
    }
}
