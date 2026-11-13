package isel.dei.pdm.demos.gamevault.domain.search.games

import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.distinctGamePlatforms

/**
 * Describes a platform of the user's collection in terms the game search service can use to
 * search games for it: its names. The vault does not keep the search provider's identifiers, so
 * platforms are matched by name, against the names the provider knows them by. A match requires
 * the name or the abbreviation to be equal, ignoring case, to one of those names.
 *
 * Versions of a platform (e.g. PlayStation 5 and its Pro version) are not distinguished, because
 * games are released for the platform, not for its versions (see [Platform.isSameGamePlatformAs]).
 * As the version is not part of the name, they have the same criterion.
 *
 * @property name The platform's full name (e.g. PlayStation 5).
 * @property abbreviation The platform's abbreviation (e.g. PS5).
 */
data class PlatformCriterion(
    val name: NonBlankString,
    val abbreviation: NonBlankString
) {
    /**
     * Checks whether the given platform, as provided by the search service, is the one described
     * by this criterion, i.e. the one known by the same name or abbreviation.
     * @param platform A platform a game was released for.
     */
    fun matches(platform: GamePlatform): Boolean {
        val knownNames = listOfNotNull(platform.name, platform.abbreviation) +
            platform.alternativeNames
        return knownNames.any {
            it.value.equals(name.value, ignoreCase = true) ||
                it.value.equals(abbreviation.value, ignoreCase = true)
        }
    }
}

/**
 * The criterion used to search games for this platform.
 */
fun Platform.toSearchCriterion(): PlatformCriterion = PlatformCriterion(name, abbreviation)

/**
 * The platforms, among the given ones, that this game was released for, as determined by the
 * criteria used to search for it (see [toSearchCriterion]). Meant, for example, to obtain the
 * platforms of the user's collection a search result can be added on. Games are oblivious of the
 * platforms' versions, so each platform is included once, through the first of its versions (see
 * [distinctGamePlatforms]).
 * @param platforms The candidate platforms (e.g. the platforms of the user's collection).
 * @return The platforms the game was released for, in the order of [platforms].
 */
fun GameInfo.releasedFor(platforms: List<Platform>): List<Platform> =
    platforms.distinctGamePlatforms().filter { candidate ->
        val criterion = candidate.toSearchCriterion()
        this.platforms.any { criterion.matches(it) }
    }
