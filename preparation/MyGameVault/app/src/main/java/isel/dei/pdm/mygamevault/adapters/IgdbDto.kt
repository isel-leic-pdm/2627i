package isel.dei.pdm.mygamevault.adapters

import isel.dei.pdm.mygamevault.domain.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId

@Serializable
data class IgdbGame(
    val id: Long,
    val name: String,
    @SerialName("first_release_date") val firstReleaseDate: Long? = null,
    val cover: IgdbCover? = null
)

@Serializable
data class IgdbCover(
    val id: Long,
    val url: String
)

/**
 * Maps an [IgdbGame] to a [Game] domain object.
 */
fun IgdbGame.toGame(): Game {
    val releaseDate = firstReleaseDate?.let {
        Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    
    // IGDB cover URLs often start with //, so we prepended https:
    val coverUrl = cover?.url?.let { if (it.startsWith("//")) "https:$it" else it }
    
    // Choosing a larger size for the cover and keeping thumb for thumbnail
    val bigCoverUrl = coverUrl?.replace("t_thumb", "t_cover_big")
    val thumbUrl = coverUrl?.replace("t_thumb", "t_thumb")

    return Game(
        id = id,
        name = name,
        releaseDate = releaseDate,
        coverUri = bigCoverUrl,
        thumbnailUri = thumbUrl
    )
}
