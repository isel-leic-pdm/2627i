package isel.dei.pdm.demos.gamevault.search.platforms.adapters

import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of the [PlatformSearchService] interface.
 * We use it to have fine control on the service behavior while developing the app.
 */
class FakePlatformSearchService : PlatformSearchService {

    private val platforms = listOf(
        PlatformInfo(1L, "PS5", "PlayStation 5", "https://fake.url/image/ps5_logo.png"),
        PlatformInfo(2L, "XSX", "Xbox Series X/S", "https://fake.url/image/xbox_logo.png"),
        PlatformInfo(3L, "NSW", "Nintendo Switch", "https://fake.url/image/switch_logo.png"),
        PlatformInfo(4L, "PC", "PC (Microsoft Windows)", "https://fake.url/image/pc_logo.png"),
        PlatformInfo(5L, "PS4", "PlayStation 4", "https://fake.url/image/ps4_logo.png"),
        PlatformInfo(6L, "XONE", "Xbox One", "https://fake.url/image/xbox_one_logo.png")
    )

    override suspend fun search(name: SearchString?, abbreviation: SearchString?): Result<List<PlatformInfo>> {
        delay(500.milliseconds) // Simulate network delay
        val filtered = platforms.filter { platform ->
            val matchesName = name == null || platform.name.value.contains(name.value, ignoreCase = true)
            val matchesAbbr = abbreviation == null || platform.abbreviation.value.contains(abbreviation.value, ignoreCase = true)
            matchesName && matchesAbbr
        }
        return Result.success(filtered)
    }
}
