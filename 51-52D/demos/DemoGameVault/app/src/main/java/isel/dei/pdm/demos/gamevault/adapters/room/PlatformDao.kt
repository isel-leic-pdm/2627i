package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for the platforms in the user's collection.
 * Listings are sorted by name and then by version, ignoring case.
 */
@Dao
abstract class PlatformDao {

    /**
     * Inserts the given platform, unless an equal one (same abbreviation, name and version,
     * ignoring case) already exists.
     * @return The identifier of the inserted platform, or -1 if it was not inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertOrIgnore(platform: PlatformEntity): Long

    /**
     * Gets the identifier of the platform with the given abbreviation, name and version
     * (ignoring case), if it exists.
     */
    @Query(
        "SELECT id FROM platforms " +
            "WHERE abbreviation = :abbreviation AND name = :name AND version = :version"
    )
    protected abstract suspend fun findId(abbreviation: String, name: String, version: String): Long?

    /**
     * Inserts the given platform, unless an equal one already exists.
     * @return The identifier of the inserted platform, or of the existing one.
     */
    @Transaction
    open suspend fun insert(platform: PlatformEntity): Long =
        insertOrIgnore(platform).takeIf { it != NOT_INSERTED }
            ?: checkNotNull(findId(platform.abbreviation, platform.name, platform.version)) {
                "Platform was neither inserted nor found"
            }

    @Query("SELECT * FROM platforms WHERE id = :id")
    abstract suspend fun getById(id: Long): PlatformEntity?

    @Query("SELECT * FROM platforms ORDER BY name COLLATE NOCASE, version COLLATE NOCASE")
    abstract suspend fun getAll(): List<PlatformEntity>

    @Query("SELECT * FROM platforms ORDER BY name COLLATE NOCASE, version COLLATE NOCASE")
    abstract fun observeAll(): Flow<List<PlatformEntity>>

    private companion object {
        /**
         * The value returned by Room when an insertion is ignored.
         */
        const val NOT_INSERTED = -1L
    }
}
