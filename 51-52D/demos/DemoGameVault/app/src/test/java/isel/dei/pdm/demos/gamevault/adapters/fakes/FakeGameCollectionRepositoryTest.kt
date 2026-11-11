package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.Accomplishment
import isel.dei.pdm.demos.gamevault.domain.collection.games.AddGameResult
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayTime
import isel.dei.pdm.demos.gamevault.domain.collection.games.StartSessionResult
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test
import isel.dei.pdm.demos.gamevault.domain.Year
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class FakeGameCollectionRepositoryTest {

    private val ps5 = Platform(1L, "PS5", "PlayStation 5")
    private val switch = Platform(2L, "Switch", "Nintendo Switch")
    private val now = Instant.parse("2026-11-09T20:00:00Z")
    private val thisYear = Year(2026)
    private val march = LocalDate(2026, 3, 12)
    private val playingStatus = PlayStatus(PlayTime(12, 30), State.PLAYING)

    private fun entry(
        id: Long,
        name: String,
        platform: Platform = ps5,
        status: PlayStatus = PlayStatus(state = State.BACKLOG),
        sessionStart: Instant? = null
    ) = CollectionEntry(Game(id, name), setOf(platform), status, sessionStartTime = sessionStart)

    private fun playing(id: Long, name: String, platform: Platform = ps5) =
        entry(id, name, platform, PlayStatus(PlayTime(1, 0), State.PLAYING))

    private suspend fun FakeGameCollectionRepository.names(query: CollectionQuery = CollectionQuery()) =
        observeItems(query).first().items.map { it.name.value }

    // Construction

    @Test
    fun `initial entries with repeated game identifiers are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            FakeGameCollectionRepository(listOf(entry(1L, "Hades"), entry(1L, "Hades II")))
        }
    }

    @Test
    fun `initial entries with more than one active session are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            FakeGameCollectionRepository(
                listOf(
                    entry(1L, "Hades", status = PlayStatus(state = State.PLAYING), sessionStart = now),
                    entry(2L, "Celeste", status = PlayStatus(state = State.PLAYING), sessionStart = now)
                )
            )
        }
    }

    // Adding games

    @Test
    fun `add stores the game, its entry and its details`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository()
        val details = NewGameDetails(notes = NonBlankString("Lent to João"))

        // Act
        val result = sut.add(NewGame("Elden Ring"), ps5, PlayStatus(state = State.BACKLOG), details)

        // Assert
        val gameId = assertIs<AddGameResult.Added>(result).gameId
        val entry = sut.observeEntry(gameId).first()
        assertEquals("Elden Ring", entry?.game?.name?.value)
        assertEquals(setOf(ps5), entry?.platforms)
        assertEquals(State.BACKLOG, entry?.playStatus?.state)
        assertEquals(details.toGameDetails(entry!!.game), sut.observeDetails(gameId).first())
    }

    @Test
    fun `add assigns distinct identifiers, after those of the initial entries`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(7L, "Hades")))

        // Act
        val first = sut.add(NewGame("Celeste"), ps5) as AddGameResult.Added
        val second = sut.add(NewGame("Hollow Knight"), ps5) as AddGameResult.Added

        // Assert
        assertEquals(8L, first.gameId)
        assertEquals(9L, second.gameId)
    }

    @Test
    fun `add of the same game on another platform adds the platform to its entry`() = runTest {
        // Arrange
        val existing = entry(1L, "The Legend of Zelda: Breath of the Wild", switch, playingStatus)
        val sut = FakeGameCollectionRepository(listOf(existing))

        // Act
        val result = sut.add(
            NewGame("the legend of zelda breath of the wild"),
            ps5,
            playStatus = PlayStatus(state = State.BACKLOG),
            details = NewGameDetails(notes = NonBlankString("Ignored"))
        )

        // Assert - No entry is added, and the play status and details are kept
        assertEquals(1L, assertIs<AddGameResult.PlatformAdded>(result).existing.id)
        assertEquals(1, sut.names().size)
        val entry = sut.observeEntry(1L).first()
        assertEquals(listOf(switch, ps5), entry?.platforms?.toList())
        assertEquals(playingStatus, entry?.playStatus)
        assertNull(sut.observeDetails(1L).first()?.notes)
    }

    @Test
    fun `add of the same game on a platform it is owned on changes nothing`() = runTest {
        // Arrange
        val existing = entry(1L, "The Legend of Zelda: Breath of the Wild", switch)
        val sut = FakeGameCollectionRepository(listOf(existing))

        // Act - Even if given an outdated copy of the platform
        val result = sut.add(NewGame("The Legend of Zelda: Breath of the Wild"), switch.copy(logoUri = null))

        // Assert
        assertEquals(1L, assertIs<AddGameResult.AlreadyOwnedOnPlatform>(result).existing.id)
        assertEquals(existing, sut.observeEntry(1L).first())
    }

    @Test
    fun `findSameEntry finds the entry of the same game, or null`() = runTest {
        val existing = entry(1L, "Hades")
        val sut = FakeGameCollectionRepository(listOf(existing))

        assertEquals(existing, sut.findSameEntry(NewGame("HADES")))
        assertNull(sut.findSameEntry(NewGame("Hades II")))
    }

    @Test
    fun `findSameEntry tells the platforms the game is owned on, regardless of versions`() = runTest {
        // Arrange
        val ps5Slim = Platform(3L, "PS5", "PlayStation 5", version = "Slim")
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades", ps5)))

        // Act
        val entry = sut.findSameEntry(NewGame("Hades"))

        // Assert - Games are oblivious of versions: owned on the PS5 is owned on the PS5 Slim
        assertTrue(entry!!.isOwnedOn(ps5Slim))
        assertFalse(entry.isOwnedOn(switch))
    }

    @Test
    fun `add of the same game on another version of a platform it is owned on changes nothing`() = runTest {
        // Arrange
        val existing = entry(1L, "Hades", ps5)
        val sut = FakeGameCollectionRepository(listOf(existing))

        // Act
        val result = sut.add(NewGame("Hades"), Platform(3L, "PS5", "PlayStation 5", version = "Pro"))

        // Assert
        assertEquals(1L, assertIs<AddGameResult.AlreadyOwnedOnPlatform>(result).existing.id)
        assertEquals(existing, sut.observeEntry(1L).first())
    }

    // Listing

    @Test
    fun `items are sorted by name, ignoring case, with the active session first`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(
            listOf(
                entry(1L, "celeste"),
                entry(2L, "Zelda", status = PlayStatus(state = State.PLAYING), sessionStart = now),
                entry(3L, "Alan Wake 2"),
                entry(4L, "Balatro")
            )
        )

        // Act & Assert
        assertEquals(listOf("Zelda", "Alan Wake 2", "Balatro", "celeste"), sut.names())
    }

    @Test
    fun `items are filtered by every criteria of the query`() = runTest {
        // Arrange
        val withPlatinum = PlayStatus(
            PlayTime(80, 0), State.FINISHED, listOf(Accomplishment(march)), Accomplishment(march)
        )
        val sut = FakeGameCollectionRepository(
            listOf(
                playing(1L, "Hades"),
                playing(2L, "Hades II", switch),
                entry(3L, "Celeste"),
                entry(4L, "Bloodborne", status = withPlatinum),
                entry(5L, "007 Legends")
            )
        )

        // Act & Assert
        assertEquals(listOf("Hades", "Hades II"), sut.names(CollectionQuery(state = State.PLAYING)))
        assertEquals(listOf("Bloodborne"), sut.names(CollectionQuery(platinumOnly = true)))
        assertEquals(listOf("Hades II"), sut.names(CollectionQuery(platformId = switch.id)))
        assertEquals(
            listOf("Hades", "Hades II"),
            sut.names(CollectionQuery(nameContains = NonBlankString("hades")))
        )
        assertEquals(listOf("Celeste"), sut.names(CollectionQuery(initial = 'C')))
        assertEquals(listOf("007 Legends"), sut.names(CollectionQuery(initial = CollectionQuery.OTHER_INITIAL)))
        assertEquals(
            listOf("Hades"),
            sut.names(CollectionQuery(state = State.PLAYING, platformId = ps5.id))
        )
    }

    @Test
    fun `platform criterion matches games owned on the platform among others`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(
            listOf(
                CollectionEntry(Game(1L, "Hades"), setOf(ps5, switch)),
                entry(2L, "Celeste", switch),
                entry(3L, "Bloodborne", ps5)
            )
        )

        // Act & Assert
        assertEquals(listOf("Celeste", "Hades"), sut.names(CollectionQuery(platformId = switch.id)))
        assertEquals(listOf("Bloodborne", "Hades"), sut.names(CollectionQuery(platformId = ps5.id)))
    }

    @Test
    fun `items are limited, signalling whether there are more`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository((1L..5L).map { entry(it, "Game $it") })

        // Act
        val limited = sut.observeItems(limit = 3).first()
        val exact = sut.observeItems(limit = 5).first()

        // Assert
        assertEquals(3, limited.items.size)
        assertTrue(limited.hasMore)
        assertEquals(5, exact.items.size)
        assertFalse(exact.hasMore)
    }

    @Test
    fun `a non positive limit is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            FakeGameCollectionRepository().observeItems(limit = 0)
        }
    }

    @Test
    fun `observed items are updated after each change`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository()
        val items = sut.observeItems()
        assertTrue(items.first().items.isEmpty())

        // Act
        sut.add(NewGame("Hades"), ps5)

        // Assert
        assertEquals(listOf("Hades"), items.first().items.map { it.name.value })
    }

    // Summary

    @Test
    fun `summary follows the domain rules`() = runTest {
        // Arrange
        val lastYear = LocalDate(2025, 6, 1)
        val sut = FakeGameCollectionRepository(
            listOf(
                entry(1L, "Backlogged"),
                playing(2L, "Being played"),
                entry(3L, "Finished this year", status = PlayStatus(PlayTime(10, 0), State.FINISHED, listOf(Accomplishment(march)))),
                entry(
                    4L, "Replayed this year",
                    status = PlayStatus(PlayTime(20, 0), State.FINISHED, listOf(Accomplishment(lastYear), Accomplishment(march)))
                ),
                entry(
                    5L, "Platinum this year",
                    status = PlayStatus(PlayTime(90, 0), State.FINISHED, listOf(Accomplishment(lastYear)), Accomplishment(march))
                )
            )
        )

        // Act
        val summary = sut.observeSummary(thisYear).first()

        // Assert
        assertEquals(thisYear, summary.year)
        assertEquals(1, summary.finishedInYear)
        assertEquals(1, summary.replayedInYear)
        assertEquals(1, summary.platinumInYear)
        assertEquals(5, summary.owned)
        assertEquals(4, summary.played)
        assertEquals(3, summary.finished)
        assertEquals(1, summary.platinum)
    }

    // Updating entries

    @Test
    fun `updateEntry stores the change and returns the updated entry`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(playing(1L, "Hades")))

        // Act
        val updated = sut.updateEntry(1L) { it.addRun(march) }

        // Assert
        assertEquals(State.FINISHED, updated?.playStatus?.state)
        assertEquals(updated, sut.observeEntry(1L).first())
    }

    @Test
    fun `updateEntry of a game not in the collection returns null`() = runTest {
        assertNull(FakeGameCollectionRepository().updateEntry(1L) { it.addRun(march) })
    }

    @Test
    fun `updateEntry rejected by the domain stores nothing`() = runTest {
        // Arrange
        val original = entry(1L, "Hades", status = PlayStatus(state = State.PLAYING), sessionStart = now)
        val sut = FakeGameCollectionRepository(listOf(original))

        // Act - Finishing for the first time while a session is active is rejected
        assertFailsWith<IllegalArgumentException> { sut.updateEntry(1L) { it.addRun(march) } }

        // Assert
        assertEquals(original, sut.observeEntry(1L).first())
    }

    @Test
    fun `updateEntry cannot start sessions`() = runTest {
        val sut = FakeGameCollectionRepository(listOf(playing(1L, "Hades")))

        assertFailsWith<IllegalStateException> { sut.updateEntry(1L) { it.startSession(now) } }
    }

    // Sessions

    @Test
    fun `startSession starts it and makes it observable`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades")))

        // Act
        val result = sut.startSession(1L, now)

        // Assert
        val started = assertIs<StartSessionResult.Started>(result).entry
        assertEquals(now, started.sessionStartTime)
        assertEquals(started, sut.observeActiveSession().first())
    }

    @Test
    fun `startSession while another session is active reports it`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades"), entry(2L, "Celeste")))
        sut.startSession(1L, now)

        // Act
        val result = sut.startSession(2L, now)

        // Assert
        assertEquals(1L, assertIs<StartSessionResult.AnotherSessionActive>(result).active.game.id)
        assertFalse(sut.observeEntry(2L).first()!!.isSessionActive)
    }

    @Test
    fun `startSession of a game not in the collection reports it`() = runTest {
        assertEquals(StartSessionResult.NotInCollection, FakeGameCollectionRepository().startSession(1L, now))
    }

    @Test
    fun `ending the session through updateEntry allows starting another one`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades"), entry(2L, "Celeste")))
        sut.startSession(1L, now)

        // Act
        sut.updateEntry(1L) { it.endSession(now + 1.hours) }
        val result = sut.startSession(2L, now + 2.hours)

        // Assert
        assertIs<StartSessionResult.Started>(result)
        assertEquals(PlayTime(1, 0), sut.observeEntry(1L).first()?.playStatus?.timeSpent)
    }

    // Updating games and details, and removing

    @Test
    fun `updateGame updates the game in its entry and details`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades")))
        val renamed = Game(1L, "Hades (2020)")

        // Act & Assert
        assertTrue(sut.updateGame(renamed))
        assertEquals(renamed, sut.observeEntry(1L).first()?.game)
        assertEquals(renamed, sut.observeDetails(1L).first()?.game)
        assertFalse(sut.updateGame(Game(2L, "Celeste")))
    }

    @Test
    fun `updateDetails updates only the details`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades")))
        val notes = NonBlankString("Try the spear")

        // Act - The game held by the details only identifies it
        val updated = sut.updateDetails(GameDetails(Game(1L, "Other name"), notes = notes))

        // Assert
        assertTrue(updated)
        val details = sut.observeDetails(1L).first()
        assertEquals(notes, details?.notes)
        assertEquals("Hades", details?.game?.name?.value)
        assertFalse(sut.updateDetails(GameDetails(Game(2L, "Celeste"))))
    }

    @Test
    fun `remove removes the game, its entry and its details`() = runTest {
        // Arrange
        val sut = FakeGameCollectionRepository(listOf(entry(1L, "Hades")))

        // Act & Assert
        assertTrue(sut.remove(1L))
        assertNull(sut.observeEntry(1L).first())
        assertNull(sut.observeDetails(1L).first())
        assertFalse(sut.remove(1L))
    }
}
