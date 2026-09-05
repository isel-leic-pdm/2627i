package isel.dei.pdm.mygamevault.adapters.db

import androidx.room.TypeConverter
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlin.time.Instant
import java.time.LocalDate

/**
 * Type converters for the Room database.
 */
internal class Converters {
    @TypeConverter
    fun fromPlayStatusState(value: PlayStatus.State): String = value.name

    @TypeConverter
    fun toPlayStatusState(value: String): PlayStatus.State = PlayStatus.State.valueOf(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? = value?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.epochSeconds

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let { Instant.fromEpochSeconds(it) }
}
