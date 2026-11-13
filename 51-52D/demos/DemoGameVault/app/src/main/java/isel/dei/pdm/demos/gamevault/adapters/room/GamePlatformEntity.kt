package isel.dei.pdm.demos.gamevault.adapters.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry

/**
 * The database representation of a game being owned on a platform, i.e. the many-to-many
 * relation between games and platforms (see [CollectionEntry.platforms]).
 *
 * The rule that an entry is not owned on two versions of the same platform (see
 * [CollectionEntry.isOwnedOn]) is enforced by [CollectionEntry] itself: it is not expressible in
 * the schema, as versions are recognized by comparing several columns of the platforms.
 *
 * Platforms belong to the collection on their own, regardless of the games owned on them: removing
 * a game removes its rows (CASCADE), but never the platforms, not even when no other games are
 * owned on them. Conversely, removing a platform the games are owned on is not allowed (RESTRICT),
 * as the games would be left without it.
 *
 * Each row refers to the version of the platform through which the game was registered, which is
 * merely a reference: as far as games are concerned, all versions of a platform are the same
 * platform (see [isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform.isSameGamePlatformAs]).
 * Hence, filtering games by platform must include all its versions (see
 * [GameCollectionDao.observeItems]).
 *
 * @property gameId The game's identifier.
 * @property platformId The identifier of the platform the game is owned on.
 */
@Entity(
    tableName = "game_platforms",
    primaryKeys = ["game_id", "platform_id"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlatformEntity::class,
            parentColumns = ["id"],
            childColumns = ["platform_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        // Games are filtered by platform (see CollectionQuery.platformId). The primary key's
        // index does not help, as platform_id is not its first column.
        Index(value = ["platform_id"])
    ]
)
data class GamePlatformEntity(
    @ColumnInfo(name = "game_id")
    val gameId: Long,
    @ColumnInfo(name = "platform_id")
    val platformId: Long
)

/**
 * Converts the platforms of this entry to their database representation.
 */
fun CollectionEntry.toPlatformEntities(): List<GamePlatformEntity> =
    platforms.map { GamePlatformEntity(gameId = game.id, platformId = it.id) }
