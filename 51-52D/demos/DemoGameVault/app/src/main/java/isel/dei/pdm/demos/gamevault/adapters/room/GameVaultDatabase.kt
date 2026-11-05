package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * The database where the user's collection is stored. Its schema is exported to app/schemas, so
 * that its evolution is versioned and migrations can be tested.
 */
@Database(entities = [PlatformEntity::class], version = 1, exportSchema = true)
abstract class GameVaultDatabase : RoomDatabase() {
    abstract fun platformDao(): PlatformDao

    companion object {
        /**
         * The name of the database file.
         */
        const val NAME = "game-vault.db"
    }
}
