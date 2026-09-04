package isel.dei.pdm.mygamevault.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours

class CollectionEntryTests {

    private val sampleGame = Game(
        id = 1,
        name = "Test Game",
        releaseDate = LocalDate.now(),
        coverUri = "https://example.com/cover.jpg",
        thumbnailUri = null
    )

    @Test
    fun `collection entry holds its properties`() {
        val status = PlayStatus(state = PlayStatus.State.FINISHED, completedRuns = 1)
        val addedDate = LocalDate.of(2024, 1, 1)
        val entry = CollectionEntry(
            game = sampleGame,
            platform = Platforms.PS5,
            playStatus = status,
            addedAt = addedDate
        )

        assertEquals(sampleGame, entry.game)
        assertEquals(Platforms.PS5, entry.platform)
        assertEquals(status, entry.playStatus)
        assertEquals(addedDate, entry.addedAt)
    }

    @Test
    fun `collection entry has current date by default`() {
        val entry = CollectionEntry(sampleGame, Platforms.PS5)
        assertEquals(LocalDate.now(), entry.addedAt)
    }

    @Test
    fun `updateStatus to FINISHED automatically increments runs to 1 if it was 0`() {
        val entry = CollectionEntry(sampleGame, Platforms.PS5)
        assertEquals(0, entry.playStatus.completedRuns)

        val updated = entry.updateStatus(PlayStatus.State.FINISHED)
        assertEquals(PlayStatus.State.FINISHED, updated.playStatus.state)
        assertEquals(1, updated.playStatus.completedRuns)
    }

    @Test
    fun `updateStatus to PLATINUM automatically increments runs to 1 if it was 0`() {
        val entry = CollectionEntry(sampleGame, Platforms.PS5)

        val updated = entry.updateStatus(PlayStatus.State.PLATINUM)
        assertEquals(PlayStatus.State.PLATINUM, updated.playStatus.state)
        assertEquals(1, updated.playStatus.completedRuns)
    }

    @Test
    fun `updateStatus to FINISHED does not change runs if already greater than 0`() {
        val status = PlayStatus(state = PlayStatus.State.PLAYING, completedRuns = 2)
        val entry = CollectionEntry(sampleGame, Platforms.PS5, playStatus = status)

        val updated = entry.updateStatus(PlayStatus.State.FINISHED)
        assertEquals(2, updated.playStatus.completedRuns)
    }

    @Test
    fun `addRun increments completed runs and transitions to FINISHED if needed`() {
        // Case 1: Increments from PLAYING (runs: 0 -> 1, state -> FINISHED)
        val entry = CollectionEntry(sampleGame, Platforms.PS5)
            .updateStatus(PlayStatus.State.PLAYING)
        val updated = entry.addRun()
        assertEquals(1, updated.playStatus.completedRuns)
        assertEquals(PlayStatus.State.FINISHED, updated.playStatus.state)

        // Case 2: Increments from FINISHED (runs: 1 -> 2, state stays FINISHED)
        val updated2 = updated.addRun()
        assertEquals(2, updated2.playStatus.completedRuns)
        assertEquals(PlayStatus.State.FINISHED, updated2.playStatus.state)
    }

    @Test
    fun `addPlayTime updates time spent`() {
        val entry = CollectionEntry(sampleGame, Platforms.PS5)
            .updateStatus(PlayStatus.State.PLAYING)
        val updated = entry.addPlayTime(1.hours)
        assertEquals(1, updated.playStatus.timeSpent.hours)
    }
}
