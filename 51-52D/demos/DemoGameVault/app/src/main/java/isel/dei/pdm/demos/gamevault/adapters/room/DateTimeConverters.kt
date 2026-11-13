package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/**
 * Room type converters for the date and time types, so that entities and DAO queries use them
 * directly, instead of their stored representation.
 *
 * SQLite has no date or time storage class: its date and time functions (e.g. `date`, `strftime`)
 * work on values stored as ISO-8601 TEXT, Julian day REAL or Unix time INTEGER. Accordingly:
 * - Dates ([LocalDate]) are stored as ISO-8601 TEXT (e.g. 2026-11-13). It is the format SQLite's
 * functions take by default (e.g. `strftime('%Y', completed_on)`), and it sorts and compares
 * chronologically.
 * - Instants are stored as Unix time INTEGER, in milliseconds. SQLite's functions take them as
 * `datetime(column / 1000, 'unixepoch')`.
 *
 * Nulls are handled by Room, so the converters only deal with non-null values.
 * Parsing malformed dates throws an IllegalArgumentException, so it is handled as data that does
 * not meet the domain requirements.
 */
class DateTimeConverters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(iso: String): LocalDate = LocalDate.parse(iso)

    @TypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(epochMillis: Long): Instant = Instant.fromEpochMilliseconds(epochMillis)
}
