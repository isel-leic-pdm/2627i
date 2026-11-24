package isel.dei.pdm.demos.gamevault.collection.games.details.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import isel.dei.pdm.demos.gamevault.domain.NonBlankString
import isel.dei.pdm.demos.gamevault.domain.collection.platforms.Platform
import isel.dei.pdm.demos.gamevault.ui.common.TextPlaceholder
import isel.dei.pdm.demos.gamevault.ui.theme.DemoGameVaultTheme

/**
 * The header of the game details screen: the game's name and, below it, the full names of the
 * platforms it is owned on, as a comma-separated list, followed by a divider.
 *
 * The platforms are displayed sorted by name, ignoring case, as they are owned in no particular
 * order (see [isel.dei.pdm.demos.gamevault.domain.collection.games.CollectionEntry.platforms]).
 * Their versions are not displayed, as they are irrelevant for games (see
 * [Platform.isSameGamePlatformAs]).
 *
 * Invalid input is not rejected: a game is owned on at least one platform, but if there are none,
 * the platforms line is not displayed, instead of being displayed empty.
 *
 * @param name The game's name.
 * @param platforms The platforms the game is owned on.
 * @param modifier Modifier to be applied to the header's layout.
 */
@Composable
fun GameDetailsHeader(
    name: NonBlankString,
    platforms: Collection<Platform>,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = name.value,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.testTag(GameDetailsHeaderTestTags.NAME)
        )
        if (platforms.isNotEmpty()) {
            Text(
                text = platforms
                    .map { it.name.value }
                    .sortedWith(String.CASE_INSENSITIVE_ORDER)
                    .joinToString(separator = ", "),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(GameDetailsHeaderTestTags.PLATFORMS)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

/**
 * The header while the game is being read: placeholders instead of its name and platforms,
 * followed by the divider, so that the layout does not change once they are displayed.
 *
 * @param modifier Modifier to be applied to the header's layout.
 */
@Composable
fun GameDetailsHeaderPlaceholder(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        TextPlaceholder(style = MaterialTheme.typography.headlineLarge, width = 220.dp)
        TextPlaceholder(style = MaterialTheme.typography.titleMedium, width = 140.dp)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

// Testing & preview utilities

object GameDetailsHeaderTestTags {
    const val NAME = "GameDetailsHeaderName"
    const val PLATFORMS = "GameDetailsHeaderPlatforms"
}

@Suppress("Unused")
class GameDetailsHeaderPreviews {

    @Preview(showBackground = true, name = "Owned on one platform")
    @Composable
    fun OnePlatformPreview() {
        DemoGameVaultTheme {
            GameDetailsHeader(
                name = NonBlankString("Elden Ring"),
                platforms = setOf(Platform(1L, "PS5", "PlayStation 5")),
                modifier = Modifier.padding(16.dp)
            )
        }
    }

    @Preview(showBackground = true, name = "Owned on several platforms, with a long name")
    @Composable
    fun SeveralPlatformsPreview() {
        DemoGameVaultTheme {
            GameDetailsHeader(
                name = NonBlankString("The Legend of Zelda: Tears of the Kingdom"),
                platforms = setOf(
                    Platform(2L, "Switch", "Nintendo Switch"),
                    Platform(1L, "PS5", "PlayStation 5", version = "Pro"),
                    Platform(3L, "PC", "PC (Microsoft Windows)")
                ),
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
