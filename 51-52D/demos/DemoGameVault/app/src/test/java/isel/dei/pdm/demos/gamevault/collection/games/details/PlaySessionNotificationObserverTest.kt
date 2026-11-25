package isel.dei.pdm.demos.gamevault.collection.games.details

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.sessions.PlaySessionNotifier
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class PlaySessionNotificationObserverTest {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val entry = CollectionEntry(game = Game(1L, "Elden Ring"), platforms = setOf(ps5))
    private val entryInSession = entry.startSession(Instant.parse("2026-11-25T21:00:00Z"))

    private val notifier = RecordingNotifier()

    private var activeSession: CollectionEntry? = null
    private var isChangingConfigurations = false

    /**
     * The lifecycle of the screen, which starts visible (i.e. started), with the observer.
     */
    private val screen = TestLifecycleOwner(
        initialState = Lifecycle.State.INITIALIZED,
        coroutineDispatcher = UnconfinedTestDispatcher()
    ).apply {
        lifecycle.addObserver(
            PlaySessionNotificationObserver(
                notifier = notifier,
                activeSession = { activeSession },
                isChangingConfigurations = { isChangingConfigurations }
            )
        )
        currentState = Lifecycle.State.RESUMED
    }

    @Test
    fun the_session_is_shown_once_the_screen_is_not_visible() {
        activeSession = entryInSession

        screen.currentState = Lifecycle.State.CREATED

        assertEquals(entryInSession, notifier.shown)
    }

    @Test
    fun the_session_is_hidden_once_the_screen_is_visible_again() {
        activeSession = entryInSession
        screen.currentState = Lifecycle.State.CREATED

        screen.currentState = Lifecycle.State.STARTED

        assertNull(notifier.shown)
    }

    @Test
    fun without_an_active_session_nothing_is_shown() {
        activeSession = entry

        screen.currentState = Lifecycle.State.CREATED

        assertNull(notifier.shown)
        assertEquals(0, notifier.showCount)
    }

    @Test
    fun without_the_game_displayed_nothing_is_shown() {
        activeSession = null

        screen.currentState = Lifecycle.State.CREATED

        assertEquals(0, notifier.showCount)
    }

    @Test
    fun configuration_changes_do_not_show_the_session() {
        activeSession = entryInSession
        isChangingConfigurations = true

        screen.currentState = Lifecycle.State.CREATED

        assertEquals(0, notifier.showCount)
    }

    @Test
    fun pausing_the_screen_does_not_show_the_session() {
        // e.g. a dialog of another app over the screen, which remains visible
        activeSession = entryInSession

        screen.currentState = Lifecycle.State.STARTED

        assertEquals(0, notifier.showCount)
    }
}

/**
 * A notifier that only records what it shows.
 */
private class RecordingNotifier : PlaySessionNotifier {

    var shown: CollectionEntry? = null
        private set

    var showCount = 0
        private set

    override fun showSession(entry: CollectionEntry) {
        shown = entry
        showCount++
    }

    override fun hideSession() {
        shown = null
    }
}
