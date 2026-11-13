package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.Year
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionItem
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionSummary
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.PlayStatus
import isel.dei.pdm.demos.gamevault.domain.collection.games.toPlayTime
import isel.dei.pdm.demos.gamevault.domain.Uri
import kotlinx.datetime.LocalDate
import kotlin.time.Duration.Companion.minutes

// The shapes in which the DAO reads and writes the collection, other than its entities. Those
// with relations (@Relation) are loaded by Room with additional queries, so the DAO methods that
// return them are annotated with @Transaction, to read a consistent snapshot.

/**
 * A game with all the information of its collection entry: the platforms it is owned on and its
 * completed runs.
 */
data class EntryWithRelations(
    @Embedded
    val game: GameEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GamePlatformEntity::class,
            parentColumn = "game_id",
            entityColumn = "platform_id"
        )
    )
    val platforms: List<PlatformEntity>,
    @Relation(parentColumn = "id", entityColumn = "game_id")
    val runs: List<CompletedRunEntity>
)

/**
 * Converts this database representation to the corresponding collection entry.
 * @throws IllegalArgumentException if the stored data does not meet the domain requirements.
 */
fun EntryWithRelations.toCollectionEntry(): CollectionEntry =
    game.toCollectionEntry(platforms.map { it.toPlatform() }, runs)

/**
 * A game with its details. The details are nullable because Room cannot guarantee that the row
 * exists, although the repository always stores them along with the game.
 */
data class GameWithDetails(
    @Embedded
    val game: GameEntity,
    @Relation(parentColumn = "id", entityColumn = "game_id")
    val details: GameDetailsEntity?
)

/**
 * Converts this database representation to the corresponding game details.
 * @throws IllegalArgumentException if the details are missing or if the stored data does not
 * meet the domain requirements.
 */
fun GameWithDetails.toGameDetails(): GameDetails {
    val details = requireNotNull(details) { "Game stored without details" }
    return details.toGameDetails(game.toGame())
}

/**
 * The columns of a listing item (see [CollectionItem]), computed by the listing query, along with
 * the abbreviations of the platforms the game is owned on. Only the abbreviations are loaded, not
 * the whole platforms (see [Relation.projection]).
 */
data class CollectionItemRow(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: LocalDate?,
    @ColumnInfo(name = "cover_uri")
    val coverUri: String?,
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String?,
    val state: String,
    @ColumnInfo(name = "time_spent_minutes")
    val timeSpentMinutes: Int,
    @ColumnInfo(name = "is_finished")
    val isFinished: Boolean,
    @ColumnInfo(name = "has_platinum")
    val hasPlatinum: Boolean,
    @ColumnInfo(name = "is_session_active")
    val isSessionActive: Boolean,
    @Relation(
        entity = PlatformEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = GamePlatformEntity::class,
            parentColumn = "game_id",
            entityColumn = "platform_id"
        ),
        projection = ["abbreviation"]
    )
    val platformAbbreviations: List<String>
)

/**
 * Converts this row to the corresponding listing item, consistently with
 * [isel.dei.pdm.demos.gamevault.domain.collection.games.toItem].
 * @throws IllegalArgumentException if the stored data does not meet the domain requirements.
 */
fun CollectionItemRow.toCollectionItem(): CollectionItem = CollectionItem(
    gameId = id,
    name = NonBlankString(name),
    releaseYear = releaseDate?.let { Year.of(it) },
    coverUri = coverUri?.let { Uri(it) },
    thumbnailUri = thumbnailUri?.let { Uri(it) },
    platformAbbreviations = platformAbbreviations.map { NonBlankString(it) },
    state = PlayStatus.State.valueOf(state),
    timeSpent = timeSpentMinutes.minutes.toPlayTime(),
    isFinished = isFinished,
    hasPlatinum = hasPlatinum,
    isSessionActive = isSessionActive
)

/**
 * The figures of the collection's summary, computed by the summary query (see
 * [CollectionSummary] for their meaning).
 */
data class CollectionSummaryRow(
    @ColumnInfo(name = "finished_in_year")
    val finishedInYear: Int,
    @ColumnInfo(name = "replayed_in_year")
    val replayedInYear: Int,
    @ColumnInfo(name = "platinum_in_year")
    val platinumInYear: Int,
    val owned: Int,
    val played: Int,
    val finished: Int,
    val platinum: Int
)

/**
 * Converts this row to the summary of the given year.
 * @param year The year the yearly figures refer to.
 */
fun CollectionSummaryRow.toCollectionSummary(year: Year): CollectionSummary = CollectionSummary(
    year = year,
    finishedInYear = finishedInYear,
    replayedInYear = replayedInYear,
    platinumInYear = platinumInYear,
    owned = owned,
    played = played,
    finished = finished,
    platinum = platinum
)

/**
 * The columns of a game that hold its own information (see [Game]), as opposed to its entry's.
 * Used to update the game without touching its entry (see [GameCollectionDao.updateGameInfo]).
 */
data class GameInfoUpdate(
    val id: Long,
    val name: String,
    @ColumnInfo(name = "release_date")
    val releaseDate: LocalDate?,
    @ColumnInfo(name = "cover_uri")
    val coverUri: String?,
    @ColumnInfo(name = "thumbnail_uri")
    val thumbnailUri: String?
)

/**
 * Converts this game to the columns that hold its own information.
 */
fun Game.toInfoUpdate(): GameInfoUpdate = GameInfoUpdate(
    id = id,
    name = name.value,
    releaseDate = releaseDate,
    coverUri = coverUri?.toString(),
    thumbnailUri = thumbnailUri?.toString()
)
