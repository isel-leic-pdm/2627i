package pt.isel.lei.pdm.nasaapod.domain.database

import androidx.room3.Database
import androidx.room3.RoomDatabase


@Database(entities = [ApodImageEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): ApodImageBookmarkDao
}