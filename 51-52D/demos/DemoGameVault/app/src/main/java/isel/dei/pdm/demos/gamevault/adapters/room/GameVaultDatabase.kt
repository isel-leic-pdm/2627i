package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The database where the user's collection is stored. Its schema is exported to app/schemas, so
 * that its evolution is versioned and migrations can be tested.
 *
 * The date and time converters are registered for the whole database, so that DAO queries may
 * also take dates and instants as parameters (e.g. the bounds of a year).
 */
@Database(
    entities = [
        PlatformEntity::class,
        GameEntity::class,
        GamePlatformEntity::class,
        CompletedRunEntity::class,
        GameDetailsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao
    abstract fun gameCollectionDao(): GameCollectionDao

    companion object {
        /**
         * The name of the database file.
         */
        const val NAME = "game-vault.db"
    }
}
