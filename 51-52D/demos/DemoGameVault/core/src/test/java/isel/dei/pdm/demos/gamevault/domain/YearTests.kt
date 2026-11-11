package isel.dei.pdm.demos.gamevault.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class YearTests {

    /**
     * A clock that always tells the given instant.
     */
    private fun fixedClock(instant: String) = object : Clock {
        override fun now(): Instant = Instant.parse(instant)
    }

    @Test
    fun `Year holds positive years`() {
        assertEquals(2026, Year(2026).value)
        assertEquals(1, Year(1).value)
    }

    @Test
    fun `Year rejects years that are not positive`() {
        listOf(0, -1, -2026).forEach { value ->
            assertFailsWith<IllegalArgumentException>("$value") { Year(value) }
        }
    }

    @Test
    fun `Year is displayed as its number`() {
        assertEquals("2026", Year(2026).toString())
    }

    @Test
    fun `Years are ordered by their numbers`() {
        assertTrue(Year(2025) < Year(2026))
        assertEquals(listOf(Year(2017), Year(2023), Year(2026)), listOf(Year(2026), Year(2017), Year(2023)).sorted())
    }

    @Test
    fun `Year contains the dates of that year only`() {
        val year = Year(2026)

        assertTrue(LocalDate(2026, 1, 1) in year)
        assertTrue(LocalDate(2026, 12, 31) in year)
        assertFalse(LocalDate(2025, 12, 31) in year)
        assertFalse(LocalDate(2027, 1, 1) in year)
    }

    @Test
    fun `Year of a date is the date's year`() {
        assertEquals(Year(2022), Year.of(LocalDate(2022, 2, 25)))
    }

    @Test
    fun `current year depends on the time zone around New Year`() {
        // Arrange - Still 2025 in UTC, but already 2026 two hours ahead
        val clock = fixedClock("2025-12-31T23:00:00Z")

        // Act & Assert
        assertEquals(Year(2025), Year.current(clock, TimeZone.UTC))
        assertEquals(Year(2026), Year.current(clock, TimeZone.of("Europe/Athens")))
    }
}
