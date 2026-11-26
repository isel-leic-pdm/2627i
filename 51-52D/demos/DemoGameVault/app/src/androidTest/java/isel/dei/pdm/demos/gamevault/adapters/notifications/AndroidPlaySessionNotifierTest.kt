package isel.dei.pdm.demos.gamevault.adapters.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.platform.app.InstrumentationRegistry
import isel.dei.pdm.demos.gamevault.R
import isel.dei.pdm.demos.gamevault.adapters.notifications.AndroidPlaySessionNotifier.Companion.CHANNEL_ID
import isel.dei.pdm.demos.gamevault.adapters.notifications.AndroidPlaySessionNotifier.Companion.NOTIFICATION_ID
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Tests for [AndroidPlaySessionNotifier], which check the notifications actually posted. The
 * permission to post them is granted beforehand, as the notifier does not ask for it.
 */
class AndroidPlaySessionNotifierTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext
    private val manager = context.getSystemService(NotificationManager::class.java)

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val eldenRing = Game(1L, "Elden Ring")
    private val hades = Game(2L, "Hades")
    private val sessionStart = Instant.parse("2026-11-25T21:00:00Z")

    private fun entryInSession(game: Game = eldenRing) =
        CollectionEntry(game = game, platforms = setOf(ps5)).startSession(sessionStart)

    /**
     * The current time, according to the notifier. By default, the session is not long.
     */
    private var now = sessionStart + 10.minutes

    private lateinit var notifier: AndroidPlaySessionNotifier

    @Before
    fun setUp() {
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.POST_NOTIFICATIONS
        )
        notifier = AndroidPlaySessionNotifier(
            context = context,
            clock = object : Clock {
                override fun now(): Instant = this@AndroidPlaySessionNotifierTest.now
            }
        )
    }

    @After
    fun tearDown() {
        notifier.hideSession()
    }

    /**
     * The posted session's notification, or null if none is posted.
     */
    private fun posted(): StatusBarNotification? =
        manager.activeNotifications.firstOrNull { it.id == NOTIFICATION_ID }

    /**
     * The posted session's notification, once it meets the given [condition]. Notifications are
     * posted and cancelled asynchronously, so the posted ones may not reflect the last calls yet.
     * @throws AssertionError If the condition is not met within [timeoutMillis].
     */
    private fun awaitPosted(
        timeoutMillis: Long = 2_000,
        condition: (StatusBarNotification?) -> Boolean
    ): StatusBarNotification? {
        val deadline = SystemClock.uptimeMillis() + timeoutMillis
        while (true) {
            val posted = posted()
            if (condition(posted)) return posted
            if (SystemClock.uptimeMillis() > deadline) throw AssertionError("Posted: $posted")
            SystemClock.sleep(20)
        }
    }

    private val StatusBarNotification?.title: String?
        get() = this?.notification?.extras?.getString(Notification.EXTRA_TITLE)

    private val StatusBarNotification?.text: String?
        get() = this?.notification?.extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()

    @Test
    fun the_channel_is_created_with_low_importance() {
        val channel = manager.getNotificationChannel(CHANNEL_ID)

        assertNotNull(channel)
        assertEquals(NotificationManager.IMPORTANCE_LOW, channel.importance)
    }

    @Test
    fun the_session_is_shown_with_the_game_and_a_timer_since_it_started() {
        notifier.showSession(entryInSession())

        val notification = checkNotNull(awaitPosted { it != null }).notification
        assertEquals("Elden Ring", notification.extras.getString(Notification.EXTRA_TITLE))
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER))
        assertEquals(sessionStart.toEpochMilliseconds(), notification.`when`)
        assertTrue((notification.flags and Notification.FLAG_ONGOING_EVENT) != 0)
        assertNotNull(notification.contentIntent)
    }

    @Test
    fun the_session_is_shown_as_in_progress_while_not_long() {
        notifier.showSession(entryInSession())

        val posted = awaitPosted { it != null }
        assertEquals(context.getString(R.string.play_session_notification_text), posted.text)
    }

    @Test
    fun a_long_session_is_shown_asking_whether_the_user_is_still_playing() {
        now = sessionStart + CollectionEntry.LONG_SESSION

        notifier.showSession(entryInSession())

        val posted = awaitPosted { it != null }
        val expected = context.getString(
            R.string.play_session_notification_long_text,
            CollectionEntry.LONG_SESSION.inWholeHours
        )
        assertEquals(expected, posted.text)
    }

    @Test
    fun the_session_is_requested_to_be_promoted_to_a_live_update() {
        // Live Updates exist since Android 16
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)

        notifier.showSession(entryInSession())

        val notification = checkNotNull(awaitPosted { it != null }).notification
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_REQUEST_PROMOTED_ONGOING))
    }

    @Test
    fun the_session_reports_its_dismissal() {
        // So that its reminder is cancelled (see PlaySessionDismissedReceiver)
        notifier.showSession(entryInSession())

        val notification = checkNotNull(awaitPosted { it != null }).notification
        assertNotNull(notification.deleteIntent)
    }

    @Test
    fun showing_another_session_replaces_the_shown_one() {
        notifier.showSession(entryInSession())

        notifier.showSession(entryInSession(hades))

        awaitPosted { it.title == "Hades" }
        assertEquals(1, manager.activeNotifications.count { it.id == NOTIFICATION_ID })
    }

    @Test
    fun hiding_the_session_removes_the_notification() {
        notifier.showSession(entryInSession())
        awaitPosted { it != null }

        notifier.hideSession()

        assertNull(awaitPosted { it == null })
    }

    @Test
    fun showing_an_entry_without_session_hides_it() {
        notifier.showSession(entryInSession())
        awaitPosted { it != null }

        notifier.showSession(CollectionEntry(game = eldenRing, platforms = setOf(ps5)))

        assertNull(awaitPosted { it == null })
    }
}
