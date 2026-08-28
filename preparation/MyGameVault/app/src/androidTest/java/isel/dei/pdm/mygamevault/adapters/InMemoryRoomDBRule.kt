package isel.dei.pdm.mygamevault.adapters

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import isel.dei.pdm.mygamevault.adapters.db.GameDao
import isel.dei.pdm.mygamevault.adapters.db.GameDatabase
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit rule that provides an in-memory Room database for testing.
 * It ensures the database is created before each test and closed afterwards.
 */
internal class InMemoryRoomDBRule : TestWatcher() {

    private var _database: GameDatabase? = null
    val database: GameDatabase
        get() = _database ?: throw IllegalStateException("Database not initialized")

    val dao: GameDao
        get() = database.gameDao()

    override fun starting(description: Description?) {
        _database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            GameDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()
    }

    override fun finished(description: Description?) {
        _database?.close()
        _database = null
    }
}
