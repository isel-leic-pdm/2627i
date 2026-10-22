package pt.isel.lei.pdm.nasaapod.domain.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ApodImageBookmarkDao {
    @Query("SELECT * FROM apod_images")
    fun getAll(): Flow<List<ApodImageEntity>>

    @Query("SELECT * FROM apod_images")
    suspend fun getAll2(): List<ApodImageEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM apod_images WHERE date = :date)")
    fun isBookmarked(date: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(image: ApodImageEntity)

    @Update
    suspend fun update(image: ApodImageEntity)

    @Delete
    suspend fun delete(image: ApodImageEntity)
}
