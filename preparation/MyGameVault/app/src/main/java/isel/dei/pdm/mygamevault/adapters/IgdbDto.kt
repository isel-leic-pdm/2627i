package isel.dei.pdm.mygamevault.adapters

import isel.dei.pdm.mygamevault.domain.Game
import isel.dei.pdm.mygamevault.domain.GameDetails
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId

@Serializable
data class IgdbGame(
    val id: Long,
    val name: String,
    @SerialName("first_release_date") val firstReleaseDate: Long? = null,
    val cover: IgdbCover? = null,
    val summary: String? = null,
    @SerialName("involved_companies") val involvedCompanies: List<IgdbInvolvedCompany>? = null,
    val genres: List<IgdbGenre>? = null
)

@Serializable
data class IgdbInvolvedCompany(
    val company: IgdbCompany,
    val developer: Boolean,
    val publisher: Boolean
)

@Serializable
data class IgdbCompany(
    val name: String
)

@Serializable
data class IgdbGenre(
    val name: String
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

/**
 * Maps an [IgdbGame] to a [GameDetails] domain object.
 */
fun IgdbGame.toGameDetails(): GameDetails {
    val game = toGame()
    val developers = involvedCompanies?.filter { it.developer }?.map { it.company.name } ?: emptyList()
    val publishers = involvedCompanies?.filter { it.publisher }?.map { it.company.name } ?: emptyList()
    val genreNames = genres?.map { it.name } ?: emptyList()

    return GameDetails(
        game = game,
        description = summary,
        developers = developers,
        publishers = publishers,
        genres = genreNames
    )
}
