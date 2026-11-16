package isel.dei.pdm.demos.gamevault.domain.collection.games

import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus.State
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class CompletionTests {

    private val march = LocalDate(2026, 3, 12)

    private val notStarted = PlayStatus()
    private val playing = PlayStatus(PlayTime(5, 0), State.PLAYING)
    private val finished = PlayStatus(PlayTime(40, 0), State.FINISHED, listOf(Accomplishment(march)))
    private val replaying = finished.updateState(State.PLAYING)
    private val platinum = finished.addPlatinum(march)
    private val replayingPlatinum = platinum.updateState(State.PLAYING)

    /**
     * The completions met by the given play status.
     */
    private fun completionsOf(status: PlayStatus): Set<Completion> =
        Completion.entries.filter { status.meets(it) }.toSet()

    @Test
    fun `games without completed runs are not finished, whatever their state`() {
        listOf(notStarted, playing).forEach { status ->
            assertEquals(setOf(Completion.NOT_FINISHED), completionsOf(status))
        }
    }

    @Test
    fun `games with completed runs are finished, whatever their state`() {
        listOf(finished, replaying).forEach { status ->
            assertEquals(setOf(Completion.FINISHED), completionsOf(status))
        }
    }

    @Test
    fun `games with the platinum are also finished, whatever their state`() {
        listOf(platinum, replayingPlatinum).forEach { status ->
            assertEquals(setOf(Completion.FINISHED, Completion.PLATINUM), completionsOf(status))
        }
    }
}
