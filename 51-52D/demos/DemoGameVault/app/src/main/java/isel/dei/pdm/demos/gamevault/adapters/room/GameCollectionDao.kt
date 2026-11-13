package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Data access object for the games in the user's collection.
 *
 * It offers the storage operations the repository is built upon: one query per kind of read, and
 * the writes that must be atomic in themselves (e.g. storing an entry along with its runs). The
 * domain rules that span several entries (one entry per game, one active session overall) are
 * enforced by the repository, which combines these operations in its own transactions.
 *
 * Listings are sorted by name, ignoring case, except that the game with the active session, if
 * any, comes first.
 */
@Dao
abstract class GameCollectionDao {

    // Reading entries

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    abstract suspend fun getEntry(gameId: Long): EntryWithRelations?

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    abstract fun observeEntry(gameId: Long): Flow<EntryWithRelations?>

    @Transaction
    @Query("SELECT * FROM games WHERE session_start_time IS NOT NULL LIMIT 1")
    abstract suspend fun getActiveSession(): EntryWithRelations?

    @Transaction
    @Query("SELECT * FROM games WHERE session_start_time IS NOT NULL LIMIT 1")
    abstract fun observeActiveSession(): Flow<EntryWithRelations?>

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    abstract fun observeDetails(gameId: Long): Flow<GameWithDetails?>

    /**
     * The games that may be the same as a game with the given release date, according to the
     * domain rules (see [isel.dei.pdm.demos.gamevault.domain.collection.games.NewGame.isSameGameAs]):
     * those whose release year is the same, if known for both. The query only narrows the
     * candidates down, as the rules compare normalized names, which SQL cannot express: the
     * repository applies the rules to the candidates.
     * @param releaseYearStart The first day of the game's release year, or null if unknown (in
     * which case all games are candidates).
     * @param releaseYearEnd The last day of the game's release year, or null if unknown.
     */
    @Query(
        """
        SELECT * FROM games
        WHERE :releaseYearStart IS NULL OR release_date IS NULL
           OR release_date BETWEEN :releaseYearStart AND :releaseYearEnd
        """
    )
    abstract suspend fun getSameGameCandidates(
        releaseYearStart: LocalDate?,
        releaseYearEnd: LocalDate?
    ): List<GameEntity>

    // Listings and summary

    /**
     * The listing items of the games that meet the given criteria. All criteria are combined, and
     * those left at their "any" value are not applied (see
     * [isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery]).
     * @param state Only games in this play state (its name), or any state if null.
     * @param platinumOnly Only games whose platinum was obtained.
     * @param anyPlatform Whether games owned on any platform are included, in which case
     * [platformIds] is ignored. Room cannot check whether a list parameter is null, hence the flag.
     * @param platformIds Only games owned on one of these platforms: the versions of the platform
     * games are filtered by (see [GamePlatformEntity]).
     * @param nameContains Only games whose name contains this text, ignoring case, or any name if
     * null. Matched with LIKE, so its wildcards must be escaped with a backslash (see
     * [observeItems], which does it).
     * @param initial Only games whose name is listed under this initial: an upper case letter,
     * or '#' for names starting with digits or symbols (see
     * [isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionQuery.OTHER_INITIAL]), or
     * any initial if null.
     * @param limit The maximum number of items. To find out whether more games meet the
     * criteria, ask for one more item than needed.
     */
    @Transaction
    @Query(
        """
        SELECT g.id, g.name, g.release_date, g.cover_uri, g.thumbnail_uri, g.state,
               g.time_spent_minutes, g.has_platinum,
               EXISTS (SELECT 1 FROM completed_runs r WHERE r.game_id = g.id) AS is_finished,
               g.session_start_time IS NOT NULL AS is_session_active
        FROM games g
        WHERE (:state IS NULL OR g.state = :state)
          AND (NOT :platinumOnly OR g.has_platinum)
          AND (:anyPlatform OR EXISTS (
                SELECT 1 FROM game_platforms gp
                WHERE gp.game_id = g.id AND gp.platform_id IN (:platformIds)))
          AND (:nameContains IS NULL OR g.name LIKE '%' || :nameContains || '%' ESCAPE '\')
          AND (:initial IS NULL
               OR (:initial = '#' AND NOT (upper(substr(ltrim(g.name), 1, 1)) BETWEEN 'A' AND 'Z'))
               OR upper(substr(ltrim(g.name), 1, 1)) = :initial)
        ORDER BY g.session_start_time IS NULL, g.name
        LIMIT :limit
        """
    )
    protected abstract fun observeItemRows(
        state: String?,
        platinumOnly: Boolean,
        anyPlatform: Boolean,
        platformIds: List<Long>,
        nameContains: String?,
        initial: String?,
        limit: Int
    ): Flow<List<CollectionItemRow>>

    /**
     * The listing items of the games that meet the given criteria (see [observeItemRows]).
     * The text to look for in the names is taken literally: its LIKE wildcards are escaped.
     * @param platformIds Only games owned on one of these platforms, or any platform if null.
     * To filter by a platform, pass the identifiers of all its versions, as recognizing them is
     * a domain rule (see
     * [isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform.isSameGamePlatformAs]).
     */
    open fun observeItems(
        state: String?,
        platinumOnly: Boolean,
        platformIds: List<Long>?,
        nameContains: String?,
        initial: Char?,
        limit: Int
    ): Flow<List<CollectionItemRow>> = observeItemRows(
        state = state,
        platinumOnly = platinumOnly,
        anyPlatform = platformIds == null,
        platformIds = platformIds.orEmpty(),
        nameContains = nameContains?.escapeLikeWildcards(),
        initial = initial?.toString(),
        limit = limit
    )

