package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.games.Game
import isel.dei.pdm.demos.gamevault.domain.collection.games.GameDetails
import isel.dei.pdm.demos.gamevault.domain.collection.games.NewGameDetails
import kotlinx.serialization.json.Json

/**
 * The database representation of the details of a game in the user's collection (see
 * [GameDetails]). They are kept apart from [GameEntity], in a one-to-one relation, so that they
 * are only loaded when working with a single game, as in the domain.
 *
 * The developers, publishers and genres are lists of strings that are only read and written as a
 * whole, and never queried, so each one is stored in a single column, as a JSON array (see
 * [StringListConverters]). Should the collection ever be queried by them (e.g. browsing by
 * genre), they should be moved to tables of their own instead.
 *
 * Removing a game removes its details (CASCADE).
 *
 * @property gameId The identifier of the game the details refer to.
 * @property description A summary or description of the game, if available.
 * @property developers The developers of the game, in the order they are to be displayed.
 * @property publishers The publishers of the game, in the order they are to be displayed.
 * @property genres The genres of the game, in the order they are to be displayed.
 * @property notes The user's personal notes about the game, if any.
 */
@Entity(
    tableName = "game_details",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(StringListConverters::class)
data class GameDetailsEntity(
    @PrimaryKey
    @ColumnInfo(name = "game_id")
    val gameId: Long,
    val description: String?,
    val developers: List<String>,
    val publishers: List<String>,
    val genres: List<String>,
    val notes: String?
)

/**
 * Converts the details of a game being added to the collection to their database representation.
 * @param gameId The identifier assigned to the game.
 */
fun NewGameDetails.toEntity(gameId: Long): GameDetailsEntity = GameDetailsEntity(
    gameId = gameId,
    description = description?.value,
    developers = developers.map { it.value },
    publishers = publishers.map { it.value },
    genres = genres.map { it.value },
    notes = notes?.value
)

/**
 * Converts these details to their database representation.
 */
fun GameDetails.toEntity(): GameDetailsEntity = GameDetailsEntity(
    gameId = game.id,
    description = description?.value,
    developers = developers.map { it.value },
    publishers = publishers.map { it.value },
    genres = genres.map { it.value },
    notes = notes?.value
)

/**
 * Converts this database representation to the details of the given game.
 * @param game The game the details refer to.
 * @throws IllegalArgumentException if the stored data does not meet the domain requirements.
 */
fun GameDetailsEntity.toGameDetails(game: Game): GameDetails {
    require(game.id == gameId) { "The details do not refer to the given game" }
    return GameDetails(
        game = game,
        description = description?.let { NonBlankString(it) },
        developers = developers.map { NonBlankString(it) },
        publishers = publishers.map { NonBlankString(it) },
        genres = genres.map { NonBlankString(it) },
        notes = notes?.let { NonBlankString(it) }
    )
}

/**
 * Room type converters that store lists of strings as JSON arrays (e.g. ["Action","RPG"]).
 * Decoding malformed JSON throws a SerializationException, which is an IllegalArgumentException,
 * so it is handled as data that does not meet the domain requirements.
 */
class StringListConverters {
    @TypeConverter
    fun fromList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun toList(json: String): List<String> = Json.decodeFromString(json)
}
