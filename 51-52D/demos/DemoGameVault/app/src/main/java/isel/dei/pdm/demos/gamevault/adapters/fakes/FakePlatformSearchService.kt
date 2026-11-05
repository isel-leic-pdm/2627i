package isel.dei.pdm.demos.gamevault.adapters.fakes

import isel.dei.pdm.demos.gamevault.domain.search.SearchString
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformInfo
import isel.dei.pdm.demos.gamevault.domain.search.platforms.PlatformSearchService
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * A fake implementation of the [PlatformSearchService] interface.
 * We use it to have fine control over the service behavior while developing the app.
 */
class FakePlatformSearchService : PlatformSearchService {

    /**
     * The platforms known by the service. As in the actual service, each version of a platform is
     * a distinct entry, and all versions of the same platform share the same id.
     */
    private val platforms = listOf(
        PlatformInfo(1L, "PS", "PlayStation", "https://fake.url/image/ps_logo.png"),
        PlatformInfo(2L, "PS2", "PlayStation 2", "https://fake.url/image/ps2_logo.png"),
        PlatformInfo(3L, "PS3", "PlayStation 3", "https://fake.url/image/ps3_logo.png"),
        PlatformInfo(4L, "PS4", "PlayStation 4", "https://fake.url/image/ps4_logo.png", "Standard"),
        PlatformInfo(4L, "PS4", "PlayStation 4", "https://fake.url/image/ps4_logo.png", "Pro"),
        PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/image/ps5_logo.png", "Standard"),
        PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/image/ps5_logo.png", "Digital Edition"),
        PlatformInfo(5L, "PS5", "PlayStation 5", "https://fake.url/image/ps5_logo.png", "Pro"),
        PlatformInfo(6L, "XSX", "Xbox Series X/S", "https://fake.url/image/xbox_logo.png"),
        PlatformInfo(7L, "NSW", "Nintendo Switch", "https://fake.url/image/switch_logo.png", "Standard"),
        PlatformInfo(7L, "NSW", "Nintendo Switch", "https://fake.url/image/switch_logo.png", "OLED"),
        PlatformInfo(7L, "NSW", "Nintendo Switch", "https://fake.url/image/switch_logo.png", "Lite"),
        PlatformInfo(8L, "PC", "PC (Microsoft Windows)", "https://fake.url/image/pc_logo.png"),
        PlatformInfo(9L, "XONE", "Xbox One", "https://fake.url/image/xbox_one_logo.png"),
        PlatformInfo(10L, "3DS", "Nintendo 3DS", "https://fake.url/image/3ds_logo.png"),
        PlatformInfo(11L, "WII", "Nintendo Wii", "https://fake.url/image/wii_logo.png"),
        PlatformInfo(12L, "NSW2", "Nintendo Switch 2", "https://fake.url/image/switch_logo.png"),
    )

    override suspend fun search(query: SearchString): List<PlatformInfo> {
        delay(500.milliseconds) // Simulate network delay
        return platforms.filter { platform ->
            platform.name.value.contains(query.value, ignoreCase = true) ||
                platform.abbreviation.value.contains(query.value, ignoreCase = true)
        }
    }
}