    /**
     * The summary of the collection, for the year between the given dates (see
     * [isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary]). It follows the
     * domain rules on runs (see [isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus]):
     * - The first run is the earliest one, and undated runs are the earliest. So, a game was first
     * finished in the year if it has no undated runs and its earliest dated run is in the year
     * (MIN ignores the undated runs, hence the explicit check).
     * - A game was replayed in the year if it has runs in the year other than the first one, i.e.
     * more runs in the year than the first one accounts for.
     * @param yearStart The first day of the year.
     * @param yearEnd The last day of the year.
     */
    @Query(
        """
        WITH per_game AS (
            SELECT g.state, g.time_spent_minutes, g.has_platinum, g.platinum_on,
                   (SELECT COUNT(*) FROM completed_runs r WHERE r.game_id = g.id) AS runs,
                   (SELECT COUNT(*) FROM completed_runs r
                    WHERE r.game_id = g.id AND r.completed_on BETWEEN :yearStart AND :yearEnd
                   ) AS runs_in_year,
                   COALESCE(
                       NOT EXISTS (SELECT 1 FROM completed_runs r
                                   WHERE r.game_id = g.id AND r.completed_on IS NULL)
                       AND (SELECT MIN(r.completed_on) FROM completed_runs r WHERE r.game_id = g.id)
                           BETWEEN :yearStart AND :yearEnd,
                       0
                   ) AS first_finished_in_year
            FROM games g
        )
        SELECT
            COUNT(*) AS owned,
            COALESCE(SUM(first_finished_in_year), 0) AS finished_in_year,
            COALESCE(SUM(runs_in_year > first_finished_in_year), 0) AS replayed_in_year,
            COALESCE(SUM(platinum_on BETWEEN :yearStart AND :yearEnd), 0) AS platinum_in_year,
            COALESCE(SUM(state NOT IN ('NONE', 'BACKLOG') OR time_spent_minutes > 0
                         OR runs > 0 OR has_platinum), 0) AS played,
            COALESCE(SUM(runs > 0), 0) AS finished,
            COALESCE(SUM(has_platinum), 0) AS platinum
        FROM per_game
        """
    )
    abstract fun observeSummary(yearStart: LocalDate, yearEnd: LocalDate): Flow<CollectionSummaryRow>

    // Writing entries

    @Insert
    protected abstract suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPlatformLink(link: GamePlatformEntity)

    @Insert
    protected abstract suspend fun insertPlatformLinks(links: List<GamePlatformEntity>)

    @Insert
    protected abstract suspend fun insertRuns(runs: List<CompletedRunEntity>)

    @Insert
    protected abstract suspend fun insertDetails(details: GameDetailsEntity)

    @Query("DELETE FROM game_platforms WHERE game_id = :gameId")
    protected abstract suspend fun deletePlatformLinks(gameId: Long)

    @Query("DELETE FROM completed_runs WHERE game_id = :gameId")
    protected abstract suspend fun deleteRuns(gameId: Long)

    @Update
    protected abstract suspend fun updateGameRow(game: GameEntity): Int

    /**
     * Inserts a new game, owned on the given platform, along with its runs and details,
     * atomically. The game identifiers of the given runs and details are ignored, as the game's
     * identifier is only known once it is inserted.
     * @return The identifier of the inserted game.
     */
    @Transaction
    open suspend fun insert(
        game: GameEntity,
        platformId: Long,
        runs: List<CompletedRunEntity>,
        details: GameDetailsEntity
    ): Long {
        val gameId = insertGame(game)
        insertPlatformLink(GamePlatformEntity(gameId = gameId, platformId = platformId))
        insertRuns(runs.map { it.copy(id = 0, gameId = gameId) })
        insertDetails(details.copy(gameId = gameId))
        return gameId
    }

    /**
     * Replaces the stored entry of the given game (its row, platforms and runs) with the given
     * one, atomically. The identifiers of the given runs are ignored, as runs are replaced.
     * @return Whether the game was updated (false if it is not in the collection).
     */
    @Transaction
    open suspend fun update(
        game: GameEntity,
        platforms: List<GamePlatformEntity>,
        runs: List<CompletedRunEntity>
    ): Boolean {
        if (updateGameRow(game) == 0) return false
        deletePlatformLinks(game.id)
        insertPlatformLinks(platforms.map { it.copy(gameId = game.id) })
        deleteRuns(game.id)
        insertRuns(runs.map { it.copy(id = 0, gameId = game.id) })
        return true
    }

    /**
     * Updates the game's own information, leaving its entry untouched.
     * @return Whether the game was updated (false if it is not in the collection).
     */
    @Update(entity = GameEntity::class)
    protected abstract suspend fun updateGameInfoRow(info: GameInfoUpdate): Int

    open suspend fun updateGameInfo(info: GameInfoUpdate): Boolean = updateGameInfoRow(info) > 0

    @Update
    protected abstract suspend fun updateDetailsRow(details: GameDetailsEntity): Int

    /**
     * Updates the details of a game.
     * @return Whether the details were updated (false if the game is not in the collection).
     */
    open suspend fun updateDetails(details: GameDetailsEntity): Boolean =
        updateDetailsRow(details) > 0

    /**
     * Deletes the given game, along with its runs, its details and its links to the platforms it
     * is owned on (CASCADE). The platforms themselves are kept, even if no other games are owned
     * on them (see [GamePlatformEntity]).
     * @return The number of deleted games (0 or 1).
     */
    @Query("DELETE FROM games WHERE id = :gameId")
    abstract suspend fun delete(gameId: Long): Int
}

/**
 * Escapes the LIKE wildcards (% and _) and the escape character itself, so that the string is
 * matched literally.
 */
private fun String.escapeLikeWildcards(): String =
    replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
