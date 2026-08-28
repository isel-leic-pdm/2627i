package isel.dei.pdm.mygamevault.adapters.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        GameEntity::class,
        PlatformEntity::class,
        CollectionEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
internal abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}
