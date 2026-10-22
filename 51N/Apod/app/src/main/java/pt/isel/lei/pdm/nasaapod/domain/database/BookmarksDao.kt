package pt.isel.lei.pdm.nasaapod.domain.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow
import pt.isel.lei.pdm.nasaapod.domain.ApodImage

@Dao
interface ApodImageBookmarkDao {
    @Query("SELECT * FROM bookmarks")
    fun getAll(): Flow<List<ApodImageEntity>>

    @Query("SELECT * FROM bookmarks")
    suspend fun getAll2(): List<ApodImageEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE date = :date)")
    fun isBookmarked(date: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(image: ApodImageEntity)

    @Update
    suspend fun update(image: ApodImageEntity)

    @Delete
    suspend fun delete(image: ApodImageEntity)
}