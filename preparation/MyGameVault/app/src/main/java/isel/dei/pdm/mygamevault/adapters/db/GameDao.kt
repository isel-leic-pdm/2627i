package isel.dei.pdm.mygamevault.adapters.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import isel.dei.pdm.mygamevault.domain.PlayStatus
import kotlinx.coroutines.flow.Flow

@Dao
internal interface GameDao {
    /**
     * Searches for entries in the collection where the game name contains [partialName].
     * Limits the result to [limit] entries, ordered by acquisition date.
     */
    @Query("""
        SELECT * FROM collection_entries
        WHERE gameId IN (SELECT id FROM games WHERE name LIKE '%' || :partialName || '%')
        ORDER BY addedAt DESC LIMIT :limit
    """)
    fun searchByName(partialName: String, limit: Int): Flow<List<CollectionEntryWithDetails>>

    /**
     * Searches for entries in the collection that belong to the given [platformIds].
     * Limits the result to [limit] entries, ordered by acquisition date.
     */
    @Query("""
        SELECT * FROM collection_entries 
        WHERE platformId IN (:platformIds)
        ORDER BY addedAt DESC LIMIT :limit
    """)
    fun searchByPlatforms(platformIds: Set<Long>, limit: Int): Flow<List<CollectionEntryWithDetails>>

    /**
     * Searches for entries in the collection that are in one of the given [states].
     * Limits the result to [limit] entries, ordered by acquisition date.
     */
    @Query("""
        SELECT * FROM collection_entries 
        WHERE state IN (:states)
        ORDER BY addedAt DESC LIMIT :limit
    """)
    fun searchByStates(states: Set<PlayStatus.State>, limit: Int): Flow<List<CollectionEntryWithDetails>>

    /**
     * Returns the latest [limit] entries in the collection.
     */
    @Query("""
        SELECT * FROM collection_entries 
        ORDER BY addedAt DESC LIMIT :limit
    """)
    fun searchLatest(limit: Int): Flow<List<CollectionEntryWithDetails>>

    @Query("SELECT COUNT(*) FROM collection_entries")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM games")
    suspend fun countGames(): Int

    @Upsert
    suspend fun upsertGame(game: GameEntity)

    @Upsert
    suspend fun upsertPlatform(platform: PlatformEntity)

    @Upsert
    suspend fun upsertEntry(entry: CollectionEntryEntity)

    @Transaction
    suspend fun saveEntry(game: GameEntity, platform: PlatformEntity, entry: CollectionEntryEntity) {
        upsertGame(game)
        upsertPlatform(platform)
        upsertEntry(entry)
    }

    @Query("DELETE FROM collection_entries WHERE gameId = :gameId AND platformId = :platformId")
    suspend fun deleteEntry(gameId: Long, platformId: Long)

    @Transaction
    @Query("SELECT * FROM collection_entries WHERE gameId = :gameId AND platformId = :platformId")
    suspend fun getEntry(gameId: Long, platformId: Long): CollectionEntryWithDetails?

    @Transaction
    @Query("SELECT * FROM collection_entries WHERE gameId = :gameId AND platformId = :platformId")
    fun observeEntry(gameId: Long, platformId: Long): Flow<CollectionEntryWithDetails?>

    @Upsert
    suspend fun upsertActiveSession(session: ActiveSessionEntity)

    @Query("DELETE FROM active_session")
    suspend fun deleteActiveSession()

    @Query("SELECT * FROM active_session LIMIT 1")
    fun observeActiveSession(): Flow<ActiveSessionEntity?>

    @Query("SELECT * FROM active_session LIMIT 1")
    suspend fun getActiveSessionOnce(): ActiveSessionEntity?
}
