package isel.dei.pdm.demos.gamevault.adapters.igdb

import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.UnexpectedServiceException
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * A [PlatformSearchService] that searches for platforms in IGDB.
 *
 * Each version of a platform is returned as a distinct [PlatformInfo]. IGDB names the original
 * version of most platforms "Initial version", which is returned without version.
 *
 * @param api The API used to execute the queries.
 */
class IgdbPlatformSearchService(private val api: IgdbApi) : PlatformSearchService {

    override suspend fun search(query: SearchString): List<PlatformInfo> {
        val json = api.query(endpoint = "platforms", query = platformsQuery(query))
        val platforms = try {
            igdbJson.decodeFromString<List<IgdbPlatform>>(json)
        } catch (e: SerializationException) {
            throw UnexpectedServiceException("Unexpected response from IGDB", e)
        }
        return platforms.flatMap { it.toPlatformInfos() }
    }
}

/**
 * The maximum number of results (not counting distinct versions) returned by a search.
 */
private const val MAX_RESULTS = 50

/**
 * Builds the Apicalypse query that searches for the platforms whose name, abbreviation or
 * alternative name contains the given text, ignoring case.
 * @param [query] The text to search for.
 * @return the payload to be sent in IGDB requests, following the syntax of the Apicalypse idiom.
 */
internal fun platformsQuery(query: SearchString): String {
    val text = query.value.trim().escapeForApicalypse()
    return """
        fields abbreviation, name, alternative_name, platform_logo.image_id, versions.name, versions.platform_logo.image_id;
        where name ~ *"$text"* | abbreviation ~ *"$text"* | alternative_name ~ *"$text"*;
        sort name asc;
        limit $MAX_RESULTS;
    """.trimIndent()
}

/**
 * Escapes the characters that have a special meaning inside Apicalypse strings, so that the
 * user's text cannot change the query. This prevents injection attacks.
 * @return the escaped string.
 */
private fun String.escapeForApicalypse(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")

/**
 * A platform, as returned by IGDB. Only the requested fields are declared.
 */
@Serializable
internal data class IgdbPlatform(
    val id: Long,
    val name: String,
    val abbreviation: String? = null,
    @SerialName("alternative_name") val alternativeName: String? = null,
    @SerialName("platform_logo") val logo: IgdbLogo? = null,
    val versions: List<IgdbPlatformVersion> = emptyList()
)

@Serializable
internal data class IgdbPlatformVersion(
    val name: String? = null,
    @SerialName("platform_logo") val logo: IgdbLogo? = null
)

@Serializable
internal data class IgdbLogo(
    @SerialName("image_id") val imageId: String
)

/**
 * The name IGDB gives to the original version of most platforms.
 */
private const val INITIAL_VERSION = "Initial version"

/**
 * Converts this platform into one [PlatformInfo] per version (or a single one, if it has no
 * versions). The conversion rules are:
 * - Platforms without abbreviation (there are a few) use their name as abbreviation.
 * - The version is omitted if it is the initial one, or if it has the same name as the platform.
 * - Versions without logo use the platform's logo.
 * - Versions that end up with the same name are only included once, because the name of the
 * version identifies it among the platform's versions.
 * - Platforms whose data does not meet the domain requirements (e.g. a blank name) are skipped,
 * instead of failing the whole search.
 */
internal fun IgdbPlatform.toPlatformInfos(): List<PlatformInfo> {
    val abbreviation = abbreviation?.takeIf { it.isNotBlank() } ?: name
    fun platformInfo(version: String?, logo: IgdbLogo?) =
        PlatformInfo(id, abbreviation, name, logo?.toUri(), version)

    return try {
        if (versions.isEmpty())
            listOf(platformInfo(version = null, logo = logo))
        else
            versions
                .map { platformInfo(version = it.versionName(platformName = name), logo = it.logo ?: logo) }
                .distinctBy { it.version }
    } catch (_: IllegalArgumentException) {
        emptyList()
    }
}

/**
 * The name of this version, or null if it is the initial version or has the same name as the
 * platform (i.e. it is not a relevant variation of the platform).
 */
private fun IgdbPlatformVersion.versionName(platformName: String): String? =
    name?.takeUnless {
        it.isBlank() ||
            it.equals(INITIAL_VERSION, ignoreCase = true) ||
            it.equals(platformName, ignoreCase = true)
    }

/**
 * The URI of this logo. IGDB images are available in several sizes: logo_med (284x160) suits
 * the list items, and PNG preserves the logo's transparency.
 */
private fun IgdbLogo.toUri(): String =
    "https://images.igdb.com/igdb/image/upload/t_logo_med/$imageId.png"
