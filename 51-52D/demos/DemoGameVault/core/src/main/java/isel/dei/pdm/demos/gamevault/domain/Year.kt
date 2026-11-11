package isel.dei.pdm.demos.gamevault.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * A year of the calendar (e.g. 2026), such as the year a platform was acquired or the year the
 * yearly accomplishments refer to. The domain has its own type, instead of using a platform's
 * (e.g. java.time.Year, which is JVM only), so that it does not depend on any platform, and so
 * that years are not mistaken for other numbers (e.g. counts or hours).
 *
 * @property value The year's number. Only positive years are supported.
 * @throws IllegalArgumentException If the value is not positive.
 */
@JvmInline
value class Year(val value: Int) : Comparable<Year> {
    init {
        require(value > 0) { "Only positive years are supported" }
    }

    /**
     * Whether the given [date] is in this year (e.g. `achievedOn in year`).
     */
    operator fun contains(date: LocalDate): Boolean = date.year == value

    override fun compareTo(other: Year): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()

    companion object {
        /**
         * The year of the given [date].
         */
        fun of(date: LocalDate): Year = Year(date.year)

        /**
         * The current year, according to the given [clock], in the given [timeZone].
         * @param clock The clock used to obtain the current instant (e.g. a fixed one, in tests).
         * @param timeZone The time zone where the current date is determined, as the year may
         * differ between time zones around New Year.
         */
        fun current(
            clock: Clock = Clock.System,
            timeZone: TimeZone = TimeZone.currentSystemDefault()
        ): Year = of(clock.todayIn(timeZone))
    }
}
