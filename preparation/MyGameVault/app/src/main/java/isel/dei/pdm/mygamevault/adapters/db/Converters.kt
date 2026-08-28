package isel.dei.pdm.mygamevault.adapters.db

import androidx.room.TypeConverter
import isel.dei.pdm.mygamevault.domain.PlayStatus

/**
 * Type converters for the Room database.
 */
internal class Converters {
    @TypeConverter
    fun fromPlayStatusState(value: PlayStatus.State): String = value.name

    @TypeConverter
    fun toPlayStatusState(value: String): PlayStatus.State = PlayStatus.State.valueOf(value)
}
