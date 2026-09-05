package isel.dei.pdm.mygamevault.adapters.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import isel.dei.pdm.mygamevault.domain.CollectionEntry
import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.NonBlankString
import isel.dei.pdm.mygamevault.domain.Platform
import isel.dei.pdm.mygamevault.domain.PlayStatus
import isel.dei.pdm.mygamevault.domain.Uri
import isel.dei.pdm.mygamevault.domain.toPlayTime
import kotlin.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.seconds

@Entity(tableName = "games")
internal data class GameEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val releaseDate: Long?,
    val coverUri: String?,
    val thumbnailUri: String?
)

@Entity(tableName = "platforms")
internal data class PlatformEntity(
    @PrimaryKey val id: Long,
    val abbreviation: String,
    val name: String,
    val logoUri: String?
)

@Entity(
    tableName = "collection_entries",
    primaryKeys = ["gameId", "platformId"],
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PlatformEntity::class,
            parentColumns = ["id"],
            childColumns = ["platformId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class CollectionEntryEntity(
    val gameId: Long,
    val platformId: Long,
    val timeSpentSeconds: Long,
    val state: PlayStatus.State,
    val completedRuns: Int,
    val addedAt: Long
)

@Entity(
    tableName = "active_session",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntryEntity::class,
            parentColumns = ["gameId", "platformId"],
            childColumns = ["gameId", "platformId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
internal data class ActiveSessionEntity(
    @PrimaryKey val id: Int = 1,
    val gameId: Long,
    val platformId: Long,
    val startTimeSeconds: Long
)

/**
 * POJO that represents a collection entry with all its details.
 */
internal data class CollectionEntryWithDetails(
    @Embedded
    val entry: CollectionEntryEntity,
    @Relation(
        parentColumn = "gameId",
        entityColumn = "id"
    )
    val game: GameEntity,
    @Relation(
        parentColumn = "platformId",
        entityColumn = "id"
    )
    val platform: PlatformEntity
)

// Mappers

internal fun GameEntity.toGame() = Game(
    id = id,
    name = NonBlankString(name),
    releaseDate = releaseDate?.let { LocalDate.ofEpochDay(it) },
    coverUri = coverUri?.let { Uri(it) },
    thumbnailUri = thumbnailUri?.let { Uri(it) }
)

internal fun Game.toEntity() = GameEntity(
    id = id,
    name = name(),
    releaseDate = releaseDate?.toEpochDay(),
    coverUri = coverUri?.value,
    thumbnailUri = thumbnailUri?.value
)

internal fun PlatformEntity.toPlatform() = Platform(
    id = id,
    abbreviation = NonBlankString(abbreviation),
    name = NonBlankString(name),
    logoUri = logoUri?.let { Uri(it) }
)

internal fun Platform.toEntity() = PlatformEntity(
    id = id,
    abbreviation = abbreviation(),
    name = name(),
    logoUri = logoUri?.value
)

internal fun CollectionEntryWithDetails.toCollectionEntry(sessionStartTime: Instant? = null) = CollectionEntry(
    game = game.toGame(),
    platform = platform.toPlatform(),
    playStatus = PlayStatus(
        timeSpent = entry.timeSpentSeconds.seconds.toPlayTime(),
        state = entry.state,
        completedRuns = entry.completedRuns
    ),
    addedAt = LocalDate.ofEpochDay(entry.addedAt),
    sessionStartTime = sessionStartTime
)

internal fun CollectionEntry.toEntity() = CollectionEntryEntity(
    gameId = game.id,
    platformId = platform.id,
    timeSpentSeconds = playStatus.timeSpent.toDuration().inWholeSeconds,
    state = playStatus.state,
    completedRuns = playStatus.completedRuns,
    addedAt = addedAt.toEpochDay()
)
